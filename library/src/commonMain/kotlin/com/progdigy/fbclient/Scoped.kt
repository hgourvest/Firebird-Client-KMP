package com.progdigy.fbclient

import com.progdigy.fbclient.Attachment.Transaction.Statement

/**
 * A class representing an attachment to a database.
 */
@OptIn(ExperimentalStdlibApi::class)
class Attachment private constructor(val status: HANDLE, val dbHandle: HANDLE): AutoCloseable {
    var dialect: Short = 3

    private var cacheBlobs: Blob? = null
    private var cacheTransactions: Attachment.Transaction? = null
    private var cacheStatements: Attachment.Transaction.Statement? = null
    private var cacheRecord: Attachment.Transaction.Record? = null
    private var cacheRecordSet: Attachment.Transaction.Statement.RecordSet? = null

    /**
     * Closes the attachment by detaching the database.
     */
    override fun close() {
        API.detachDatabase(status, dbHandle)
        API.freeHandle(dbHandle)
        API.freeStatusArray(status)
        clearCache()
    }

    private fun clearCache() {
        clearBlobs()
        clearTransactions()
        clearStatements()
        clearParams()
        clearRecordSet()
    }

    private fun clearBlobs() {
        var cache = cacheBlobs
        while (cache != null) {
            val next = cache.next
            cache.next = null
            cache = next
        }
        cacheBlobs = null
    }

    private fun clearTransactions() {
        var cache = cacheTransactions
        while (cache != null) {
            val next = cache.next
            cache.next = null
            cache = next
        }
        cacheTransactions = null
    }

    private fun clearStatements() {
        var cache = cacheStatements
        while (cache != null) {
            val next = cache.next
            cache.next = null
            cache = next
        }
        cacheStatements = null
    }

    private fun clearParams() {
        var cache = cacheRecord
        while (cache != null) {
            val next = cache.next
            cache.next = null
            cache = next
        }
        cacheRecord = null
    }

    private fun clearRecordSet() {
        var cache = cacheRecordSet
        while (cache != null) {
            val next = cache.next
            cache.next = null
            cache = next
        }
        cacheRecordSet = null
    }

    /**
     * Represents an interface for reading a Blob.
     */
    interface BlobRead {
        /**
         * Retrieves the length of the Blob.
         *
         * @return The length of the Blob as a Long value.
         */
        fun getLength(): Long

        /**
         * Reads data from a buffer into an array of bytes.
         *
         * @param buffer The destination byte array to read data into.
         * @param offset The starting offset within the buffer.
         * @param length The maximum number of bytes to read.
         * @return The total number of bytes read into the buffer.
         */
        fun read(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): Int
    }

    /**
     * This interface represents a writer for binary data to a Blob.
     */
    interface BlobWrite {
        /**
         * Writes the specified buffer to a Blob.
         *
         * @param buffer The byte array containing the data to be written.
         * @param offset The position within the buffer to start writing from. Default is 0.
         * @param length The number of bytes to write from the buffer. Default is the size of the buffer.
         *
         * @return The number of bytes actually written to the Blob.
         */
        fun write(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): Int
    }

    inner class Blob(var blobHandle: HANDLE): BlobRead, BlobWrite {
        var next: Blob? = null
        override fun getLength(): Long =
            API.blobLength(status, blobHandle)

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            API.blobRead(status, blobHandle, buffer, offset, length)

        override fun write(buffer: ByteArray, offset: Int, length: Int): Int =
            API.blobWrite(status, blobHandle, buffer, offset, length)
    }

    fun getBlob(blobHandle: HANDLE): Blob {
        val cache = cacheBlobs
        return if (cache != null) {
            cacheBlobs = cache.next
            cache.next = null
            cache.blobHandle = blobHandle
            cache
        } else
            Blob(blobHandle)
    }

    fun releaseBlob(blob: Blob) {
        blob.blobHandle =0L
        blob.next = cacheBlobs
        cacheBlobs = blob
    }

    inline fun blobScope(blobHandle: HANDLE, block: Blob.() -> Unit) {
        val scope = getBlob(blobHandle)
        scope.block()
        releaseBlob(scope)
    }

    /**
     * This class represents a transaction within an attachment.
     *
     * The Transaction class provides methods for executing SQL statements, managing transaction state, and working with
     * blobs.
     *
     * @see Attachment
     */
    inner class Transaction(var trHandle: HANDLE) {
        internal var next: Transaction? = null

        /**
         * Executes the provided block of code for each record in the SQL statement's result set.
         *
         * @param block The code block to execute for each record in the result set.
         */
        inline fun Statement.forEach(block: SQLDA.() -> Unit) =
            open {
                while (!eof) {
                    block()
                    fetch()
                }
            }

        fun SQLDA.getBooleanOrNull(index: Int): Boolean? = if (getIsNull(index)) null else getBoolean(index)
        fun SQLDA.getShortOrNull(index: Int): Short? = if (getIsNull(index)) null else getShort(index)
        fun SQLDA.getIntOrNull(index: Int): Int? = if (getIsNull(index)) null else getInt(index)
        fun SQLDA.getLongOrNull(index: Int): Long? = if (getIsNull(index)) null else getLong(index)
        fun SQLDA.getInt128OrNull(index: Int): LongArray? = if (getIsNull(index)) null else getInt128(index)
        fun SQLDA.getFloatOrNull(index: Int): Float? = if (getIsNull(index)) null else getFloat(index)
        fun SQLDA.getDoubleOrNull(index: Int): Double? = if (getIsNull(index)) null else getDouble(index)
        fun SQLDA.getStringOrNull(index: Int): String? = if (getIsNull(index)) null else getString(index)
        fun SQLDA.getByteArrayOrNull(index: Int): ByteArray? = if (getIsNull(index)) null else getByteArray(index)
        fun SQLDA.getBlobIdOrNull(index: Int): Long? = if (getIsNull(index)) null else getBlobId(index)

        open inner class SQLDA(var sqlda: HANDLE) {
            /**
             * Retrieves the data type of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The data type of the field.
             */
            fun getType(index: Int): Type = Type.entries[API.getType(sqlda, index)]

            /**
             * Retrieves the scale of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The scale of the field.
             */
            fun getScale(index: Int): Long = API.getScale(sqlda, index)

            /**
             * Retrieves the null status of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return true if the field is null, false otherwise.
             */
            fun getIsNull(index: Int): Boolean = API.getIsNull(sqlda, index)

            /**
             * Retrieves the boolean value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The boolean value of the field.
             */
            fun getBoolean(index: Int): Boolean = API.getValueBoolean(sqlda, index)

            /**
             * Retrieves the Short value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The Short value of the field.
             */
            fun getShort(index: Int): Short = API.getValueShort(sqlda, index)

            /**
             * Retrieves the integer value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The integer value of the field.
             */
            fun getInt(index: Int): Int = API.getValueInt(sqlda, index)

            /**
             * Retrieves the long value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The long value of the field.
             */
            fun getLong(index: Int): Long = API.getValueLong(sqlda, index)

            /**
             * Retrieves the value of a 128-bit signed integer at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The value of the 128-bit signed integer as an [LongArray] object.
             */
            fun getInt128(index: Int): LongArray = API.getValueInt128(sqlda, index)

            /**
             * Retrieves the float value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The float value of the field.
             */
            fun getFloat(index: Int): Float = API.getValueFloat(sqlda, index)

            /**
             * Retrieves the double value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The double value of the field.
             */
            fun getDouble(index: Int): Double = API.getValueDouble(sqlda, index)

            /**
             * Retrieves the value of a string (or blob string) field at the specified index.
             *
             * @param index The index of the field in the SQLDA.
             * @return The value of the field as a string.
             */
            fun getString(index: Int): String = API.getValueString(status, dbHandle, trHandle, sqlda, index)

            /**
             * Retrieves a byte array of the field at the specified index.
             *
             * @param index The index of the field.
             * @return The byte array representation of the field.
             */
            fun getByteArray(index: Int): ByteArray = API.getValueByteArray(status, dbHandle, trHandle, sqlda, index)

            /**
             * Retrieves the value of the field at the specified index in the SQLDA as the number of days since the epoch.
             *
             * @param index The index of the field in the SQLDA.
             * @return The number of days since the epoch.
             */
            fun getEpochDays(index: Int): Int = API.getValueDate(sqlda, index)

            /**
             * Retrieves the milliseconds of the day from the given index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The milliseconds of the day.
             */
            fun getMillisecondOfDay(index: Int): Int = API.getValueTime(sqlda, index)

            /**
             * Retrieves the time zone ID at the specified index in the SQLDA.
             *
             * @param index The index of the time zone ID in the SQLDA.
             * @return The time zone ID as a TimeZoneId object.
             */
            fun getTimeZoneId(index: Int): TimeZoneId = TimeZoneId(API.getValueTimeZone(sqlda, index))

            /**
             * Retrieves the blob ID of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @return The blob ID of the field.
             */
            fun getBlobId(index: Int): Long = API.getValueBlobId(sqlda, index)

            /**
             * Sets the null status of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             */
            fun setIsNull(index: Int) = API.setIsNull(sqlda, index)

            /**
             * Sets the boolean value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The boolean value to set.
             */
            fun setBoolean(index: Int, value: Boolean) = API.setValueBoolean(sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA to a Short value.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The Short value to set.
             */
            fun setShort(index: Int, value: Short) = API.setValueShort(sqlda, index, value)

            /**
             * Sets the integer value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The integer value to set.
             */
            fun setInt(index: Int, value: Int) = API.setValueInt(sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA to a long value.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The long value to set.
             */
            fun setLong(index: Int, value: Long) = API.setValueLong(sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA to a 128-bit signed integer.
             *
             * @param index The index of the field in the SQLDA.
             * @param a The first 64 bits of the 128-bit signed integer.
             * @param b The last 64 bits of the 128-bit signed integer.
             */
            fun setInt128(index: Int, a: Long, b: Long) = API.setValueInt128(sqlda, index, a, b)

            /**
             * Sets the value of the field at the specified index in the SQLDA to a float value.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The float value to set.
             */
            fun setFloat(index: Int, value: Float) = API.setValueFloat(sqlda, index, value)

            /**
             * Sets the double value of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The double value to set.
             */
            fun setDouble(index: Int, value: Double) = API.setValueDouble(sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA with the given string value.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The string value to set.
             */
            fun setString(index: Int, value: String) = API.setValueString(status, dbHandle, trHandle, sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA to the provided byte array value.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The byte array value to set.
             */
            fun setByteArray(index: Int, value: ByteArray) = API.setValueByteArray(status, dbHandle, trHandle, sqlda, index, value)

            /**
             * Sets the value of the field at the specified index in the SQLDA to the provided number of epoch days.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The number of days since the epoch to set.
             */
            fun setEpochDays(index: Int, value: Int) = API.setValueDate(sqlda, index, value)

            /**
             * Sets the milliseconds of the day of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The milliseconds of the day to set.
             */
            fun setMillisecondOfDay(index: Int, value: Int) = API.setValueTime(sqlda, index, value)

            /**
             * Sets the time zone ID of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The time zone ID to set.
             */
            fun setTimeZoneId(index: Int, value: TimeZoneId) = API.setValueTimeZone(sqlda, index, value.id)

            /**
             * Sets the blob ID of the field at the specified index in the SQLDA.
             *
             * @param index The index of the field in the SQLDA.
             * @param value The blob ID to set.
             */
            fun setBlobId(index: Int, value: Long) = API.setValueBlobId(sqlda, index, value)
        }


        inner class Record(sqlda: HANDLE): SQLDA(sqlda) {
            internal var next: Record? = null
        }

        /**
         * A class representing a statement in a transaction.
         */
        inner class Statement(var stHandle: HANDLE, var output: HANDLE) {
            internal var next: Statement? = null

            /**
             * Represents a record set obtained from executing a SQL statement.
             */
            inner class RecordSet(sqlda: HANDLE): SQLDA(sqlda) {
                internal var next: RecordSet? = null
                private var isEof = false
                val eof: Boolean
                    get() = isEof

                /**
                 * Fetches the next record from the record set.
                 */
                fun fetch() {
                    if (!isEof) {
                        when (val ret = API.fetch(status, stHandle, sqlda)) {
                            0L -> {
                                // success
                            }
                            100L -> isEof = true
                            else -> checkStatus(status, ret)
                        }
                    }
                }
            }

            private fun getRecord(sqlda: HANDLE): Record {
                val cache = cacheRecord
                return if (cache != null) {
                    cacheRecord = cache.next
                    cache.next = null
                    cache.sqlda = sqlda
                    cache
                } else
                    Record(sqlda)
            }

            private fun releaseRecord(param: Record) {
                param.sqlda = 0L
                param.next = cacheRecord
                cacheRecord = param
            }

            private var _params: Record? = null
            private var _result: Record? = null

            var input: HANDLE = 0L

            /**
             * Represents parameters used when executing or opening the statement.
             */
            val params: SQLDA
                get() {
                    if (_params == null) {
                        if (input == 0L) {
                            input = API.allocHandle()
                            checkStatus(status, API.prepareParams(status, stHandle, dialect, input))
                        }
                        _params = getRecord(input)
                    }
                    return _params!!
                }

            /**
             * Represents a result of an SQL statement execution.
             */
            val result: SQLDA
                get() {
                    if (_result == null)
                        _result = getRecord(output)
                    return _result!!
                }

            /**
             * Executes the SQL statement.
             */
            fun execute() {
                checkStatus(status, API.execute2(status, trHandle, stHandle, dialect, input, output))
            }

            fun getRecordSet(sqlda: HANDLE): RecordSet {
                val cache = cacheRecordSet
                return if (cache != null) {
                    cacheRecordSet = cache.next
                    cache.next = null
                    cache.sqlda = sqlda
                    cache
                } else
                    RecordSet(sqlda)
            }

            fun releaseRecordSet(set: RecordSet) {
                set.sqlda = 0L
                set.next = cacheRecordSet
                cacheRecordSet = set
            }

            /**
             * Opens a statement set and executes the provided block of code within the record set's scope.
             *
             * @param block The code block to execute within the record set's scope.
             */
            inline fun open(block: RecordSet.() -> Unit) {
                checkStatus(status, API.execute(status, trHandle, stHandle, dialect, input))
                val scope = getRecordSet(output)
                try {
                    scope.fetch()
                    scope.block()
                } finally {
                    checkStatus(status, API.freeStatement(status, stHandle, DSQL_close))
                }
                releaseRecordSet(scope)
            }

            /**
             * Closes the statement and frees any associated resources.
             *
             * This method releases the statement handle and frees the SQLDA objects associated with the statement.
             * It also releases any cached parameter and result records.
             *
             * Note: After calling this method, you can no longer use the statement.
             */
            fun close() {
                API.freeStatement(status, stHandle, DSQL_drop)
                stHandle = 0L

                if (input != 0L) {
                    API.freeSQLDA(input)
                    input = 0L
                }

                API.freeSQLDA(output)
                output = 0L

                var p = _params
                if (p != null) {
                    releaseRecord(p)
                    _params = null
                }

                p = _result
                if (p != null) {
                    releaseRecord(p)
                    _params = null
                }
            }
        }

        /**
         * Commits the current transaction and releases the transaction handle.
         *
         * You can't continue to use this transaction afterward.
         *
         * @throws FirebirdException if the commit operation fails.
         */
        fun commit() {
            if (trHandle != 0L) {
                checkStatus(status, API.commitTransaction(status, trHandle, false))
                API.freeHandle(trHandle)
                trHandle = 0L
            }
        }

        /**
         * Commits the current transaction and retains the transaction handle.
         *
         * You can continue to use this transaction afterward.
         *
         * @throws FirebirdException if the commit operation fails.
         */
        fun commitRetaining() {
            if (trHandle != 0L)
                checkStatus(status, API.commitTransaction(status, trHandle, true))
        }

        /**
         * Rolls back the current transaction and releases the transaction handle.
         *
         * You can't continue to use this transaction afterward.
         *
         * @throws FirebirdException if the rollback operation fails
         */
        fun rollback() {
            if (trHandle != 0L) {
                checkStatus(status, API.rollbackTransaction(status, trHandle, false))
                API.freeHandle(trHandle)
                trHandle = 0L
            }
        }

        /**
         * Rolls back the current transaction and retains the transaction handle.
         *
         * You can continue to use this transaction afterward.
         *
         * @throws FirebirdException if the rollback operation fails
         */
        fun rollbackRetaining() {
            if (trHandle != 0L)
                checkStatus(status, API.rollbackTransaction(status, trHandle, true))
        }

        /**
         * Creates a blob and executes the provided block of code within the blob's scope.
         *
         * @param id The ID of the blob to open.
         * @param block The code block to execute within the blob's scope.
         */
        inline fun blobOpen(id: Long, block: BlobRead.() -> Unit) {
            val blobHandle = API.allocHandle()
            try {
                checkStatus(status, API.blobOpen(status, dbHandle, trHandle, blobHandle, id))
                try {
                    blobScope(blobHandle, block)
                } finally {
                    checkStatus(status, API.blobClose(status, blobHandle))
                }
            } finally {
                API.freeHandle(blobHandle)
            }
        }

        /**
         * Creates a blob and executes the provided block of code within the blob's scope.
         *
         * @param block The code block to execute within the blob's scope.
         * @return The ID of the created blob.
         */
        inline fun blobCreate(block: BlobWrite.() -> Unit): Long {
            val blobHandle = API.allocHandle()
            try {
                val blobId = API.blobCreate(status, dbHandle, trHandle, blobHandle)
                try {
                    blobScope(blobHandle, block)
                    return blobId
                } finally {
                    checkStatus(status, API.blobClose(status, blobHandle))
                }
            } finally {
                API.freeHandle(blobHandle)
            }
        }

        /**
         * Executes an SQL statement.
         *
         * @param sql The SQL statement to execute.
         * @return true if the execution is successful, false otherwise.
         */
        fun execute(sql: String) =
            checkStatus(status, API.executeImmediate(status, dbHandle, trHandle, sql, dialect))

        fun getStatement(stHandle: HANDLE, output: HANDLE): Statement {
            val cache = cacheStatements
            return if (cache != null) {
                cacheStatements = cache.next
                cache.next = null
                cache.stHandle = stHandle
                cache.output = output
                cache
            } else
                Statement(stHandle, output)
        }

        fun releaseStatement(statement: Statement) {
            statement.stHandle = 0L
            statement.next = cacheStatements
            cacheStatements = statement
        }

        /**
         * Executes a SQL statement within a transaction block.
         *
         * @param sql The SQL statement to execute.
         * @param cursor The cursor name. Defaults to null.
         * @param block The code block to execute within the statement's scope.
         */
        inline fun statement(sql: String, cursor: String? = null, block: Statement.() -> Unit) {
            val stHandle = API.allocHandle()
            val output = API.allocHandle()
            try {
                checkStatus(status, API.prepareStatement(status, dbHandle, trHandle, stHandle, sql, cursor, dialect, output))
                val scope = getStatement(stHandle, output)
                try {
                    scope.block()
                } finally {
                    scope.close()
                }
                releaseStatement(scope)
            } finally {
                API.freeHandle(stHandle)
                API.freeHandle(output)
            }
        }
    }

    fun getTransaction(trHandle: HANDLE): Attachment.Transaction {
        val cache = cacheTransactions
        return if (cache != null) {
            cacheTransactions = cache.next
            cache.next = null
            cache.trHandle = trHandle
            cache
        } else
            Transaction(trHandle)
    }

    fun releaseTransaction(transaction: Transaction) {
            transaction.trHandle = 0L
        transaction.next = cacheTransactions
        cacheTransactions = transaction
    }

    /**
     * Executes the given transaction block within a transaction.
     *
     * This method encapsulates the execution logic of the transaction block within a transaction. It handles the start,
     * commit, and rollback phases of the transaction. If an exception occurs during the execution of the block,
     * the transaction will be rolled back.
     *
     * @param tpb The transaction parameter buffer (TPB) as a byte array. It is used to specify transaction settings.
     *             Defaults to null, which indicates default transaction settings.
     * @param block The block of code to execute within the transaction.
     *
     * @see Transaction
     */
    inline fun transaction(tpb: ByteArray? = null, block: Transaction.() -> Unit) {
        val trHandle = API.allocHandle()
        checkStatus(status, API.startTransaction(status, trHandle, dbHandle, tpb))
        val scope = getTransaction(trHandle)
        try {
            try {
                scope.block()
            } catch (e: Exception) {
                scope.rollback()
                throw e
            }
        } finally {
            scope.commit()
        }
        releaseTransaction(scope)
    }

    /**
     * Executes the given SQL statement within a transaction.
     *
     * This method executes the provided SQL statement within a transaction. It encapsulates the execution logic within
     * a transaction block, handling the start, commit, and rollback phases of the transaction.
     *
     * @param sql The SQL statement to execute.
     * @param cursor The cursor name, if any.
     * @param block The block of code to execute within the statement.
     *
     * @see transaction
     * @see attachment
     * @see Blob
     */
    inline fun statement(sql: String, cursor: String? = null, block: Statement.() -> Unit) {
        transaction {
            statement(sql, cursor, block)
        }
    }

    /**
     * Executes the given SQL statement within a transaction.
     *
     * This method executes the provided SQL statement within a transaction. It encapsulates the execution
     * logic within a transaction block, handling the start, commit, and rollback phases of the transaction.
     *
     * @param sql The SQL statement to execute.
     *
     * @see transaction
     * @see attachment
     * @see Blob
     */
    fun execute(sql: String) {
        transaction { execute(sql) }
    }

    companion object {
        /**
         * Attaches a database using the specified file name and database parameter block (DPB).
         * If no DPB is provided, a default DPB will be created using the `makeDPB` function.
         * Returns an `Attachment` object representing the attached database.
         *
         * @param fileName The path to the database file.
         * @param dpb The database parameter block (DPB) used for attaching the database.
         *            Optional, defaults to the result of calling `makeDPB {}`.
         * @return An `Attachment` object representing the attached database.
         * @throws Throwable if an error occurs during the attachment process.
         */
        fun attachDatabase(fileName: String, dpb: ByteArray = makeDPB {}): Attachment {
            val status = API.allocStatusArray()
            val dbHandle = API.allocHandle()
            try {
                checkStatus(status, API.attachDatabase(status, fileName, dbHandle, dpb))
                return Attachment(status, dbHandle)
            } catch (e: Throwable) {
                API.freeHandle(dbHandle)
                API.freeStatusArray(status)
                throw e
            }
        }

        /**
         * Creates a new database with the specified file name and optional database parameter block (DPB).
         *
         * @param fileName The path and name of the database file.
         * @param dpb The optional database parameter block represented as a byte array. Default value is generated using the `makeDPB` function.
         * @return An instance of the `Attachment` class representing the newly created database.
         * @throws Throwable if an error occurs during the creation of the database.
         */
        fun createDatabase(fileName: String, dpb: ByteArray = makeDPB {}): Attachment {
            val status = API.allocStatusArray()
            val dbHandle = API.allocHandle()
            try {
                checkStatus(status, API.createDatabase(status, fileName, dbHandle, dpb))
                return Attachment(status, dbHandle)
            } catch (e: Throwable) {
                API.freeHandle(dbHandle)
                API.freeStatusArray(status)
                throw e
            }
        }
    }
}