import platform.posix.*
import kotlin.random.Random

actual fun Testing.getTestDBPath(): String {
    return "/tmp/fbtest${Random.nextLong()}.fdb"
}

actual fun Testing.deleteTestDB(path: String) {
    unlink(path)
}
