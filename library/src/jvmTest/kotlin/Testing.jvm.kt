import java.io.File

actual fun Testing.deleteTestDB(path: String) {
    File(path).delete()
}

actual fun Testing.getTestDBPath(): String {
    val tmp = System.getProperty("java.io.tmpdir")
    val time = System.currentTimeMillis()
    return "$tmp/fbtest$time.fdb"
}
