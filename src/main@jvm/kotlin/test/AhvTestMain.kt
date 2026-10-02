package test

import cn.anling.inftool.command.AhvCommand
import cn.anling.inftool.command.B50Command
import cn.anling.inftool.command.DispatcherCommand
import cn.anling.inftool.command.FloatingWindowCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

suspend fun main() {
    val args = listOf("--experimental-level", "ahv", "--out=ahv.jpeg", "--min=10.0", "--max=15.0")
    DispatcherCommand().subcommands(B50Command(), FloatingWindowCommand(), AhvCommand()).main(args)
}