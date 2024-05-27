@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.progdigy.fbclient

import kotlinx.cinterop.*
import org.firebirdsql.fbclient.*
import platform.posix.memcpy
import platform.posix.memset
import kotlin.math.min

@OptIn(ExperimentalForeignApi::class)
actual object API {
    private const val ERR_CONVERSION = "Data type conversion error"
    private const val ERR_OUT_OF_BOUND = "Index out of bound"
    private const val ERR_INVALID_HANDLE = "Invalid Handle value"
    private const val ERR_FIELD_NULL = "Field is null"
    private const val ERR_STRING_TRUNCATION = "String truncation"

    private const val ISC_SEGMENT = 335544366L

    private inline fun HANDLE.toXSQLDA() = toCPointer<CPointerVar<XSQLDA>>()?.pointed?.value?.pointed

    private inline fun xsqldaLength(n: ISC_SHORT): Long = sizeOf<XSQLDA>() + (n - 1) * sizeOf<XSQLVAR>()

    /**
     * Calculates the size of a UTF-8 encoded string up to a certain maximum length.
     *
     * @param string The null-terminated UTF-8 encoded string represented as a C pointer to an array of bytes.
     * @param maxLength The maximum length of the string to consider when calculating the size.
     * @param maxSize The maximum size of the string to consider when calculating the size.
     * @return The size of the UTF-8 encoded string in bytes.
     */
    private fun utf8Size(string: CPointer<ByteVarOf<Byte>>, maxLength: Int, maxSize: Int): Int {
        var length = 0
        var size = 0
        var value = string[size].toInt()
        while (value != 0) {
            if((value and 0xC0) != 0x80) length++
            if(length > maxLength || size++ == maxSize)
                break
            value = string[size].toInt()
        }
        return size
    }

    /**
     * @brief Allocates memory for the data buffer of an XSQLDA structure.
     *
     * This function allocates memory for a data buffer based on the information provided in the specified XSQLDA structure.
     * The data buffer is used to store the actual data values for each field in the XSQLDA structure.
     *
     * @param sqlda The XSQLDA structure containing the field definitions.
     *
     * @note The XSQLDA structure should be pre-initialized with the correct values for version, sqldaid, sqldabc, sqln, and sqld.
     *       The sqlvar array should contain the field definitions.
     */
    private fun allocateDataBuffer(sqlda: XSQLDA) {
        var total = 0L
        for (i in 0 until sqlda.sqld) {
            val v = sqlda.sqlvar[i]
            v.sqldata = total.toCPointer()
            when (v.sqltype.toInt() and 1.inv()) {
                SQL_TEXT -> {
                    // + zero terminal
                    total += v.sqllen
                    total++
                }
                SQL_VARYING -> {
                    // size of PARAMVARY + zero terminal
                    total += sizeOf<ISC_USHORTVar>() + v.sqllen
                    total++
                }
                SQL_FLOAT, SQL_D_FLOAT, SQL_DOUBLE -> {
                    // scale is irrelevant and lead to duplicate code
                    v.sqlscale = 0
                    total += v.sqllen
                }
                else -> {
                    total += v.sqllen
                }
            }
            if ((v.sqltype.toInt() and 1) == 1) {
                v.sqlind = total.toCPointer()
                total += sizeOf<ShortVar>()
            } else
                v.sqlind = null
        }
        val buffer = nativeHeap.allocArray<ISC_SCHARVar>(total)
        memset(buffer, 0, total.toULong())
        for (i in 0 until sqlda.sqld) {
            val v = sqlda.sqlvar[i]
            v.sqldata = buffer.plus(v.sqldata.toLong())
            if (v.sqlind != null) {
                v.sqlind = buffer.plus(v.sqlind!!.toLong())!!.reinterpret()
                v.sqlind!!.pointed.value = -1 // nullables are null
            }
        }
    }

    /**
     * Frees the memory allocated for the SQLDA structure.
     *
     * @param handle the handle to the SQLDA structure to be freed
     */
    actual fun freeSQLDA(handle: HANDLE) {
        val ptr = handle.toCPointer<CPointerVar<ByteVar>>()
        if (ptr != null) {
            val sqlda = ptr.pointed.value
            if (sqlda != null) {
                nativeHeap.free(sqlda)
                ptr.pointed.value = null
            }
        }
    }

    /**
     * Retrieves the field value from the given XSQLDA structure at the specified index.
     *
     * @param sqlda The XSQLDA structure containing the field values.
     * @param index The index of the field value to retrieve.
     * @param block The block of code that processes the field value.
     *
     * @return The processed field value.
     *
     * @throws FirebirdException if the XSQLDA is invalid, the index is out of bounds, or the field value is null.
     */

    private inline fun <T> getFieldValue(sqlda: HANDLE, index: Int, block: (ISC_SCHARVar, Int, Short, Short)->T): T {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                val v = p.sqlvar[index]
                if (v.sqlind?.pointed?.value == (-1).toShort())
                    throw FirebirdException(ERR_FIELD_NULL)
                else
                    return block(v.sqldata!!.pointed, v.sqltype.toInt() and 1.inv(), v.sqllen, v.sqlsubtype)
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    private inline fun <T> getFieldValue(sqlda: HANDLE, index: Int, block: (ISC_SCHARVar, Int)->T): T {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                val v = p.sqlvar[index]
                if (v.sqlind?.pointed?.value == (-1).toShort())
                    throw FirebirdException(ERR_FIELD_NULL)
                else
                    return block(v.sqldata!!.pointed, v.sqltype.toInt() and 1.inv())
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    /**
     * Sets the value of a field in a SQLDA structure.
     *
     * @param sqlda The SQLDA structure.
     * @param index The index of the field.
     * @param block The block of code to execute to set the field value.
     *              Parameters:
     *                - sqldata: The pointer to the field data.
     *                - sqlCode: The SQL code of the field.
     *                - sqlLen: The length of the field data.
     *                - sqlSubType: The subtype of the field data.
     *              Returns: Unit.
     *
     * @throws FirebirdException if the index is out of bounds or the handle is invalid.
     */
    private inline fun setFieldValue(sqlda: HANDLE, index: Int, block: (ISC_SCHARVar, Int, Short, Short)->Unit) {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                val v = p.sqlvar[index]
                block(v.sqldata!!.pointed, v.sqltype.toInt() and 1.inv(), v.sqllen, v.sqlsubtype)
                v.sqlind?.pointed?.value = 0
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    private inline fun setFieldValue(sqlda: HANDLE, index: Int, block: (ISC_SCHARVar, Int)->Unit) {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                val v = p.sqlvar[index]
                block(v.sqldata!!.pointed, v.sqltype.toInt() and 1.inv())
                v.sqlind?.pointed?.value = 0
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    /**
     * Retrieves a field from the SQLDA.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field to retrieve
     * @param block a lambda function that operates on the specified field
     * @return the result of the lambda function
     * @throws FirebirdException if the index is out of bounds or the handle is invalid
     */
    private inline fun <T> getField(sqlda: HANDLE, index: Int, block: (XSQLVAR)->T): T {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                return block(p.sqlvar[index])
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    /**
     * Sets the field value of the specified index in the given `sqlda`.
     * The field value is modified by the provided `block` function.
     *
     * @param sqlda The HANDLE representing the SQLDA.
     * @param index The index of the field.
     * @param block The lambda function that modifies the field value.
     * @throws FirebirdException If `sqlda` is `null` or if `index` is out of bounds.
     */
    private fun setField(sqlda: HANDLE, index: Int, block: (XSQLVAR)->Unit) {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            if (index >= 0 && index < p.sqld) {
                return block(p.sqlvar[index])
            } else
                throw FirebirdException("$ERR_OUT_OF_BOUND: $index")
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    /**
     * Allocates a handle.
     *
     * @return The allocated handle.
     */
    actual fun allocHandle(): HANDLE {
        val handle = nativeHeap.alloc<LongVar>()
        handle.value = 0L
        return handle.rawPtr.toLong()
    }

    /**
     * Frees the memory associated with a handle.
     *
     * @param handle The handle to be freed.
     */
    actual fun freeHandle(handle: HANDLE) {
        val ptr = handle.toCPointer<LongVar>()
        nativeHeap.free(ptr.rawValue)
    }

    /**
     * Allocates an array of ISC_STATUSVar objects in the native heap and initializes all elements to 0.
     *
     * @return A handle to the allocated array.
     */
    actual fun allocStatusArray(): HANDLE {
        val status = nativeHeap.allocArray<ISC_STATUSVar>(ISC_STATUS_LENGTH)
        for(i in 0 until ISC_STATUS_LENGTH) {
            status[i] = 0
        }
        return status.toLong()
    }

    /**
     * Frees the memory allocated for the status array.
     *
     * @param status The handle to the status array.
     */
    actual fun freeStatusArray(status: HANDLE) {
        val ptr = status.toCPointer<ISC_STATUSVar>()
        nativeHeap.free(ptr.rawValue)
    }

    /**
     * Interprets the given status and returns a description of it.
     *
     * @param status The handle to the status to be interpreted.
     * @return A string representing the description of the status.
     */
    actual fun interpret(status: HANDLE): String {
        val buffer = nativeHeap.allocArray<ISC_SCHARVar>(1024)
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val pStatusArray = nativeHeap.allocArray<CPointerVar<ISC_STATUSVar>>(1)
        pStatusArray[0] = statusArray

        var len = fb_interpret(buffer, 1024u, pStatusArray)
        var total = len
        while (len > 0 && total < 1024) {
            buffer[total++] = '\n'.code.toByte()
            len = fb_interpret(buffer + total, (1024 - total).toUInt(), pStatusArray)
            total += len
        }
        val result = buffer.toKString()
        nativeHeap.free(buffer)
        nativeHeap.free(pStatusArray)
        return result
    }

    /**
     * Attaches a database using the provided parameters.
     *
     * @param status The status handle for error reporting.
     * @param path The path of the database to attach.
     * @param dbHandle The handle to hold the attached database.
     * @param options Additional options for attaching the database (optional).
     * @return The status of the attachment operation.
     */
    actual fun attachDatabase(
        status: HANDLE,
        path: String,
        dbHandle: HANDLE,
        options: ByteArray?
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val dpb = options?.toCValues()
        val cPath = path.cstr
        val size = (dpb?.size?.toShort()) ?: 0
        return isc_attach_database(statusArray, cPath.size.toShort(), cPath, dbHandlePtr, size, dpb)
    }

    /**
     * Creates a database with the specified parameters.
     *
     * @param status The status handle to hold error information.
     * @param path The path to the database file.
     * @param dbHandle The handle to receive the database connection.
     * @param options The optional array of bytes containing database creation options.
     * @return The status code indicating the success or failure of the database creation.
     */
    actual fun createDatabase(
        status: HANDLE,
        path: String,
        dbHandle: HANDLE,
        options: ByteArray?
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val dpb = options?.toCValues()
        val cPath = path.cstr
        val size = (dpb?.size?.toUShort()) ?: 0u
        return isc_create_database(statusArray, cPath.size.toUShort(), cPath, dbHandlePtr, size, dpb, 0u)
    }

    /**
     * Detaches the database associated with the given handle.
     *
     * @param status The handle containing the status of the operation.
     * @param dbHandle The handle of the database to detach.
     * @return The status of the detach operation.
     */
    actual fun detachDatabase(status: HANDLE, dbHandle: HANDLE): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        return isc_detach_database(statusArray, dbHandlePtr)
    }

    /**
     * Executes an immediate SQL statement using the provided handles and SQL string.
     *
     * @param status The handle for the status array.
     * @param dbHandle The handle for the database.
     * @param trHandle The handle for the transaction.
     * @param sql The SQL string to execute.
     * @return The status after executing the SQL statement.
     */
    actual fun executeImmediate(
        status: HANDLE,
        dbHandle: HANDLE,
        trHandle: HANDLE,
        sql: String,
        dialect: Short
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val str = sql.cstr
        return isc_dsql_execute_immediate(statusArray, dbHandlePtr, trHandlePtr, str.size.toUShort(), str, dialect.toUShort(), null)
    }

    /**
     * Starts a transaction with the specified parameters.
     *
     * @param status The status array.
     * @param trHandle Handle to store the transaction handle.
     * @param dbHandle Handle to the database connection.
     * @param options Additional options for the transaction (nullable).
     * @return The status of the transaction start operation.
     */
    actual fun startTransaction(
        status: HANDLE,
        trHandle: HANDLE,
        dbHandle: HANDLE,
        options: ByteArray?
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val tpb = options?.toCValues()
        val len = (tpb?.size?.toShort()) ?: 0
        return isc_start_transaction(statusArray, trHandlePtr, 1, dbHandlePtr, len, tpb)
    }


    /**
     * Commits a transaction in the Firebird database.
     *
     * @param status The status array for error reporting. Should be of type `HANDLE`.
     * @param trHandle The handle of the transaction to commit. Should be of type `HANDLE`.
     * @param retain Flag indicating whether to retain the transaction context after committing.
     *               Set to `true` to retain the context, or `false` otherwise.
     * @return The status of the commit operation.
     */
    actual fun commitTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        return if (retain)
            isc_commit_retaining(statusArray, trHandlePtr)
        else
            isc_commit_transaction(statusArray, trHandlePtr)
    }

    /**
     * Rollbacks a database transaction.
     *
     * @param status The status array to store error information (output parameter).
     * @param trHandle The transaction handle.
     * @param retain Determines whether the transaction context should be retained after rollback.
     *        If `true`, the transaction context is retained; if `false`, the transaction context is discarded.
     * @return The status of the rollback operation.
     */
    actual fun rollbackTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        return if (retain)
            isc_rollback_retaining(statusArray, trHandlePtr)
        else
            isc_rollback_transaction(statusArray, trHandlePtr)
    }

    /**
     * Prepares an SQL statement for execution.
     *
     * @param status The status array to store any errors or warnings.
     * @param dbHandle The database handle.
     * @param trHandle The transaction handle.
     * @param stHandle The statement handle.
     * @param sql The SQL string to prepare.
     * @param cursor The cursor name (if any) to associate with the statement.
     * @param dialect The SQL dialect.
     * @param sqlda The handle to the XSQLDA structure.
     * @return The status code.
     */
    actual fun prepareStatement(
        status: HANDLE,
        dbHandle: HANDLE,
        trHandle: HANDLE,
        stHandle: HANDLE,
        sql: String,
        cursor: String?,
        dialect: Short,
        sqlda: HANDLE
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        val sqldaPtr = sqlda.toCPointer<CPointerVar<XSQLDA>>()
        val str = sql.cstr

        var ret = isc_dsql_allocate_statement(statusArray, dbHandlePtr, stHandlePtr)

        if (ret == 0L) {
            var len = xsqldaLength(1)

            val da = nativeHeap.allocArray<ByteVar>(len){ value = 0 }.reinterpret<XSQLDA>().pointed
            da.version = SQLDA_VERSION1.toShort()

            try {
                ret = isc_dsql_prepare(statusArray, trHandlePtr, stHandlePtr, str.size.toUShort(), str, dialect.toUShort(), da.ptr)

                if (ret == 0L) {
                    if (cursor != null)
                        ret = isc_dsql_set_cursor_name(statusArray, stHandlePtr, cursor, 0u)

                    if (ret == 0L && sqldaPtr != null && da.sqld > 0) {
                        len = xsqldaLength(da.sqld)

                        val pXSQLDA = nativeHeap.allocArray<ByteVar>(len){value = 0}.reinterpret<XSQLDA>().pointed

                        pXSQLDA.version = SQLDA_VERSION1.toShort()
                        pXSQLDA.sqln = da.sqld
                        ret = isc_dsql_describe(statusArray, stHandlePtr, dialect.toUShort(), pXSQLDA.ptr)
                        if (ret == 0L) {
                            sqldaPtr.pointed.value = pXSQLDA.ptr
                            allocateDataBuffer(pXSQLDA)
                        } else {
                            nativeHeap.free(pXSQLDA)
                        }
                    }
                }
            } finally {
                nativeHeap.free(da)
            }
        }
        return ret
    }

    /**
     * Retrieves the type of a SQL statement.
     *
     * @param status The status handle.
     * @param stHandle The statement handle.
     * @return The type of the SQL statement as an integer.
     * @throws FirebirdException if the provided statement handle is invalid.
     */
    actual fun getStatementType(status: HANDLE, stHandle: HANDLE): Int {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        if (stHandlePtr == null || stHandlePtr.pointed.value == 0u)
            throw FirebirdException(ERR_INVALID_HANDLE)
        val data = nativeHeap.allocArray<ByteVar>(9)
        data[0] = isc_info_sql_stmt_type.toByte()
        isc_dsql_sql_info(statusArray, stHandlePtr, 1, data, 8, data.plus(1))
        val ret = data[4] - 1
        nativeHeap.free(data)
        return ret
    }

    /**
     * Executes a SQL statement with the given parameters.
     *
     * @param status The status array handle.
     * @param trHandle The transaction handle.
     * @param stHandle The statement handle.
     * @param dialect The SQL dialect.
     * @param sqlda The prepared statement handle.
     * @return The status of the execution.
     */
    actual fun execute(
        status: HANDLE,
        trHandle: HANDLE,
        stHandle: HANDLE,
        dialect: Short,
        sqlda: HANDLE
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        val xsqldaPtr = sqlda.toCPointer<CPointerVar<XSQLDA>>()
        val da = xsqldaPtr?.pointed?.value
        return isc_dsql_execute(statusArray, trHandlePtr, stHandlePtr, dialect.toUShort(), da)
    }

    /**
     * Executes a SQL statement with the given parameters and returns the status of the execution.
     *
     * @param status The status handle.
     * @param trHandle The transaction handle.
     * @param stHandle The statement handle.
     * @param dialect The dialect of the SQL statement.
     * @param input The input SQLDA handle.
     * @param output The output SQLDA handle.
     * @return The status of the execution.
     */
    actual fun execute2(
        status: HANDLE,
        trHandle: HANDLE,
        stHandle: HANDLE,
        dialect: Short,
        input: HANDLE,
        output: HANDLE
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        val i = input.toCPointer<CPointerVar<XSQLDA>>()
        val o = output.toCPointer<CPointerVar<XSQLDA>>()
        val ida = i?.pointed?.value
        val oda = o?.pointed?.value
        return isc_dsql_execute2(statusArray, trHandlePtr, stHandlePtr, dialect.toUShort(), ida, oda)
    }

    /**
     * Fetches the next row of data from the database.
     *
     * @param status The HANDLE object for the status.
     * @param stHandle The HANDLE object for the statement.
     * @param sqlda The HANDLE object for the SQLDA.
     * @return The status of the fetch operation.
     */
    actual fun fetch(status: HANDLE, stHandle: HANDLE, sqlda: HANDLE): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        val xsqldaPtr = sqlda.toCPointer<CPointerVar<XSQLDA>>()
        val da = xsqldaPtr?.pointed?.value
        return isc_dsql_fetch(statusArray,  stHandlePtr, SQLDA_VERSION1.toUShort(), da)
    }

   /**
    * Free a prepared statement handle.
    *
    * @param status The ISC_STATUS array.
    * @param stHandle The handle for the prepared statement.
    * @param action The action to perform: DSQL_close, DSQL_reset, DSQL_unprepare, or DSQL_drop.
    * @return The status of the operation.
    */
   actual fun freeStatement(status: HANDLE, stHandle: HANDLE, action: Short): STATUS {
       val statusArray = status.toCPointer<ISC_STATUSVar>()
       val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
       val ret = isc_dsql_free_statement(statusArray, stHandlePtr, action.toUShort())
       if (action == DSQL_drop)
            stHandlePtr?.pointed?.value = 0u
       return ret
   }

    /**
     * Prepares the parameters for the given SQL statement.
     *
     * @param status The status array.
     * @param stHandle The statement handle.
     * @param dialect The database dialect.
     * @param sqlda The XSQLDA handle.
     * @return The status of the operation.
     */
    actual fun prepareParams(
        status: HANDLE,
        stHandle: HANDLE,
        dialect: Short,
        sqlda: HANDLE
    ): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val stHandlePtr = stHandle.toCPointer<FB_API_HANDLEVar>()
        val sqldaPtr = sqlda.toCPointer<CPointerVar<XSQLDA>>()
        var len = xsqldaLength(1)
        val da = nativeHeap.allocArray<ByteVar>(len).reinterpret<XSQLDA>().pointed
        da.version = SQLDA_VERSION1.toShort()
        da.sqld = 0
        da.sqln = 0

        var ret = isc_dsql_describe_bind(statusArray, stHandlePtr, dialect.toUShort(), da.ptr)
        if (ret == 0L && da.sqld > 0) {
            len = xsqldaLength(da.sqld)
            val pXSQLDA = nativeHeap.allocArray<ByteVar>(len){ value = 0 }.reinterpret<XSQLDA>().pointed
            memset(pXSQLDA.ptr, 0, len.toULong())
            pXSQLDA.version = SQLDA_VERSION1.toShort()
            pXSQLDA.sqln = da.sqld
            ret = isc_dsql_describe_bind(statusArray, stHandlePtr, dialect.toUShort(), pXSQLDA.ptr)
            if (ret == 0L && sqldaPtr != null) {
                sqldaPtr.pointed.value = pXSQLDA.ptr
                allocateDataBuffer(pXSQLDA)
            } else {
                nativeHeap.free(pXSQLDA)
            }
        }

        nativeHeap.free(da)
        return ret
    }

    /**
     * Sets the field value of the specified index in the given SQLDA to null.
     *
     * @param sqlda The HANDLE representing the SQLDA.
     * @param index The index of the field.
     * @throws FirebirdException If `sqlda` is `null` or if `index` is out of bounds.
     */
    actual fun setIsNull(sqlda: HANDLE, index: Int) {
        setField(sqlda, index) { v ->
            v.sqlind?.pointed?.value = -1
        }
    }

    /**
     * Sets the value of a field in the given SQLDA structure at the specified index to a boolean value.
     *
     * @param sqlda The SQLDA structure to update.
     * @param index The index of the field in the SQLDA structure.
     * @param value The boolean value to set.
     * @throws FirebirdException If the conversion of the value fails or the index is out of bound.
     */
    actual fun setValueBoolean(sqlda: HANDLE, index: Int, value: Boolean) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_BOOLEAN -> data.reinterpret<ByteVar>().value = value.toByte()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a short field in a given SQLDA handle at the specified index.
     *
     * @param sqlda The SQL descriptor area handle.
     * @param index The index of the field.
     * @param value The value to be set.
     * @throws FirebirdException If the field is out of bounds or if there is a conversion error.
     */
    actual fun setValueShort(sqlda: HANDLE, index: Int, value: Short) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_SHORT -> data.reinterpret<ShortVar>().value = value
                SQL_LONG -> data.reinterpret<IntVar>().value = value.toInt()
                SQL_QUAD, SQL_INT64 ->
                    data.reinterpret<LongVar>().value = value.toLong()
                SQL_FLOAT ->
                    data.reinterpret<FloatVar>().value = value.toFloat()
                SQL_DOUBLE, SQL_D_FLOAT ->
                    data.reinterpret<DoubleVar>().value = value.toDouble()
                SQL_INT128 ->
                    data.reinterpret<FB_I128>().apply {
                        fb_data[0] = value.toULong()
                        fb_data[1] = if (value < 0) (-1).toULong() else 0u
                    }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the integer value of a field in the SQLDA structure.
     *
     * @param sqlda The SQLDA structure.
     * @param index The index of the field within the SQLDA structure.
     * @param value The integer value to be set.
     * @throws FirebirdException when an error occurs during the conversion or the index is out of bounds.
     */
    actual fun setValueInt(sqlda: HANDLE, index: Int, value: Int) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_LONG -> data.reinterpret<IntVar>().value = value
                SQL_QUAD, SQL_INT64 ->
                    data.reinterpret<LongVar>().value = value.toLong()
                SQL_DOUBLE, SQL_D_FLOAT ->
                    data.reinterpret<DoubleVar>().value = value.toDouble()
                SQL_INT128 ->
                    data.reinterpret<FB_I128>().apply {
                        fb_data[0] = value.toULong()
                        fb_data[1] = if (value < 0) (-1).toULong() else 0u
                    }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a Long field in the specified SQL descriptor.
     *
     * @param sqlda The SQL descriptor handle.
     * @param index The index of the field in the SQL descriptor.
     * @param value The value to be set.
     * @throws FirebirdException if an error occurs during the conversion or if the index is out of bounds.
     */
    actual fun setValueLong(sqlda: HANDLE, index: Int, value: Long) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_QUAD, SQL_INT64 ->
                    data.reinterpret<LongVar>().value = value
                SQL_INT128 ->
                    data.reinterpret<FB_I128>().apply {
                        fb_data[0] = value.toULong()
                        fb_data[1] = if (value < 0) (-1).toULong() else 0u
                    }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a 128-bit signed integer field in the given SQLDA structure at the specified index.
     *
     * @param sqlda The SQLDA structure to update.
     * @param index The index of the field in the SQLDA structure.
     * @param a The most significant 64 bits of the value.
     * @param b The least significant 64 bits of the value.
     * @throws FirebirdException if an error occurs during the conversion or when accessing the SQLDA structure.
     */
    actual fun setValueInt128(sqlda: HANDLE, index: Int, a: Long, b: Long) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_INT128 ->
                    data.reinterpret<FB_I128>().apply {
                        fb_data[0] = a.toULong()
                        fb_data[1] = b.toULong()
                    }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a string field in a SQLDA structure.
     *
     * @param status The status handle.
     * @param dbHandle The database handle.
     * @param trHandle The transaction handle.
     * @param sqlda The SQLDA structure.
     * @param index The index of the field.
     * @param value The string value to set.
     *
     * @throws FirebirdException if the index is out of bounds or the handle is invalid.
     * @throws FirebirdException if there is an error during the field value setting.
     */
    actual fun setValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: String) =
        setFieldValue(sqlda, index) { data, sqlCode, sqlLen, sqlSubType ->
            when (sqlCode) {
                SQL_VARYING ->
                    if (sqlSubType == 4.toShort()) {
                        val vary = data.reinterpret<PARAMVARY>()
                        val len = sqlLen / 4
                        if (value.length <= len) {
                            val valueBytes = value.encodeToByteArray()
                            val size = valueBytes.size
                            memcpy(vary.vary_string, valueBytes.refTo(0), size.toULong())
                            vary.vary_length = size.toUShort()
                        } else
                            throw FirebirdException("$ERR_STRING_TRUNCATION: $index")
                    } else
                        throw FirebirdException("$ERR_CONVERSION ($index)")
                SQL_TEXT ->
                    if (sqlSubType == 4.toShort()) {
                        val len = sqlLen / 4
                        if (value.length <= len) {
                            val valueBytes = value.encodeToByteArray()
                            val size = valueBytes.size
                            memset(data.ptr, 32, sqlLen.toULong())
                            memcpy(data.ptr, valueBytes.refTo(0), size.toULong())
                        } else
                            throw FirebirdException("$ERR_STRING_TRUNCATION: $index")
                    } else
                        throw FirebirdException("$ERR_CONVERSION ($index)")
                SQL_BLOB ->
                    if (sqlSubType == 1.toShort()) {
                        val statusArray = status.toCPointer<ISC_STATUSVar>()
                        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
                        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
                        memScoped {
                            val blob = memScope.alloc<FB_API_HANDLEVar>()
                            blob.value = 0u
                            val blobId = data.reinterpret<GDS_QUAD>()
                            var ret = isc_create_blob(statusArray, dbHandlePtr, trHandlePtr, blob.ptr, blobId.ptr)
                            if (ret == 0L) {
                                val bytes = value.cstr
                                var length = bytes.size - 1 // remove zero terminal
                                var toWrite = min(length, Short.MAX_VALUE.toInt())
                                var p: CPointer<ByteVar>? = bytes.getPointer(this)
                                while (length > 0 && isc_put_segment(statusArray, blob.ptr, toWrite.toUShort(), p) == 0L) {
                                    length -= toWrite
                                    if (length <= 0)
                                        break
                                    toWrite = min(length, Short.MAX_VALUE.toInt())
                                    p = p + toWrite
                                }

                                ret = isc_close_blob(statusArray, blob.ptr)
                            }
                            if (ret != 0L)
                                throw FirebirdException(interpret(status))
                        }
                    } else
                        throw FirebirdException("$ERR_CONVERSION ($index)")
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a field with a byte array in the SQLDA structure.
     *
     * @param status The status handle.
     * @param dbHandle The database handle.
     * @param trHandle The transaction handle.
     * @param sqlda The SQLDA structure.
     * @param index The index of the field.
     * @param value The byte array containing the value to be set.
     * @throws FirebirdException if there is an error setting the field value.
     */
    actual fun setValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: ByteArray) =
        setFieldValue(sqlda, index) { data, sqlCode, sqlLen, _ ->
            when (sqlCode) {
                SQL_VARYING -> {
                    val vary = data.reinterpret<PARAMVARY>()
                    if (value.size <= sqlLen) {
                        memcpy(vary.vary_string, value.refTo(0), value.size.toULong())
                        vary.vary_length = value.size.toUShort()
                    } else
                        throw FirebirdException("$ERR_STRING_TRUNCATION: $index")
                }
                SQL_TEXT -> {
                    if (value.size <= sqlLen) {
                        memcpy(data.ptr, value.refTo(0), value.size.toULong())
                    } else
                        throw FirebirdException("$ERR_STRING_TRUNCATION: $index")
                }
                SQL_BLOB -> {
                    val statusArray = status.toCPointer<ISC_STATUSVar>()
                    val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
                    val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
                    memScoped {
                        val blob = memScope.alloc<FB_API_HANDLEVar>()
                        blob.value = 0u
                        val blobId = data.reinterpret<GDS_QUAD>()
                        var ret = isc_create_blob(statusArray, dbHandlePtr, trHandlePtr, blob.ptr, blobId.ptr)
                        if (ret == 0L) {
                            value.usePinned {
                                var p = 0
                                var length = value.size
                                var toWrite = min(length, Short.MAX_VALUE.toInt())
                                while (length > 0 && isc_put_segment(statusArray, blob.ptr, toWrite.toUShort(), it.addressOf(p)) == 0L) {
                                    length -= toWrite
                                    if (length <= 0)
                                        break
                                    toWrite = min(length, Short.MAX_VALUE.toInt())
                                    p += toWrite
                                }
                            }

                            ret = isc_close_blob(statusArray, blob.ptr)
                        }
                        if (ret != 0L)
                            throw FirebirdException(interpret(status))
                    }
                }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets a Float value at the given index in the provided SQLDA object.
     *
     * @param sqlda The SQLDA object.
     * @param index The index of the value.
     * @param value The Float value to set.
     * @throws FirebirdException If an error occurs during the conversion or if the index is out of bounds.
     */
    actual fun setValueFloat(sqlda: HANDLE, index: Int, value: Float) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_FLOAT ->
                    data.reinterpret<FloatVar>().value = value
                SQL_DOUBLE, SQL_D_FLOAT ->
                    data.reinterpret<DoubleVar>().value = value.toDouble()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a DOUBLE or D_FLOAT field in a SQLDA structure.
     *
     * @param sqlda The SQLDA structure.
     * @param index The index of the field in the SQLDA structure.
     * @param value The value to set.
     * @throws FirebirdException If the field type is not DOUBLE or D_FLOAT.
     * @throws FirebirdException If the index is out of bounds.
     */
    actual fun setValueDouble(sqlda: HANDLE, index: Int, value: Double) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_DOUBLE, SQL_D_FLOAT ->
                    data.reinterpret<DoubleVar>().value = value
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a date field in the provided `sqlda` structure at the specified `index`.
     *
     * @param sqlda The SQLDA structure.
     * @param index The index of the field in the SQLDA structure.
     * @param value The value to be set for the date field.
     * @throws FirebirdException if the SQL code is not recognized as a date or timestamp type.
     */
    actual fun setValueDate(sqlda: HANDLE, index: Int, value: Int) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_TYPE_DATE,
                SQL_TIMESTAMP,
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    data.reinterpret<ISC_DATEVar>().value = value + 40587
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a time field in the given SQLDA at the specified index.
     *
     * @param sqlda The SQLDA handle.
     * @param index The index of the field in the SQLDA.
     * @param value The value to set for the time field.
     * @throws FirebirdException if an error occurs while setting the value.
     */
    actual fun setValueTime(sqlda: HANDLE, index: Int, value: Int) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_TYPE_TIME,
                SQL_TIME_TZ,
                SQL_TIME_TZ_EX ->
                    data.reinterpret<ISC_TIMEVar>().value = value.toUInt() * 10u
                SQL_TIMESTAMP,
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    data.reinterpret<ISC_TIMESTAMP>().timestamp_time = value.toUInt() * 10u
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the time zone value of a field in the SQLDA structure.
     * The time zone value is used for fields of type SQL_TIME_TZ, SQL_TIME_TZ_EX, SQL_TIMESTAMP_TZ, SQL_TIMESTAMP_TZ_EX.
     *
     * @param sqlda The SQLDA structure.
     * @param index The index of the field in the SQLDA structure.
     * @param value The time zone value to be set.
     * @throws FirebirdException if the field type is not supported or an error occurs.
     */
    actual fun setValueTimeZone(sqlda: HANDLE, index: Int, value: Int) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_TIME_TZ,
                SQL_TIME_TZ_EX ->
                    data.reinterpret<ISC_TIME_TZ>().time_zone = value.toUShort()
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    data.reinterpret<ISC_TIMESTAMP_TZ>().time_zone = value.toUShort()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Sets the value of a BLOB ID field at the specified index in the given `sqlda`.
     *
     * @param sqlda The handle to the SQLDA structure.
     * @param index The index of the BLOB ID field.
     * @param value The value to be set.
     */
    actual fun setValueBlobId(sqlda: HANDLE, index: Int, value: Long) =
        setFieldValue(sqlda, index) { data, sqlCode ->
            when(sqlCode) {
                SQL_BLOB ->
                    data.reinterpret<LongVar>().value = value
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Returns the type of the field at the specified index in the SQLDA.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field to retrieve
     * @return the type of the field at the specified index
     */
    actual fun getType(sqlda: HANDLE, index: Int): Int =
        getField(sqlda, index) { v ->
            return@getField when (v.sqltype.toInt() and 1.inv()) {
                SQL_SHORT -> 0
                SQL_LONG -> 1
                SQL_QUAD, SQL_INT64 -> 2
                SQL_FLOAT -> 3
                SQL_D_FLOAT, SQL_DOUBLE -> 4
                SQL_TEXT, SQL_VARYING ->
                    if (v.sqlsubtype != 0.toShort()) 5 else 6
                SQL_INT128 -> 7
                SQL_BOOLEAN -> 8
                SQL_TYPE_DATE -> 0
                SQL_TYPE_TIME -> 10
                SQL_TIMESTAMP -> 11
                SQL_TIME_TZ, SQL_TIME_TZ_EX -> 12
                SQL_TIMESTAMP_TZ, SQL_TIMESTAMP_TZ_EX -> 13
                SQL_BLOB -> if (v.sqlsubtype == 1.toShort()) 15 else 14
                else -> -1  // if none of the cases match
            }
        }

    /**
     * Retrieves the number of fields in the specified SQLDA. [sqlda].
     *
     * @param sqlda The SQLDA handle from which to retrieve the count.
     * @return The count retrieved from the SQLDA handle.
     * @throws FirebirdException if the SQLDA handle is invalid.
     */
    actual fun getCount(sqlda: HANDLE): Int {
        val p = sqlda.toXSQLDA()
        if (p != null) {
            return p.sqld.toInt()
        } else
            throw FirebirdException(ERR_INVALID_HANDLE)
    }

    /**
     * Returns the name of the field at the specified index in the given SQLDA.
     *
     * @param sqlda the SQLDA containing the SQL data
     * @param index the index of the SQL data in the SQLDA
     * @return the name of the field
     */
    actual fun getName(sqlda: HANDLE, index: Int): String {
        return getField(sqlda, index) {v ->
            val buffer = ByteArray(v.sqlname_length.toInt())
            buffer.usePinned {
                val pointer = it.addressOf(0)
                memcpy(pointer, v.sqlname, v.sqlname_length.toULong())
            }
            return@getField buffer.decodeToString()
        }
    }

    /**
     * Retrieves the relation associated with the given SQLDA and index.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the relation to retrieve
     * @return the relation associated with the given field
     */
    actual fun getRelation(sqlda: HANDLE, index: Int): String {
        return getField(sqlda, index) {v ->
            val buffer = ByteArray(v.relname_length.toInt())
            buffer.usePinned {
                val pointer = it.addressOf(0)
                memcpy(pointer, v.relname, v.relname_length.toULong())
            }
            return@getField buffer.decodeToString()
        }
    }

    /**
     * Retrieves the owner of a given SQLDA at the specified index.
     *
     * @param sqlda the SQLDA handle.
     * @param index the index of the SQLDA.
     * @return the owner of the field.
     */
    actual fun getOwner(sqlda: HANDLE, index: Int): String {
        return getField(sqlda, index) {v ->
            val buffer = ByteArray(v.ownname_length.toInt())
            buffer.usePinned {
                val pointer = it.addressOf(0)
                memcpy(pointer, v.ownname, v.ownname_length.toULong())
            }
            return@getField buffer.decodeToString()
        }
    }

    /**
     * Retrieves the alias name of a field in the SQLDA at the specified index.
     *
     * @param sqlda   the SQLDA handle
     * @param index   the index of the field to retrieve
     * @return        the alias name of the field
     * @throws FirebirdException if the index is out of bounds or the handle is invalid
     */
    actual fun getAlias(sqlda: HANDLE, index: Int): String {
        return getField(sqlda, index) {v ->
            val buffer = ByteArray(v.aliasname_length.toInt())
            buffer.usePinned {
                val pointer = it.addressOf(0)
                memcpy(pointer, v.aliasname, v.aliasname_length.toULong())
            }
            return@getField buffer.decodeToString()
        }
    }

    /**
     * Retrieves the null status of a field from the SQLDA.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field to retrieve
     * @return true if the field is null, false otherwise
     * @throws FirebirdException if the index is out of bounds or the handle is invalid
     */
    actual fun getIsNull(sqlda: HANDLE, index: Int): Boolean =
        getField(sqlda, index) { v ->
            return@getField v.sqlind?.pointed?.value == (-1).toShort()
        }

    /**
     * Retrieves the scale of a field from the SQLDA.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field to retrieve
     * @return the scale of the field as a Long value
     * @throws FirebirdException if the index is out of bounds or the handle is invalid
     */
    actual fun getScale(sqlda: HANDLE, index: Int): Long =
        getField(sqlda, index) { v->
            return@getField v.sqlscale.toLong()
        }

    /**
     * Retrieves the boolean value of the specified field from the given sqlda handle.
     *
     * @param sqlda The sqlda handle.
     * @param index The index of the field.
     * @return The boolean value of the field.
     * @throws FirebirdException if an error occurs during the conversion or if the field is out of bounds.
     */
    actual fun getValueBoolean(sqlda: HANDLE, index: Int): Boolean =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_BOOLEAN -> data.reinterpret<ByteVar>().value.toBoolean()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value at the specified index in the SQLDA as a Short.
     *
     * @param sqlda The SQLDA handle.
     * @param index The index of the value to retrieve.
     * @return The retrieved Short value.
     * @throws FirebirdException If an error occurs.
     */
    actual fun getValueShort(sqlda: HANDLE, index: Int): Short =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_SHORT -> data.reinterpret<ShortVar>().value
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the integer value from the specified field at the given index in the SQLDA.
     *
     * @param sqlda The SQLDA handle.
     * @param index The index of the desired field.
     * @return The integer value of the field.
     * @throws FirebirdException If an error occurs during the conversion or if the index is out of bounds.
     */
    actual fun getValueInt(sqlda: HANDLE, index: Int): Int =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_LONG -> data.reinterpret<IntVar>().value
                SQL_SHORT -> data.reinterpret<ShortVar>().value.toInt()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value at the specified index as a long integer.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field to retrieve the value from
     * @return the value at the specified index as a long integer
     * @throws FirebirdException if there is an error retrieving the value or if the conversion fails
     */
    actual fun getValueLong(sqlda: HANDLE, index: Int): Long =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_INT64, SQL_QUAD -> data.reinterpret<LongVar>().value
                SQL_LONG -> data.reinterpret<IntVar>().value.toLong()
                SQL_SHORT -> data.reinterpret<ShortVar>().value.toLong()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value of a field at the specified index as a string.
     *
     * @param status The handle to the firebird status.
     * @param dbHandle The handle to the firebird database.
     * @param trHandle The handle to the firebird transaction.
     * @param sqlda The handle to the XSQLDA structure containing the field values.
     * @param index The index of the field value to retrieve.
     *
     * @return The value of the field as a string.
     *
     * @throws FirebirdException if the XSQLDA is invalid, the index is out of bounds, or the field value is null.
     */
    actual fun getValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): String =
        getFieldValue(sqlda, index) { data, sqlCode, sqlLen, sqlSubType ->

                return@getFieldValue when (sqlCode) {
                    SQL_VARYING ->
                        if (sqlSubType > 0) {
                            val vary = data.reinterpret<PARAMVARY>()
                            val length = vary.vary_length
                            vary.vary_string[length.toInt()] = 0u
                            vary.vary_string.reinterpret<ByteVar>().toKString()
                        } else
                            throw FirebirdException("$ERR_CONVERSION ($index)")
                    SQL_TEXT ->
                        if (sqlSubType > 0) {
                            val arr = data.ptr.reinterpret<ByteVar>()
                            val size = utf8Size(arr, sqlLen / sqlSubType, sqlLen.toInt())
                            arr[size] = 0
                            arr.toKString()
                        } else
                            throw FirebirdException("$ERR_CONVERSION ($index)")
                    SQL_BLOB -> {
                        if (sqlSubType == 1.toShort()) { // text
                            getBlobData(status, dbHandle, trHandle, data).decodeToString()
                        } else
                            throw FirebirdException("$ERR_CONVERSION ($index)")
                    }
                    else ->
                        throw FirebirdException("$ERR_CONVERSION ($index)")
                }

        }

    /**
     * Retrieves the byte array value from the given XSQLDA structure at the specified index.
     *
     * @param status The status handle.
     * @param dbHandle The database handle.
     * @param trHandle The transaction handle.
     * @param sqlda The XSQLDA structure containing the field values.
     * @param index The index of the field value to retrieve.
     *
     * @return The byte array value retrieved from the XSQLDA structure.
     *
     * @throws FirebirdException if the XSQLDA is invalid, the index is out of bounds, or the field value is null.
     */
    actual fun getValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): ByteArray =
        getFieldValue(sqlda, index) { data, sqlCode, sqlLen, _ ->
            return@getFieldValue when (sqlCode) {
                SQL_VARYING -> {
                    val vary = data.reinterpret<PARAMVARY>()
                    val length = vary.vary_length
                    val buffer = ByteArray(length.toInt())
                    buffer.usePinned {
                        val pointer = it.addressOf(0)
                        memcpy(pointer, vary.vary_string, length.toULong())
                    }
                    buffer
                }

                SQL_TEXT -> {
                    val buffer = ByteArray(sqlLen.toInt())
                    buffer.usePinned {
                        val pointer = it.addressOf(0)
                        memcpy(pointer, data.ptr, sqlLen.toULong())
                    }
                    buffer
                }
                SQL_BLOB ->
                    getBlobData(status, dbHandle, trHandle, data)
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the byte array data of a blob from the Firebird database.
     *
     * @param status The handle to the Firebird status.
     * @param dbHandle The handle to the Firebird database.
     * @param trHandle The handle to the Firebird transaction.
     * @param data The blob data to retrieve.
     *
     * @return The byte array data of the blob.
     *
     * @throws FirebirdException if there is an error retrieving the blob data.
     */
    private fun getBlobData(
        status: HANDLE,
        dbHandle: HANDLE,
        trHandle: HANDLE,
        data: ISC_SCHARVar
    ): ByteArray {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        memScoped {
            val blob = alloc<FB_API_HANDLEVar>()
            blob.value = 0u
            val blobId = data.reinterpret<GDS_QUAD>()
            //char* str = nullptr
            var ret = isc_open_blob(statusArray, dbHandlePtr, trHandlePtr, blob.ptr, blobId.ptr)
            if (ret == 0L) {
                val buffer = allocArray<ByteVar>(9)
                val info = allocArray<ByteVar>(1)
                var arr: ByteArray? = null
                info[0] = isc_info_blob_total_length.toByte()
                ret = isc_blob_info(statusArray, blob.ptr, 1, info, 9, buffer)
                if (ret == 0L) {
                    var length = (buffer + 3)!!.reinterpret<ISC_LONGVar>().pointed.value
                    arr = ByteArray(length)
                    if (length > 0) {
                        var toRead = min(length, Short.MAX_VALUE.toInt())
                        val size = alloc<ISC_USHORTVar>()
                        arr.usePinned {
                            var p = 0
                            ret = isc_get_segment(statusArray, blob.ptr, size.ptr, toRead.toUShort(), it.addressOf(p))
                            while (ret == 0L || statusArray!![1] == ISC_SEGMENT) {
                                length -= size.value.toInt()
                                if (length <= 0)
                                    break
                                toRead = min(length, Short.MAX_VALUE.toInt())
                                p += size.value.toInt()
                                ret = isc_get_segment(statusArray, blob.ptr, size.ptr, toRead.toUShort(), it.addressOf(p))
                            }
                        }
                    }
                }
                ret = isc_close_blob(statusArray, blob.ptr)

                if (arr != null)
                    return arr
            }
        }

        throw FirebirdException(interpret(status))
    }

    /**
     * Retrieves the float value from the specified field in the given SQLDA handle.
     *
     * @param sqlda the SQLDA handle
     * @param index the index of the field
     * @return the float value of the field
     * @throws FirebirdException if there is an error retrieving the value or the conversion is not possible
     */
    actual fun getValueFloat(sqlda: HANDLE, index: Int): Float =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_FLOAT -> data.reinterpret<FloatVar>().value
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value at the given index in the SQLDA as a Double.
     *
     * @param sqlda The SQLDA handle.
     * @param index The index of the value in the SQLDA.
     * @return The value at the specified index as a Double.
     * @throws FirebirdException if there is an error in the conversion or if the index is out of bounds.
     */
    actual fun getValueDouble(sqlda: HANDLE, index: Int): Double =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_D_FLOAT, SQL_DOUBLE -> data.reinterpret<DoubleVar>().value
                SQL_FLOAT -> data.reinterpret<FloatVar>().value.toDouble()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value of a 128-bit integer from the specified sqlda and index.
     *
     * @param sqlda The handle to the sqlda.
     * @param index The index of the value within the sqlda.
     * @return An array of two long values representing the 128-bit integer.
     * @throws FirebirdException if conversion fails or the index is out of bounds.
     */
    actual fun getValueInt128(sqlda: HANDLE, index: Int): LongArray =
        getFieldValue(sqlda, index) { data, sqlCode  ->
            return@getFieldValue when (sqlCode) {
                SQL_INT128 -> {
                    val ret = LongArray(2)
                    ret.usePinned {
                        val pointer = it.addressOf(0)
                        memcpy(pointer, data.ptr, 16u)
                    }
                    ret
                }

                SQL_QUAD, SQL_INT64 -> {
                    val int64 = data.reinterpret<LongVar>().value
                    longArrayOf(int64, if(int64>=0L) 0L else -1L)
                }
                SQL_LONG -> {
                    val int32 = data.reinterpret<IntVar>().value
                    longArrayOf(int32.toLong(), if(int32>=0) 0L else -1L)
                }
                SQL_SHORT -> {
                    val int16 = data.reinterpret<ShortVar>().value
                    longArrayOf(int16.toLong(), if(int16>=0) 0L else -1L)
                }
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Returns the value of the given field as a date.
     *
     * @param sqlda the XSQLDA handle
     * @param index the index of the field
     * @return the value of the field as a date
     * @throws FirebirdException if an error occurs during the conversion
     */
    actual fun getValueDate(sqlda: HANDLE, index: Int): Int =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_TYPE_DATE,
                SQL_TIMESTAMP,
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    //LocalDate(1858, 11, 17).toEpochDays() = -40587
                    data.reinterpret<ISC_DATEVar>().value - 40587
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the time value from the specified sqlda and index.
     *
     * @param sqlda The sqlda object.
     * @param index The index of the desired value in the sqlda object.
     * @return The retrieved time value as an Int.
     * @throws FirebirdException If the conversion fails or the index is out of bounds.
     */
    actual fun getValueTime(sqlda: HANDLE, index: Int): Int =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_TYPE_TIME,
                SQL_TIME_TZ,
                SQL_TIME_TZ_EX ->
                    (data.reinterpret<ISC_TIMEVar>().value / 10u).toInt()
                SQL_TIMESTAMP,
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    (data.reinterpret<ISC_TIMESTAMP>().timestamp_time / 10u).toInt()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the value of a time zone from the specified sqlda handle at the given index.
     *
     * @param sqlda The sqlda handle.
     * @param index The index of the value within the sqlda.
     * @return The time zone value.
     *
     * @throws FirebirdException if there is an error with the conversion or if the index is out of bounds.
     */
    actual fun getValueTimeZone(sqlda: HANDLE, index: Int): Int =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_TIME_TZ,
                SQL_TIME_TZ_EX ->
                    data.reinterpret<ISC_TIME_TZ>().time_zone.toInt()
                SQL_TIMESTAMP_TZ,
                SQL_TIMESTAMP_TZ_EX ->
                    data.reinterpret<ISC_TIMESTAMP_TZ>().time_zone.toInt()
                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Retrieves the BLOB ID value of a specified index from the given SQLDA.
     *
     * @param sqlda The SQLDA handle.
     * @param index The index of the BLOB ID field.
     * @return The BLOB ID value as a Long.
     * @throws FirebirdException if the SQL code is not SQL_BLOB.
     */
    actual fun getValueBlobId(sqlda: HANDLE, index: Int): Long =
        getFieldValue(sqlda, index) { data, sqlCode ->
            return@getFieldValue when (sqlCode) {
                SQL_BLOB -> {
                    data.reinterpret<LongVar>().value
                }

                else ->
                    throw FirebirdException("$ERR_CONVERSION ($index)")
            }
        }

    /**
     * Opens a blob for reading or writing.
     *
     * @param status the status array to store error information
     * @param dbHandle the database handle
     * @param trHandle the transaction handle
     * @param blobHandle the blob handle
     * @param blobId the ID of the blob
     * @return the status of the operation
     */
    actual fun blobOpen(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE, blobId: Long): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        val gdsQuad = nativeHeap.alloc<GDS_QUAD>()
        gdsQuad.reinterpret<LongVar>().value = blobId
        val ret = isc_open_blob(statusArray, dbHandlePtr, trHandlePtr, blobHandlePtr, gdsQuad.ptr)
        nativeHeap.free(gdsQuad)
        return ret
    }

    /**
     * Closes the specified blob handle.
     *
     * @param status The status array to store error information.
     * @param blobHandle The handle of the blob to close.
     * @return The status of the operation.
     */
    actual fun blobClose(status: HANDLE, blobHandle: HANDLE): STATUS {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        return isc_close_blob(statusArray, blobHandlePtr)
    }

    /**
     * Reads data from a blob handle into a buffer.
     *
     * @param status The status handle.
     * @param blobHandle The handle of the blob to read from.
     * @param buffer The buffer to read the data into.
     * @param offset The offset in the buffer to start reading from.
     * @param length The maximum number of bytes to read.
     * @return The total number of bytes read.
     */
    actual fun blobRead(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        var arrayLength = buffer.size
        var total = 0
        if (arrayLength > 0 && offset >= 0 && length > 0) {
            arrayLength = min(arrayLength - offset, length)
            buffer.usePinned {
                var p = offset
                val size = nativeHeap.alloc<ISC_USHORTVar>()

                while (arrayLength > 0) {
                    val toRead = min(arrayLength, Short.MAX_VALUE.toInt())
                    var ret = isc_get_segment(statusArray, blobHandlePtr, size.ptr, toRead.toUShort(), it.addressOf(p))
                    var success = ret == 0L || statusArray!![1] == ISC_SEGMENT
                    while (success && size.value == 0.toUShort()) {
                        ret = isc_get_segment(statusArray, blobHandlePtr, size.ptr, toRead.toUShort(), it.addressOf(p))
                        success = ret == 0L || statusArray!![1] == ISC_SEGMENT
                    }
                    val s = size.value.toInt()
                    if (s > 0) {
                        p += s
                        arrayLength -= s
                        total += s
                        size.value = 0.toUShort()
                    } else
                        break
                }
                nativeHeap.free(size)
            }

        }
        return total
    }

    /**
     * Returns the length of the given blob.
     *
     * @param status The handle representing the status.
     * @param blobHandle The handle representing the blob.
     * @return The length of the blob. Returns 0 if an error occurs.
     */
    actual fun blobLength(status: HANDLE, blobHandle: HANDLE): Long {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        memScoped {
            val buffer = allocArray<ByteVar>(9)
            val info = allocArray<ByteVar>(1)
            info[0] = isc_info_blob_total_length.toByte()
            val ret = isc_blob_info(statusArray, blobHandlePtr, 1, info, 9, buffer)
            if (ret == 0L)
                return (buffer + 3)!!.reinterpret<ISC_LONGVar>().pointed.value.toLong()
        }
        throw FirebirdException(interpret(status))
    }

    /**
     * Writes a portion of the given byte array to a blob.
     *
     * @param status the handle to the status object.
     * @param blobHandle the handle to the blob object.
     * @param buffer the byte array to write to the blob.
     * @param offset the starting position in the byte array to write from.
     * @param length the number of bytes to write from the byte array.
     * @return the total number of bytes successfully written to the blob.
     */
    actual fun blobWrite(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        var arrayLength = buffer.size
        var total = 0
        if (arrayLength > 0 && offset >= 0 && length > 0) {
            arrayLength = min(arrayLength - offset, length)
            buffer.usePinned {
                var p = offset
                while (arrayLength > 0) {
                    val toWrite = min(arrayLength, Short.MAX_VALUE.toInt())
                    if (toWrite > 0 && isc_put_segment(statusArray, blobHandlePtr, toWrite.toUShort(), it.addressOf(p)) == 0L) {
                        p += toWrite
                        arrayLength -= toWrite
                        total += toWrite
                    } else
                        break
                }
            }
        }
        return total
    }

    /**
     * Creates a new blob
     *
     * @param status The handle to the status array.
     * @param dbHandle The handle to the database connection.
     * @param trHandle The handle to the transaction.
     * @param blobHandle The handle to the blob.
     * @return The ID of the created blob.
     * @throws FirebirdException If an error occurs while creating the blob.
     */
    actual fun blobCreate(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE): Long {
        val statusArray = status.toCPointer<ISC_STATUSVar>()
        val dbHandlePtr = dbHandle.toCPointer<FB_API_HANDLEVar>()
        val trHandlePtr = trHandle.toCPointer<FB_API_HANDLEVar>()
        val blobHandlePtr = blobHandle.toCPointer<FB_API_HANDLEVar>()
        val blobId = nativeHeap.alloc<GDS_QUAD>()
        val ret = isc_create_blob(statusArray, dbHandlePtr, trHandlePtr, blobHandlePtr, blobId.ptr)

        val id = blobId.reinterpret<LongVar>().value
        nativeHeap.free(blobId)
        if (ret != 0L)
            throw FirebirdException(interpret(status))
        return id
    }
}