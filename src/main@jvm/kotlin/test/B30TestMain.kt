package test

import cn.anling.inftool.command.AchievementsImageCommand
import cn.anling.inftool.command.PotentialImageCommand
import cn.anling.inftool.command.DispatcherCommand
import cn.anling.inftool.command.FloatingWindowCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.subcommands

suspend fun main() {
    val args = listOf("--experimental-level", "b30", "--out=b30.jpeg")
    DispatcherCommand().subcommands(PotentialImageCommand(), FloatingWindowCommand(), AchievementsImageCommand()).main(args)
}