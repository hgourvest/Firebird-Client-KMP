package com.progdigy.fbclient

typealias HANDLE = Long
typealias STATUS = Long

const val ISC_MASK   = 0x14000000 // Defines the code as a valid ISC code
const val FAC_MASK   = 0x00FF0000 // Specifies the facility where the code is located
const val CODE_MASK  = 0x0000FFFF // Specifies the code in the message file
const val CLASS_MASK = 0xF0000000 // Defines the code as warning, error, info, or other

const val CLASS_ERROR   = 0 // Code represents an error
const val CLASS_WARNING = 1 // Code represents a warning
const val CLASS_INFO    = 2 // Code represents an information msg

const val DSQL_close : Short = 1
const val DSQL_drop	 : Short = 2

/**
 * Represents the data types for SQLDA fields.
 */
enum class Type {
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    BYTEARRAY,
    INT128,
    BOOLEAN,
    DATE,
    TIME,
    DATETIME,
    TIME_TZ,
    DATETIME_TZ,
    BLOB_BINARY,
    BLOB_TEXT
}

const val isc_arith_except         = 335544321L
const val isc_bad_dbkey            = 335544322L
const val isc_bad_db_format        = 335544323L
const val isc_bad_db_handle        = 335544324L
const val isc_bad_dpb_content      = 335544325L
const val isc_bad_dpb_form         = 335544326L
const val isc_bad_req_handle       = 335544327L
const val isc_bad_segstr_handle    = 335544328L
const val isc_bad_segstr_id        = 335544329L
const val isc_bad_tpb_content      = 335544330L
const val isc_bad_tpb_form         = 335544331L
const val isc_bad_trans_handle     = 335544332L
const val isc_bug_check            = 335544333L
const val isc_convert_error        = 335544334L
const val isc_db_corrupt           = 335544335L
const val isc_deadlock             = 335544336L
const val isc_excess_trans         = 335544337L
const val isc_from_no_match        = 335544338L
const val isc_infinap              = 335544339L
const val isc_infona               = 335544340L
const val isc_infunk               = 335544341L
const val isc_integ_fail           = 335544342L
const val isc_invalid_blr          = 335544343L
const val isc_io_error             = 335544344L
const val isc_lock_conflict        = 335544345L
const val isc_metadata_corrupt     = 335544346L
const val isc_not_valid            = 335544347L
const val isc_no_cur_rec           = 335544348L
const val isc_no_dup               = 335544349L
const val isc_no_finish            = 335544350L
const val isc_no_meta_update       = 335544351L
const val isc_no_priv              = 335544352L
const val isc_no_recon             = 335544353L
const val isc_no_record            = 335544354L
const val isc_no_segstr_close      = 335544355L
const val isc_obsolete_metadata    = 335544356L
const val isc_open_trans           = 335544357L
const val isc_port_len             = 335544358L
const val isc_read_only_field      = 335544359L
const val isc_read_only_rel        = 335544360L
const val isc_read_only_trans      = 335544361L
const val isc_read_only_view       = 335544362L
const val isc_req_no_trans         = 335544363L
const val isc_req_sync             = 335544364L
const val isc_req_wrong_db         = 335544365L
const val isc_segment              = 335544366L
const val isc_segstr_eof           = 335544367L
const val isc_segstr_no_op         = 335544368L
const val isc_segstr_no_read       = 335544369L
const val isc_segstr_no_trans      = 335544370L
const val isc_segstr_no_write      = 335544371L
const val isc_segstr_wrong_db      = 335544372L
const val isc_sys_request          = 335544373L
const val isc_unavailable          = 335544375L
const val isc_unres_rel            = 335544376L
const val isc_uns_ext              = 335544377L
const val isc_wish_list            = 335544378L
const val isc_wrong_ods            = 335544379L
const val isc_wronumarg            = 335544380L
const val isc_imp_exc              = 335544381L
const val isc_random               = 335544382L
const val isc_fatal_conflict       = 335544383L

/**
 * Checks the status of a Firebird operation.
 *
 * @param statusArray The handle to the status array.
 * @param status The status value to check.
 * @return True if the status is successful, false otherwise.
 * @throws FirebirdException if the status indicates an error.
 */
fun checkStatus(statusArray: HANDLE, status: STATUS): Boolean =
    if (status != 0L) {
        if (((status and CLASS_MASK) shr 30).toInt() == CLASS_ERROR) {
            throw FirebirdException(status, API.interpret(statusArray))
        } else {
            false
        }
    } else
        true

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object API {
    fun allocHandle(): HANDLE
    fun allocStatusArray(): HANDLE
    fun freeHandle(handle: HANDLE)
    fun freeStatusArray(status: HANDLE)
    fun freeSQLDA(handle: HANDLE)
    fun interpret(status: HANDLE): String
    fun attachDatabase(status: HANDLE, path: String, dbHandle: HANDLE, options: ByteArray?): STATUS
    fun createDatabase(status: HANDLE, path: String, dbHandle: HANDLE, options: ByteArray?): STATUS
    fun detachDatabase(status: HANDLE, dbHandle: HANDLE): STATUS
    fun executeImmediate(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sql: String, dialect: Short): STATUS
    fun startTransaction(status: HANDLE, trHandle: HANDLE, dbHandle: HANDLE, options: ByteArray?): STATUS
    fun commitTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS
    fun rollbackTransaction(status: HANDLE, trHandle: HANDLE, retain: Boolean): STATUS
    fun prepareStatement(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, stHandle: HANDLE, sql: String,
                                  cursor: String?, dialect: Short, sqlda: HANDLE): STATUS
    fun freeStatement(status: HANDLE, stHandle: HANDLE, action: Short): STATUS
    fun prepareParams(status: HANDLE, stHandle: HANDLE, dialect: Short, sqlda: HANDLE): STATUS
    fun execute(status: HANDLE, trHandle: HANDLE, stHandle: HANDLE, dialect: Short, sqlda: HANDLE): STATUS
    fun execute2(status: HANDLE, trHandle: HANDLE, stHandle: HANDLE, dialect: Short, input: HANDLE, output: HANDLE): STATUS
    fun fetch(status: HANDLE, stHandle: HANDLE, sqlda: HANDLE): STATUS

    fun getType(sqlda: HANDLE, index: Int): Int

    fun getIsNull(sqlda: HANDLE, index: Int): Boolean
    fun getScale(sqlda: HANDLE, index: Int): Long
    fun getValueBoolean(sqlda: HANDLE, index: Int): Boolean
    fun getValueShort(sqlda: HANDLE, index: Int): Short
    fun getValueInt(sqlda: HANDLE, index: Int): Int
    fun getValueLong(sqlda: HANDLE, index: Int): Long
    fun getValueInt128(sqlda: HANDLE, index: Int): LongArray
    fun getValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): String
    fun getValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int): ByteArray
    fun getValueFloat(sqlda: HANDLE, index: Int): Float
    fun getValueDouble(sqlda: HANDLE, index: Int): Double
    fun getValueDate(sqlda: HANDLE, index: Int): Int
    fun getValueTime(sqlda: HANDLE, index: Int): Int
    fun getValueTimeZone(sqlda: HANDLE, index: Int): Int
    fun getValueBlobId(sqlda: HANDLE, index: Int): Long

    fun setIsNull(sqlda: HANDLE, index: Int)
    fun setValueBoolean(sqlda: HANDLE, index: Int, value: Boolean)
    fun setValueShort(sqlda: HANDLE, index: Int, value: Short)
    fun setValueInt(sqlda: HANDLE, index: Int, value: Int)
    fun setValueLong(sqlda: HANDLE, index: Int, value: Long)
    fun setValueInt128(sqlda: HANDLE, index: Int, a: Long, b: Long)
    fun setValueString(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: String)
    fun setValueByteArray(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, sqlda: HANDLE, index: Int, value: ByteArray)
    fun setValueFloat(sqlda: HANDLE, index: Int, value: Float)
    fun setValueDouble(sqlda: HANDLE, index: Int, value: Double)
    fun setValueDate(sqlda: HANDLE, index: Int, value: Int)
    fun setValueTime(sqlda: HANDLE, index: Int, value: Int)
    fun setValueTimeZone(sqlda: HANDLE, index: Int, value: Int)
    fun setValueBlobId(sqlda: HANDLE, index: Int, value: Long)

    fun blobOpen(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE, blobId: Long): STATUS
    fun blobClose(status: HANDLE, blobHandle: HANDLE): STATUS
    fun blobRead(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int
    fun blobLength(status: HANDLE, blobHandle: HANDLE): Long
    fun blobWrite(status: HANDLE, blobHandle: HANDLE, buffer: ByteArray, offset: Int, length: Int): Int
    fun blobCreate(status: HANDLE, dbHandle: HANDLE, trHandle: HANDLE, blobHandle: HANDLE): Long
}