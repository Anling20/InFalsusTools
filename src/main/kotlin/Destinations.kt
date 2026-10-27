package cn.anling.inftool

sealed class HomeDestination {
    data object Home : HomeDestination()
    data class Module(val child: ModuleDestination) : HomeDestination()
    data object Terminal : HomeDestination()
    data object Setting : HomeDestination()
    data object More : HomeDestination()
}

sealed class ModuleDestination {
    data object PotentialWindow : ModuleDestination()
    data object PotentialImage : ModuleDestination()
    data object AchievementsImage : ModuleDestination()
    data object PlayHistories : ModuleDestination()
    data object Ranking : ModuleDestination()
}