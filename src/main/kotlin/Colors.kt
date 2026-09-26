package cn.anling.inftool

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Colors {
    object SongCard {
        object Background {
            val perfectDive
                @Composable
                get() = MaterialTheme.colorScheme.surfaceContainerHighest

            val fullLink
                @Composable
                get() = MaterialTheme.colorScheme.primaryFixedDim

            val diveCleared
                @Composable
                get() = MaterialTheme.colorScheme.primaryContainer

            val diveFailed
                @Composable
                get() = MaterialTheme.colorScheme.errorContainer

            val none
                @Composable
                get() = MaterialTheme.colorScheme.surfaceDim
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