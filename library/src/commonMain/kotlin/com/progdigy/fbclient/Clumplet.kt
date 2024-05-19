package com.progdigy.fbclient

internal const val isc_dpb_version1: Byte = 1

internal const val isc_dpb_page_size: Byte = 4
internal const val isc_dpb_num_buffers: Byte = 5
internal const val isc_dpb_debug: Byte = 7
internal const val isc_dpb_verify: Byte = 9
internal const val isc_dpb_sweep: Byte = 10
internal const val isc_dpb_enable_journal: Byte = 11
internal const val isc_dpb_dbkey_scope: Byte = 13
internal const val isc_dpb_trace: Byte = 15
internal const val isc_dpb_no_garbage_collect: Byte = 16
internal const val isc_dpb_damaged: Byte = 17
internal const val isc_dpb_activate_shadow: Byte = 21
internal const val isc_dpb_sweep_interval: Byte = 22
internal const val isc_dpb_delete_shadow: Byte = 23
internal const val isc_dpb_force_write: Byte = 24
internal const val isc_dpb_no_reserve: Byte = 27
internal const val isc_dpb_user_name: Byte = 28
internal const val isc_dpb_password: Byte = 29
internal const val isc_dpb_interp: Byte = 32
internal const val isc_dpb_drop_walfile: Byte = 40
internal const val isc_dpb_lc_ctype: Byte = 48
internal const val isc_dpb_shutdown: Byte = 50
internal const val isc_dpb_online: Byte = 51
internal const val isc_dpb_shutdown_delay: Byte = 52
internal const val isc_dpb_reserved: Byte = 53
internal const val isc_dpb_overwrite: Byte = 54
internal const val isc_dpb_sec_attach: Byte = 55
internal const val isc_dpb_disable_wal: Byte = 56
internal const val isc_dpb_connect_timeout: Byte = 57
internal const val isc_dpb_dummy_packet_interval: Byte = 58
internal const val isc_dpb_gbak_attach: Byte = 59
internal const val isc_dpb_sql_role_name: Byte = 60
internal const val isc_dpb_set_page_buffers: Byte = 61
internal const val isc_dpb_working_directory: Byte = 62
internal const val isc_dpb_sql_dialect: Byte = 63
internal const val isc_dpb_set_db_readonly: Byte = 64
internal const val isc_dpb_set_db_sql_dialect: Byte = 65
internal const val isc_dpb_gfix_attach: Byte = 66
internal const val isc_dpb_gstat_attach: Byte = 67
internal const val isc_dpb_set_db_charset: Byte = 68
internal const val isc_dpb_address_path: Byte = 70
internal const val isc_dpb_process_id: Byte = 71
internal const val isc_dpb_no_db_triggers: Byte = 72
internal const val isc_dpb_trusted_auth: Byte = 73
internal const val isc_dpb_process_name: Byte = 74
internal const val isc_dpb_org_filename: Byte = 76
internal const val isc_dpb_utf8_filename: Byte = 77
internal const val isc_dpb_ext_call_depth: Byte = 78
internal const val isc_dpb_auth_block: Byte = 79
internal const val isc_dpb_client_version: Byte = 80
internal const val isc_dpb_remote_protocol: Byte = 81
internal const val isc_dpb_host_name: Byte = 82
internal const val isc_dpb_os_user: Byte = 83
internal const val isc_dpb_config: Byte = 87
internal const val isc_dpb_nolinger: Byte = 88
internal const val isc_dpb_reset_icu: Byte = 89
internal const val isc_dpb_map_attach: Byte = 90
internal const val isc_dpb_session_time_zone: Byte = 91
internal const val isc_dpb_set_db_replica: Byte = 92
internal const val isc_dpb_set_bind: Byte = 93
internal const val isc_dpb_decfloat_round: Byte = 94
internal const val isc_dpb_decfloat_traps: Byte = 95
internal const val isc_dpb_clear_map: Byte = 96
internal const val isc_dpb_upgrade_db: Byte = 97
internal const val isc_dpb_parallel_workers: Byte = 100
internal const val isc_dpb_worker_attach: Byte = 101

/*
 * clumplet tags used inside isc_dpb_address_path
 *						 and isc_spb_address_path
 */

const val isc_dpb_address = 1

const val isc_dpb_addr_protocol = 1
const val isc_dpb_addr_endpoint = 2
const val isc_dpb_addr_flags = 3
const val isc_dpb_addr_crypt = 4

// possible addr flags

const val isc_dpb_addr_flag_conn_compressed = 0x01
const val isc_dpb_addr_flag_conn_encrypted = 0x02

/*
 * isc_dpb_verify specific flags
 */

const val isc_dpb_pages = 1
const val isc_dpb_records = 2
const val isc_dpb_indices = 4
const val isc_dpb_transactions = 8
const val isc_dpb_no_update = 16
const val isc_dpb_repair = 32
const val isc_dpb_ignore = 64

/*
 * isc_dpb_shutdown specific flags
 */

const val isc_dpb_shut_cache = 0x1
const val isc_dpb_shut_attachment = 0x2u
const val isc_dpb_shut_transaction = 0x4u
const val isc_dpb_shut_force = 0x8u
const val isc_dpb_shut_mode_mask = 0x70u
const val isc_dpb_shut_default = 0x0u
const val isc_dpb_shut_normal = 0x10u
const val isc_dpb_shut_multi = 0x20u
const val isc_dpb_shut_single = 0x30u
const val isc_dpb_shut_full = 0x40u

/*
 * isc_dpb_set_db_replica specific flags
 */

const val isc_dpb_replica_none = 0
const val isc_dpb_replica_read_only = 1
const val isc_dpb_replica_read_write = 2


internal const val isc_tpb_version1: Byte = 1

internal const val isc_tpb_consistency: Byte = 1
internal const val isc_tpb_concurrency: Byte = 2
internal const val isc_tpb_shared: Byte = 3
internal const val isc_tpb_protected: Byte = 4
internal const val isc_tpb_exclusive: Byte = 5
internal const val isc_tpb_wait: Byte = 6
internal const val isc_tpb_nowait: Byte = 7
internal const val isc_tpb_read: Byte = 8
internal const val isc_tpb_write: Byte = 9
internal const val isc_tpb_lock_read: Byte = 10
internal const val isc_tpb_lock_write: Byte = 11
internal const val isc_tpb_ignore_limbo: Byte = 14
internal const val isc_tpb_read_committed: Byte = 15
internal const val isc_tpb_autocommit: Byte = 16
internal const val isc_tpb_rec_version: Byte = 17
internal const val isc_tpb_no_rec_version: Byte = 18
internal const val isc_tpb_restart_requests: Byte = 19
internal const val isc_tpb_no_auto_undo: Byte = 20
internal const val isc_tpb_lock_timeout: Byte = 21
internal const val isc_tpb_read_consistency: Byte = 22
internal const val isc_tpb_at_snapshot_number: Byte = 23

internal interface Clumplet {
    fun version(version: Byte)
    fun add(code: Byte)
    fun tag(code: Byte)
    fun addString(code: Byte, value: String)
    fun addByteArray(code: Byte, value: ByteArray)
    fun addInt(code: Byte, value: Int)
    fun addUInt(code: Byte, value: UInt)
    fun addUByte(code: Byte, value: UByte)
    fun addUShort(code: Byte, value: UShort)
    fun addBoolean(code: Byte, value: Boolean)
}

interface DatabaseParams {
    fun userName(value: String)
    fun password(value: String)
    fun setDBCharset(value: String)
    fun sqlDialect(value: Int)
    fun pageSize(value: Int)
    fun numBuffers(value: Int)
    fun debug(value: Int)
    fun verify(value: Int)
    fun sweep(value: Int)
    fun enableJournal(value: String)
    fun dbkeyScope(value: Int)
    fun trace(value: Int)
    fun noGarbageCollect()
    fun damaged()
    fun activateShadow()
    fun sweepInterval(value: Int)
    fun deleteShadow()
    fun forceWrite(value: Boolean)
    fun noReserve(value: Boolean)
    fun interp(value: Int)
    fun dropWalfile(value: Int)
    fun shutdown(value: Int)
    fun online(value: Int)
    fun shutdownDelay(value: Int)
    fun reserved()
    fun overwrite(value: Boolean)
    fun secAttach(value: Boolean)
    fun disableWal()
    fun connectTimeout(value: Int)
    fun dummyPacketInterval(value: Int)
    fun gbakAttach(value: String)
    fun sqlRoleName(value: String)
    fun setPageBuffers(value: Int)
    fun workingDirectory(value: String)
    fun setDbReadonly(value: Boolean)
    fun setDbSqlDialect(value: Int)
    fun gfixAttach()
    fun gstatAttach()
    fun addressPath(value: ByteArray)
    fun processId(value: Int)
    fun noDbTriggers(value: Boolean)
    fun trustedAuth(value: String)
    fun processName(value: String)
    fun orgFilename(value: String)
    fun extCallDepth(value: Int)
    fun authBlock(value: ByteArray)
    fun clientVersion(value: String)
    fun remoteProtocol(value: String)
    fun hostName(value: String)
    fun osUser(value: String)
    fun config(value: String)
    fun noLinger()
    fun resetIcu()
    fun mapAttach()
    fun sessionTimeZone(value: String)
    fun setDbReplica(value: Int)
    fun setBind(value: String)
    fun decfloatRound(value: String)
    fun decfloatTraps(value: String)
    fun clearMap(value: Boolean)
    fun upgradeDb()
    fun parallelWorkers(value: Int)
    fun workerAttach()
}

interface TransactionParams {
    fun consistency()
    fun concurrency()
    fun shared()
    fun protected()
    fun exclusive()
    fun waitForIt()
    fun noWait()
    fun read()
    fun write()
    fun lockRead(value: String)
    fun lockWrite(value: String)
    fun ignoreLimbo()
    fun readCommitted()
    fun autoCommit()
    fun recVersion()
    fun noRecVersion()
    fun restartRequests()
    fun noAutoUndo()
    fun lockTimeout(value: Int)
    fun readConsistency()
    fun atSnapshotNumber(value: Int)
}

internal fun createPB(what: Clumplet.() -> Unit): ByteArray {
    var ver: Byte = 0
    var calc = true
    var index = 1
    var ret: ByteArray? = null
    val stack: MutableList<ByteArray> = mutableListOf()
    val params = object : Clumplet, DatabaseParams, TransactionParams {

        override fun version(version: Byte) {
            ver = version
        }

        override fun tag(code: Byte) {
            if (calc) index += 2
            else {
                ret!![index++] = code
                ret!![index++] = 0
            }
        }

        override fun add(code: Byte) {
            if (calc) index += 1
            else ret!![index++] = code
        }

        override fun addString(code: Byte, value: String) {
            addByteArray(code, value.encodeToByteArray())
        }

        override fun addByteArray(code: Byte, value: ByteArray) {
            val data = if (calc) {
                stack.add(value)
                value
            } else {
                stack.removeAt(0)
            }
            val size = data.size.toUByte()
            if (calc) {
                index += size.toInt() + 2
            } else {
                ret!![index++] = code
                ret!![index++] = size.toByte()
                data.copyInto(ret!!, index, 0, size.toInt())
                index += size.toInt()
            }
        }

        override fun addInt(code: Byte, value: Int) {
            addUInt(code, value.toUInt())
        }

        override fun addUInt(code: Byte, value: UInt) {
            if (calc) {
                index += when {
                    value <= 255u -> 3
                    value <= 65535u -> 4
                    else -> 6
                }
            } else when {
                value <= 255u -> {
                    ret!![index++] = code
                    ret!![index++] = 1
                    ret!![index++] = value.toByte()
                }

                value <= 65535u -> {
                    ret!![index++] = code
                    ret!![index++] = 2
                    ret!![index++] = (value shr 8).toByte()
                    ret!![index++] = value.toByte()
                }

                else -> {
                    ret!![index++] = code
                    ret!![index++] = 4
                    ret!![index++] = (value shr 24).toByte()
                    ret!![index++] = (value shr 16).toByte()
                    ret!![index++] = (value shr 8).toByte()
                    ret!![index++] = value.toByte()
                }
            }
        }

        override fun addBoolean(code: Byte, value: Boolean) {
            addUInt(code, if (value) 1u else 0u)
        }

        override fun addUByte(code: Byte, value: UByte) {
            if (calc) index += 1
            else ret!![index++] = value.toByte()
        }

        override fun addUShort(code: Byte, value: UShort) {
            if (calc) index += 2
            else {
                ret!![index++] = (value.toInt() shr 8).toByte()
                ret!![index++] = value.toByte()
            }

        }

        // Database

        override fun userName(value: String) {
            addString(isc_dpb_user_name, value)
        }

        override fun password(value: String) {
            addString(isc_dpb_password, value)
        }

        override fun setDBCharset(value: String) {
            addString(isc_dpb_set_db_charset, value)
        }

        override fun sqlDialect(value: Int) {
            addUInt(isc_dpb_sql_dialect, value.toUInt())
        }

        override fun pageSize(value: Int) {
            addInt(isc_dpb_page_size, value)
        }

        override fun numBuffers(value: Int) {
            addInt(isc_dpb_num_buffers, value)
        }

        override fun debug(value: Int) {
            addInt(isc_dpb_debug, value)
        }

        override fun verify(value: Int) {
            addInt(isc_dpb_verify, value)
        }

        override fun sweep(value: Int) {
            addInt(isc_dpb_sweep, value)
        }

        override fun enableJournal(value: String) {
            addString(isc_dpb_enable_journal, value)
        }

        override fun dbkeyScope(value: Int) {
            addInt(isc_dpb_dbkey_scope, value)
        }

        override fun trace(value: Int) {
            addInt(isc_dpb_trace, value)
        }

        override fun noGarbageCollect() {
            tag(isc_dpb_no_garbage_collect)
        }

        override fun damaged() {
            addUInt(isc_dpb_damaged, 1u)
        }

        override fun activateShadow() {
            add(isc_dpb_activate_shadow)
        }

        override fun sweepInterval(value: Int) {
            addInt(isc_dpb_sweep_interval, value)
        }

        override fun deleteShadow() {
            tag(isc_dpb_delete_shadow)
        }

        override fun forceWrite(value: Boolean) {
            addBoolean(isc_dpb_force_write, value)
        }

        override fun noReserve(value: Boolean) {
            addBoolean(isc_dpb_no_reserve, value)
        }

        override fun interp(value: Int) {
            addInt(isc_dpb_interp, value)
        }

        override fun dropWalfile(value: Int) {
            addInt(isc_dpb_drop_walfile, value)
        }

        override fun shutdown(value: Int) {
            addInt(isc_dpb_shutdown, value)
        }

        override fun online(value: Int) {
            addInt(isc_dpb_online, value)
        }

        override fun shutdownDelay(value: Int) {
            addInt(isc_dpb_shutdown_delay, value)
        }

        override fun reserved() {
            addString(isc_dpb_reserved, "YES")
        }

        override fun overwrite(value: Boolean) {
            addBoolean(isc_dpb_overwrite, value)
        }

        override fun secAttach(value: Boolean) {
            addBoolean(isc_dpb_sec_attach, value)
        }

        override fun disableWal() {
            tag(isc_dpb_disable_wal)
        }

        override fun connectTimeout(value: Int) {
            addInt(isc_dpb_connect_timeout, value)
        }

        override fun dummyPacketInterval(value: Int) {
            addInt(isc_dpb_dummy_packet_interval, value)
        }

        override fun gbakAttach(value: String) {
            addString(isc_dpb_gbak_attach, value)
        }

        override fun sqlRoleName(value: String) {
            addString(isc_dpb_sql_role_name, value)
        }

        override fun setPageBuffers(value: Int) {
            addInt(isc_dpb_set_page_buffers, value)
        }

        override fun workingDirectory(value: String) {
            addString(isc_dpb_working_directory, value)
        }

        override fun setDbReadonly(value: Boolean) {
            addBoolean(isc_dpb_set_db_readonly, value)
        }

        override fun setDbSqlDialect(value: Int) {
            addInt(isc_dpb_set_db_sql_dialect, value)
        }

        override fun gfixAttach() {
            tag(isc_dpb_gfix_attach)
        }

        override fun gstatAttach() {
            tag(isc_dpb_gstat_attach)
        }

        override fun addressPath(value: ByteArray) {
            addByteArray(isc_dpb_address_path, value)
        }

        override fun processId(value: Int) {
            addInt(isc_dpb_process_id, value)
        }

        override fun noDbTriggers(value: Boolean) {
            addBoolean(isc_dpb_no_db_triggers, value)
        }

        override fun trustedAuth(value: String) {
            addString(isc_dpb_trusted_auth, value)
        }

        override fun processName(value: String) {
            addString(isc_dpb_process_name, value)
        }

        override fun orgFilename(value: String) {
            addString(isc_dpb_org_filename, value)
        }

        override fun extCallDepth(value: Int) {
            addInt(isc_dpb_ext_call_depth, value)
        }

        override fun authBlock(value: ByteArray) {
            addByteArray(isc_dpb_auth_block, value)
        }

        override fun clientVersion(value: String) {
            addString(isc_dpb_client_version, value)
        }

        override fun remoteProtocol(value: String) {
            addString(isc_dpb_remote_protocol, value)
        }

        override fun hostName(value: String) {
            addString(isc_dpb_host_name, value)
        }

        override fun osUser(value: String) {
            addString(isc_dpb_os_user, value)
        }

        override fun config(value: String) {
            addString(isc_dpb_config, value)
        }

        override fun noLinger() {
            tag(isc_dpb_nolinger)
        }

        override fun resetIcu() {
            tag(isc_dpb_reset_icu)
        }

        override fun mapAttach() {
            add(isc_dpb_map_attach)
        }

        override fun sessionTimeZone(value: String) {
            addString(isc_dpb_session_time_zone, value)
        }

        override fun setDbReplica(value: Int) {
            addInt(isc_dpb_set_db_replica, value)
        }

        override fun setBind(value: String) {
            addString(isc_dpb_set_bind, value)
        }

        override fun decfloatRound(value: String) {
            addString(isc_dpb_decfloat_round, value)
        }

        override fun decfloatTraps(value: String) {
            addString(isc_dpb_decfloat_traps, value)
        }

        override fun clearMap(value: Boolean) {
            addBoolean(isc_dpb_clear_map, value)
        }

        override fun upgradeDb() {
            add(isc_dpb_upgrade_db)
        }

        override fun parallelWorkers(value: Int) {
            addInt(isc_dpb_parallel_workers, value)
        }

        override fun workerAttach() {
            add(isc_dpb_worker_attach)
        }

        // Transaction

        override fun consistency() {
            add(isc_tpb_consistency)
        }

        override fun concurrency() {
            add(isc_tpb_concurrency)
        }

        override fun shared() {
            add(isc_tpb_shared)
        }

        override fun protected() {
            add(isc_tpb_protected)
        }

        override fun exclusive() {
            add(isc_tpb_exclusive)
        }

        override fun waitForIt() {
            add(isc_tpb_wait)
        }

        override fun noWait() {
            add(isc_tpb_nowait)
        }

        override fun read() {
            add(isc_tpb_read)
        }

        override fun write() {
            add(isc_tpb_write)
        }

        override fun lockRead(value: String) {
            addString(isc_tpb_lock_read, value)
        }

        override fun lockWrite(value: String) {
            addString(isc_tpb_lock_write, value)
        }

        override fun ignoreLimbo() {
            add(isc_tpb_ignore_limbo)
        }

        override fun readCommitted() {
            add(isc_tpb_read_committed)
        }

        override fun autoCommit() {
            add(isc_tpb_autocommit)
        }

        override fun recVersion() {
            add(isc_tpb_rec_version)
        }

        override fun noRecVersion() {
            add(isc_tpb_no_rec_version)
        }

        override fun restartRequests() {
            add(isc_tpb_restart_requests)
        }

        override fun noAutoUndo() {
            add(isc_tpb_no_auto_undo)
        }

        override fun lockTimeout(value: Int) {
            addInt(isc_tpb_lock_timeout, value)
        }

        override fun readConsistency() {
            add(isc_tpb_read_consistency)
        }

        override fun atSnapshotNumber(value: Int) {
            addInt(isc_tpb_at_snapshot_number, value)
        }
    }

    params.what()
    ret = ByteArray(index)
    ret[0] = ver
    index = 1
    calc = false

    params.what()

    return ret
}


/**
 * Creates a dynamic parameter block (DPB) for a database connection.
 *
 * @param params A lambda expression that configures the database connection parameters.
 * The lambda has a receiver of type [DatabaseParams], which allows calling various methods to set the desired parameters.
 *
 * @return The DPB as a byte array.
 */
fun makeDPB(params: DatabaseParams.() -> Unit): ByteArray {
    return createPB {
        version(isc_dpb_version1)
        tag(isc_dpb_utf8_filename)
        (this as DatabaseParams).params()
        addString(isc_dpb_lc_ctype, "UTF-8")
    }
}

/**
 * Creates a Transaction Parameter Buffer (TPB) based on the provided configuration.
 *
 * @param params a lambda expression that configures the TransactionParams object.
 * @return the byte array representing the TPB.
 */
fun makeTPB(params: TransactionParams.() -> Unit): ByteArray {
    return createPB {
        version(isc_tpb_version1)
        (this as TransactionParams).params()
    }
}

