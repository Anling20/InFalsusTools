import cn.anling.inftool.command.AhvCommand
import cn.anling.inftool.command.B50Command
import cn.anling.inftool.command.DispatcherCommand
import cn.anling.inftool.command.FloatingWindowCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import java.io.File
import java.io.PrintStream

private fun redirectOutput() {
    val logFile = File("log.txt")
    val stream = PrintStream(logFile.outputStream(), true)
    System.setOut(stream)
    System.setErr(stream)
}

suspend fun main(args: Array<String>) {
    redirectOutput()
    DispatcherCommand().subcommands(B50Command(), FloatingWindowCommand(), AhvCommand()).main(args)
}