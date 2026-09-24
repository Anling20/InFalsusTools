import cn.anling.inftool.InfToolCommand
import com.github.ajalt.clikt.command.main
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.PrintStream

private fun redirectOutput() {
    val logFile = File("log.txt")
    val stream = PrintStream(logFile.outputStream(), true)
    System.setOut(stream)
    System.setErr(stream)
}

fun main(args: Array<String>) {
    redirectOutput()
    runBlocking {
        InfToolCommand().main(args)
    }
}