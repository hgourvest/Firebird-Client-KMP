import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(ExperimentalStdlibApi::class)
abstract class Pool<T: Any>(size: Int): AutoCloseable {
    private val input = Channel<T>(size)
    private val output = Channel<T>(Channel.RENDEZVOUS)
    private val items: MutableList<T> = mutableListOf()
    abstract fun newInstance(): T
    abstract fun freeInstance(value: T)
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    val job = GlobalScope.launch {
        var count = 0
        while (true) {
            if (input.isEmpty && count < size) {
                val item = newInstance()
                items.add(item)
                dispose(item)
                count++
            } else
                output.send(input.receive())

        }
    }
    override fun close() {
        job.cancel()
        input.cancel()
        output.cancel()
        items.forEach {
                item -> freeInstance(item)
        }
    }

    suspend fun acquire(): T = output.receive()
    suspend fun dispose(value: T) = input.send(value)
}
