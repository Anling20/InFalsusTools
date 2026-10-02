package cn.anling.inftool

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Colors {
    object SongCard {
        object Background {
            val perfectDive
                @Composable
                get() = MaterialTheme.colorScheme.primaryContainer

            val fullLink
                @Composable
                get() = MaterialTheme.colorScheme.tertiaryContainer

            val diveCleared
                @Composable
                get() = MaterialTheme.colorScheme.surfaceContainerHighest

            val diveFailed
                @Composable
                get() = MaterialTheme.colorScheme.surfaceContainerHighest

            val none
                @Composable
                get() = MaterialTheme.colorScheme.surfaceContainerHighest
        }

        object Difficult {
            val min = Color(30, 147, 227)
            val evo = Color(42, 150, 137)
            val ult = Color(138, 43, 226)
            val fbd = Color(231, 24, 11)
        }

        object Judgement {
            object ExactPlus {
                val text = Brush.verticalGradient(
                    listOf(
                        Color(247, 174, 247),
                        Color(245, 195, 242),
                        Color.White,
                    )
                )

                val outline = Color(184, 92, 175).copy(alpha = 0.75F)
                val shadow = Color(212, 122, 200).copy(alpha = 0.7F)
            }

            object Exact {
                val text = Brush.verticalGradient(
                    listOf(
                        Color(192, 95, 216),
                        Color(238, 112, 255),
                        Color.White,
                    )
                )

                val outline = Color(176, 51, 197).copy(alpha = 0.75F)
                val shadow = Color(193, 61, 223).copy(alpha = 0.7F)
            }

            object Near {
                val text = Brush.verticalGradient(
                    listOf(
                        Color(85, 187, 213),
                        Color(142, 220, 240),
                        Color.White,
                    )
                )

                val outline = Color(59, 145, 168).copy(alpha = 0.75F)
                val shadow = Color(69, 175, 203).copy(alpha = 0.7F)
            }

            object Break {
                val text = Brush.verticalGradient(
                    listOf(
                        Color(229, 62, 72),
                        Color(255, 102, 102),
                        Color.White,
                    )
                )

                val outline = Color(200, 50, 60).copy(alpha = 0.75F)
                val shadow = Color(216, 61, 71).copy(alpha = 0.7F)
            }
        }
    }
}