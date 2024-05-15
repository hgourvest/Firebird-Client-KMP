package com.progdigy.fbclient

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object API {
    init {
        NativeUtils.loadLibrary("jnifbclient")
    }

    @JvmStatic
    actual external fun allocHandle(): HANDLE
    @JvmStatic
    actual external fun allocStatusArray(): HANDLE
    @JvmStatic
    actual external fun freeHandle(handle: HANDLE)
    @JvmStatic
    actual external fun freeStatusArray(status: HANDLE)
    @JvmStatic
    actual external fun freeSQLDA(handle: HANDLE)
    @JvmStatic
    actual external fun interpret(status: HANDLE): String
    @JvmStatic
    actual external fun attachDatabase(status: HANDLE, path: String, dbHandle: HANDLE, options: ByteArray?): STATUS
    @JvmStatic
    actual external fun createDatabase(status: HANDLE, path: String, dbHandle: HANDLE, options: ByteArray?): STATUS
    @JvmStatic
    actual external fun detachDatabase(status: HANDLE, dbHandle: HANDLE): STATUS
    @JvmStatic
    actual external fun executeImmediate(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sql: String, dialect: Short): STATUS
    @JvmStatic
    actual external fun startTransaction(status: HANDLE, trHandle: HANDLE, dbHandle: HANDLE, options: ByteArray?): STATUS
    @JvmStatic
    actual external fun commitTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS
    @JvmStatic
    actual external fun rollbackTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS
    @JvmStatic
    actual external fun prepareStatement(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, stHandle: HANDLE, sql: String,
                                  cursor: String?, dialect: Short, sqlda: HANDLE): STATUS
    @JvmStatic
    actual external fun freeStatement(status: HANDLE, stHandle: HANDLE, action: Short): STATUS
    @JvmStatic
    actual external fun prepareParams(status: HANDLE, stHandle: HANDLE, dialect: Short, sqlda: HANDLE): STATUS
    @JvmStatic
    actual external fun setIsNull(sqlda: HANDLE, index: Int)
    @JvmStatic
    actual external fun setValueBoolean(sqlda: HANDLE, index: Int, value: Boolean)
    @JvmStatic
    actual external fun setValueShort(sqlda: HANDLE, index: Int, value: Short)
    @JvmStatic
    actual external fun setValueInt(sqlda: HANDLE, index: Int, value: Int)
    @JvmStatic
    actual external fun setValueLong(sqlda: HANDLE, index: Int, value: Long)
    @JvmStatic
    actual external fun setValueInt128(sqlda: HANDLE, index: Int, a: Long, b: Long)
    @JvmStatic
    actual external fun setValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: String)
    @JvmStatic
    actual external fun setValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: ByteArray)
    @JvmStatic
    actual external fun setValueFloat(sqlda: HANDLE, index: Int, value: Float)
    @JvmStatic
    actual external fun setValueDouble(sqlda: HANDLE, index: Int, value: Double)
    @JvmStatic
    actual external fun setValueDate(sqlda: HANDLE, index: Int, value: Int)
    @JvmStatic
    actual external fun setValueTime(sqlda: HANDLE, index: Int, value: Int)
    @JvmStatic
    actual external fun setValueTimeZone(sqlda: HANDLE, index: Int, value: Int)
    @JvmStatic
    actual external fun getValueDate(sqlda: HANDLE, index: Int): Int
    @JvmStatic
    actual external fun getValueTime(sqlda: HANDLE, index: Int): Int
    @JvmStatic
    actual external fun getValueTimeZone(sqlda: HANDLE, index: Int): Int
    @JvmStatic
    actual external fun getType(sqlda: HANDLE, index: Int): Int
    @JvmStatic
    actual external fun getIsNull(sqlda: HANDLE, index: Int): Boolean
    @JvmStatic
    actual external fun getScale(sqlda: HANDLE, index: Int): Long
    @JvmStatic
    actual external fun getValueBoolean(sqlda: HANDLE, index: Int): Boolean
    @JvmStatic
    actual external fun getValueShort(sqlda: HANDLE, index: Int): Short
    @JvmStatic
    actual external fun getValueInt(sqlda: HANDLE, index: Int): Int
    @JvmStatic
    actual external fun getValueLong(sqlda: HANDLE, index: Int): Long
    @JvmStatic
    actual external fun getValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): String
    @JvmStatic
    actual external fun getValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): ByteArray
    @JvmStatic
    actual external fun getValueFloat(sqlda: HANDLE, index: Int): Float
    @JvmStatic
    actual external fun getValueDouble(sqlda: HANDLE, index: Int): Double
    @JvmStatic
    actual external fun getValueInt128(sqlda: HANDLE, index: Int): LongArray
    @JvmStatic
    actual external fun execute(status: HANDLE, trHandle: HANDLE, stHandle: HANDLE, dialect: Short, sqlda: HANDLE): STATUS
    @JvmStatic
    actual external fun execute2(status: HANDLE, trHandle: HANDLE, stHandle: HANDLE, dialect: Short, input: HANDLE, output: HANDLE): STATUS
    @JvmStatic
    actual external fun fetch(status: HANDLE, stHandle: HANDLE, sqlda: HANDLE): STATUS
    @JvmStatic
    actual external fun blobOpen(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE, blobId: Long): STATUS
    @JvmStatic
    actual external fun blobClose(status: HANDLE, blobHandle: HANDLE): STATUS
    @JvmStatic
    actual external fun setValueBlobId(sqlda: HANDLE, index: Int, value: Long)
    @JvmStatic
    actual external fun getValueBlobId(sqlda: HANDLE, index: Int): Long
    @JvmStatic
    actual external fun blobRead(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int
    @JvmStatic
    actual external fun blobLength(status: HANDLE, blobHandle: HANDLE): Long
    @JvmStatic
    actual external fun blobWrite(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int
    @JvmStatic
    actual external fun blobCreate(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE): Long
}


