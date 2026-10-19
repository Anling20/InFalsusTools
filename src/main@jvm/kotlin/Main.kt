
import cn.anling.inftool.command.AchievementsImageCommand
import cn.anling.inftool.command.DispatcherCommand
import cn.anling.inftool.command.FloatingWindowCommand
import cn.anling.inftool.command.PotentialImageCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands
import io.github.vinceglb.filekit.FileKit
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
    FileKit.init("cn.anling.inftool")
    DispatcherCommand()
        .subcommands(PotentialImageCommand(), FloatingWindowCommand(), AchievementsImageCommand())
        .main(args)
}