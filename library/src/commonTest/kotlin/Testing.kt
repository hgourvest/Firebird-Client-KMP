import com.progdigy.fbclient.*
import com.progdigy.fbclient.Attachment.Transaction
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

expect fun Testing.getTestDBPath(): String
expect fun Testing.deleteTestDB(path: String)

private fun Transaction.createTable() {
    execute("CREATE GENERATOR GEN_TEST")
    execute("""
                    CREATE TABLE TEST_TABLE (
                        ID INT NOT NULL PRIMARY KEY,
                        DESCRIPTION VARCHAR(32)
                    );
                    """.trimIndent()
    )
}

private fun Transaction.createData(count: Int) {
    statement("INSERT INTO TEST_TABLE (ID, DESCRIPTION) VALUES (GEN_ID(GEN_TEST, 1), 'data') RETURNING ID") {
        repeat(count) {
            execute()
        }
    }
}

class Testing {
    val dpb = makeDPB {
        userName("SYSDBA")
        password("masterkey")
        setDBCharset("UTF8")
        sqlDialect(3)
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun attachment(block: Attachment.(String) -> Unit) {
        val db = "localhost:"+getTestDBPath()
        try {
            Attachment.createDatabase(db, dpb).use {
                it.block(db)
            }
        } finally {
            deleteTestDB(db)
        }
    }

    @Test
    fun dataTypes() {
        attachment {
            transaction {
                val table = "RDB\$DATABASE"
                val array = "0123456789".encodeToByteArray()
                statement("""
                    select 
                        cast(? as smallint),
                        cast(? as int),
                        cast(? as bigint),
                        cast(? as int128),
                        cast(? as double precision),
                        cast(? as float),
                        cast(? as boolean),
                        cast(? as char(10)),
                        cast(? as varchar(10)),
                        cast(? as char(10) CHARACTER SET NONE),
                        cast(? as varchar(10) CHARACTER SET NONE)
                       
                    from $table
                """.trimIndent()) {
                    params.apply {
                        setShort(0, Short.MAX_VALUE)
                        setInt(1, Int.MAX_VALUE)
                        setLong(2, Long.MAX_VALUE)
                        setLong(3, Long.MIN_VALUE)
                        setDouble(4, Double.MAX_VALUE)
                        setFloat(5, Float.MAX_VALUE)
                        setBoolean(6, true)
                        setString(7,"firebird")
                        setString(8,"firebird")
                        setByteArray(9, array)
                        setByteArray(10, array)
                    }
                    open {
                        assertEquals(Short.MAX_VALUE, getShort(0))
                        assertEquals(Int.MAX_VALUE, getInt(1))
                        assertEquals(Long.MAX_VALUE, getLong(2))
                        assertEquals(Short.MAX_VALUE, getShort(0))
                        val int128 = getInt128(3)
                        assertEquals(Long.MIN_VALUE, int128[0])
                        assertEquals(-1L, int128[1])
                        assertEquals(Float.MAX_VALUE, getFloat(5))
                        assertEquals(true, getBoolean(6))
                        assertEquals("firebird  ", getString(7))
                        assertEquals("firebird", getString(8))
                        assertContentEquals(array, getByteArray(9))
                        assertContentEquals(array, getByteArray(10))
                    }

                }
            }
        }
    }

    @Test
    fun returning() {
        attachment {
            transaction {
                createTable()
                commitRetaining()

                statement("INSERT INTO TEST_TABLE (ID, DESCRIPTION) VALUES (GEN_ID(GEN_TEST, 1), ?) RETURNING ID") {
                    params.apply {
                        setString(0, "Some value")
                    }

                    execute()
                    assertEquals(result.getInt(0), 1)
                }
            }
        }
    }

    @Test
    fun select_for_update() {
        attachment {
            transaction {
                createTable()
                commitRetaining()
                createData(10)
                commitRetaining()

                statement("SELECT * FROM TEST_TABLE FOR UPDATE", "S") {
                    open {
                        statement("UPDATE TEST_TABLE SET DESCRIPTION = ? WHERE CURRENT OF S") {
                            while (!eof) {
                                params.setString(0, "updated")
                                execute()
                                fetch()
                            }
                        }
                    }
                }
                commitRetaining()

                statement("SELECT DESCRIPTION FROM TEST_TABLE") {
                    forEach {
                        assertEquals(getString(0), "updated")
                    }
                }
            }
        }
    }

    @Test
    fun read_write_blob() {
        attachment {
            transaction {
                statement("select cast(? as blob sub_type text character set WIN1252) from RDB\$DATABASE") {
                   listOf("", "lorem ipsum", "sp€cial offer").forEach { data ->
                       // Text
                       params.setString(0, data)
                       open {
                           assertEquals(data, getString(0))
                       }

                       // ByteArray
                       params.setByteArray(0, data.encodeToByteArray())
                       open {
                           assertEquals(data, getByteArray(0).decodeToString())
                       }

                       // buffered usage
                       params.setBlobId(0, blobCreate {
                           write(data.encodeToByteArray())
                           write(data.encodeToByteArray())
                       })
                       open {
                           blobOpen(getBlobId(0)) {
                               val buffer = ByteArray(getLength().toInt())
                               read(buffer)
                               val text = buffer.decodeToString()
                               assertEquals(data + data, text)
                           }
                       }
                   }
                }
            }
        }
    }

    inner class DBPool(size: Int, private val db: String): Pool<Attachment>(size) {
        override fun newInstance(): Attachment {
            return Attachment.attachDatabase(db, dpb)
        }

        override fun freeInstance(value: Attachment) {
            value.close()
        }
    }

    private fun Attachment.insert() {
        execute("INSERT INTO TEST_TABLE (ID, DESCRIPTION) VALUES (GEN_ID(GEN_TEST, 1), 'data')")
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun pooling() {
        attachment { db ->
            transaction {
                createTable()
            }

            val count = 100L

            DBPool(8, db).use { pool ->
                runBlocking {
                    for (i in 1..count) {
                        launch {
                            val v = pool.acquire()
                            try {
                                v.insert()
                            } finally {
                                pool.dispose(v)
                            }
                        }
                    }
                }
            }

            statement("select count(id) from TEST_TABLE") {
                open {
                    assertEquals(getLong(0), count)
                }
            }
        }
    }
}