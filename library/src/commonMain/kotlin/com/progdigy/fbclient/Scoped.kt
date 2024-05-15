package com.progdigy.fbclient

import com.progdigy.fbclient.Attachment.Transaction.Statement

@OptIn(ExperimentalStdlibApi::class)
class Attachment private constructor(val status: HANDLE, val dbHandle: HANDLE): AutoCloseable {
    var dialect: Short = 3

    private var cacheBlobs: Blob? = null
    private var cacheTransactions: Attachment.Transaction? = null
    private var cacheStatements: Attachment.Transaction.Statement? = null
    private var cacheRecord: Attachment.Transaction.Record? = null
    private var cacheRecordSet: Attachment.Transaction.Statement.RecordSet? = null

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

    interface BlobRead {
        fun getLength(): Long
        fun read(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): Int
    }

    interface BlobWrite {
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

    inner class Transaction(var trHandle: HANDLE) {
        internal var next: Transaction? = null

        fun SQLDA.getBooleanOrNull(index: Int): Boolean? = if (getIsNull(index)) null else getBoolean(index)
        fun SQLDA.getShortOrNull(index: Int): Short? = if (getIsNull(index)) null else getShort(index)
        fun SQLDA.getIntOrNull(index: Int): Int? = if (getIsNull(index)) null else getInt(index)
        fun SQLDA.getLongOrNull(index: Int): Long? = if (getIsNull(index)) null else getLong(index)
        fun SQLDA.getInt128OrNull(index: Int): Int128? = if (getIsNull(index)) null else getInt128(index)
        fun SQLDA.getFloatOrNull(index: Int): Float? = if (getIsNull(index)) null else getFloat(index)
        fun SQLDA.getDoubleOrNull(index: Int): Double? = if (getIsNull(index)) null else getDouble(index)
        fun SQLDA.getStringOrNull(index: Int): String? = if (getIsNull(index)) null else getString(index)
        fun SQLDA.getByteArrayOrNull(index: Int): ByteArray? = if (getIsNull(index)) null else getByteArray(index)
        fun SQLDA.getBlobIdOrNull(index: Int): Long? = if (getIsNull(index)) null else getBlobId(index)

        inline fun Statement.forEach(block: SQLDA.() -> Unit) =
            open {
                while (!eof) {
                    block()
                    fetch()
                }
            }

        open inner class SQLDA(var sqlda: HANDLE) {
            fun getType(index: Int): Type = Type.entries[API.getType(sqlda, index)]
            fun getScale(index: Int): Long = API.getScale(sqlda, index)

            fun getIsNull(index: Int): Boolean = API.getIsNull(sqlda, index)
            fun getBoolean(index: Int): Boolean = API.getValueBoolean(sqlda, index)
            fun getShort(index: Int): Short = API.getValueShort(sqlda, index)
            fun getInt(index: Int): Int = API.getValueInt(sqlda, index)
            fun getLong(index: Int): Long = API.getValueLong(sqlda, index)
            fun getInt128(index: Int): Int128 {
                val arr = API.getValueInt128(sqlda, index)
                return Int128(arr[0], arr[1])
            }
            fun getFloat(index: Int): Float = API.getValueFloat(sqlda, index)
            fun getDouble(index: Int): Double = API.getValueDouble(sqlda, index)
            fun getString(index: Int): String = API.getValueString(status, dbHandle, trHandle, sqlda, index)
            fun getByteArray(index: Int): ByteArray = API.getValueByteArray(status, dbHandle, trHandle, sqlda, index)
            fun getEpochDays(index: Int): Int = API.getValueDate(sqlda, index)
            fun getMillisecondOfDay(index: Int): Int = API.getValueTime(sqlda, index)
            fun getTimeZoneId(index: Int): TimeZoneId = TimeZoneId(API.getValueTimeZone(sqlda, index))
            fun getBlobId(index: Int): Long = API.getValueBlobId(sqlda, index)

            fun setIsNull(index: Int) = API.setIsNull(sqlda, index)
            fun setBoolean(index: Int, value: Boolean) = API.setValueBoolean(sqlda, index, value)
            fun setShort(index: Int, value: Short) = API.setValueShort(sqlda, index, value)
            fun setInt(index: Int, value: Int) = API.setValueInt(sqlda, index, value)
            fun setLong(index: Int, value: Long) = API.setValueLong(sqlda, index, value)
            fun setInt128(index: Int, value: Int128) = API.setValueInt128(sqlda, index, value.a, value.b)
            fun setInt128(index: Int, a: Long, b: Long) = API.setValueInt128(sqlda, index, a, b)
            fun setFloat(index: Int, value: Float) = API.setValueFloat(sqlda, index, value)
            fun setDouble(index: Int, value: Double) = API.setValueDouble(sqlda, index, value)
            fun setString(index: Int, value: String) = API.setValueString(status, dbHandle, trHandle, sqlda, index, value)
            fun setByteArray(index: Int, value: ByteArray) = API.setValueByteArray(status, dbHandle, trHandle, sqlda, index, value)
            fun setEpochDays(index: Int, value: Int) = API.setValueDate(sqlda, index, value)
            fun setMillisecondOfDay(index: Int, value: Int) = API.setValueTime(sqlda, index, value)
            fun setTimeZoneId(index: Int, value: TimeZoneId) = API.setValueTimeZone(sqlda, index, value.id)
            fun setBlobId(index: Int, value: Long) = API.setValueBlobId(sqlda, index, value)
        }

        inner class Record(sqlda: HANDLE): SQLDA(sqlda) {
            internal var next: Record? = null
        }

        inner class Statement(var stHandle: HANDLE, var output: HANDLE) {
            internal var next: Statement? = null
            inner class RecordSet(sqlda: HANDLE): SQLDA(sqlda) {
                internal var next: RecordSet? = null
                private var isEof = false
                val eof: Boolean
                    get() = isEof

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

            val result: SQLDA
                get() {
                    if (_result == null)
                        _result = getRecord(output)
                    return _result!!
                }

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

        fun commit() {
            if (trHandle != 0L) {
                checkStatus(status, API.commitTransaction(status, trHandle, false))
                API.freeHandle(trHandle)
                trHandle = 0L
            }
        }

        fun commitRetaining() {
            if (trHandle != 0L)
                checkStatus(status, API.commitTransaction(status, trHandle, true))
        }

        fun rollback() {
            if (trHandle != 0L) {
                checkStatus(status, API.rollbackTransaction(status, trHandle, false))
                API.freeHandle(trHandle)
                trHandle = 0L
            }
        }

        fun rollbackRetaining() {
            if (trHandle != 0L)
                checkStatus(status, API.rollbackTransaction(status, trHandle, true))
        }

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

    inline fun statement(sql: String, cursor: String? = null, block: Statement.() -> Unit) {
        transaction {
            statement(sql, cursor, block)
        }
    }

    fun execute(sql: String) {
        transaction { execute(sql) }
    }

    companion object {
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