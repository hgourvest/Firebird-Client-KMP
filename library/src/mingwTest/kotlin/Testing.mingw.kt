import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.unlink
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
actual fun Testing.getTestDBPath(): String {
    val random = Random.nextLong()
    val temp = getenv("TEMP")!!.toKString()
    return "$temp/fbtest$random.fdb"
}

actual fun Testing.deleteTestDB(path: String) {
    unlink(path)
}
