package cn.anling.inftool

import androidx.compose.ui.graphics.Color

object Colors {
    object SongCard {
        object Background {
            val perfectDive = Color(229, 225, 233)
            val fullLink = Color(196, 192, 255)
            val diveCleared = Color(227, 223, 255)
            val diveFailed = Color(255, 218, 214)
        }

        object Difficult {
            val min = Color(30, 147, 227)
            val evo = Color(42, 150, 137)
            val ult = Color(138, 43, 226)
            val fbd = Color(231, 24, 11)
        }

        object Judgement {
            val exactPlus = Color(255, 175, 239)
            val exact = Color(173, 150, 255)
            val near = Color(148, 211, 214)
            val `break` = Color(243, 107, 117)
        }
    }
}