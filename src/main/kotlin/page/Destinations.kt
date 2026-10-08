package cn.anling.inftool.page

sealed class HomeDestination {
    data object Home : HomeDestination()
    data class Module(val child: ModuleDestination) : HomeDestination()
    data object Setting : HomeDestination()
    data object More : HomeDestination()
}

sealed class ModuleDestination {
    data object PotentialWindow : ModuleDestination()
    data object Best30Image : ModuleDestination()
    data object AchievementsImage : ModuleDestination()
    data object PlayHistories : ModuleDestination()
}