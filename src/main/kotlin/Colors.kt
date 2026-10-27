package cn.anling.inftool

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Colors {
    object DiveCard {
        object Normal {
            object Background {
                val perfectDive = Brush.linearGradient(
                    0.25F to Color(255, 255, 255),
                    1.00F to Color(237, 187, 255),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val fullLink = Brush.linearGradient(
                    0.25F to Color(255, 255, 255),
                    1.00F to Color(173, 150, 247),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val diveCleared = Brush.linearGradient(
                    0.25F to Color(255, 255, 255),
                    1.00F to Color(123, 245, 255),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val diveFailed = Brush.linearGradient(
                    0.25F to Color(255, 255, 255),
                    1.00F to Color(255, 105, 81),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val none = Brush.linearGradient(
                    0.25F to Color(255, 255, 255),
                    1.00F to Color(70, 83, 80),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )
            }
        }

        object Flat {
            object Background {
                val perfectDive = Brush.linearGradient(
                    0.25F to Color(255, 255, 255).copy(alpha = 0.25F),
                    1.00F to Color(237, 187, 255).copy(alpha = 0.75F),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val fullLink = Brush.linearGradient(
                    0.25F to Color(255, 255, 255).copy(alpha = 0.25F),
                    1.00F to Color(173, 150, 247).copy(alpha = 0.75F),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val diveCleared = Brush.linearGradient(
                    0.25F to Color(255, 255, 255).copy(alpha = 0.25F),
                    1.00F to Color(123, 245, 255).copy(alpha = 0.75F),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val diveFailed = Brush.linearGradient(
                    0.25F to Color(255, 255, 255).copy(alpha = 0.25F),
                    1.00F to Color(255, 105, 81).copy(alpha = 0.75F),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )

                val none = Brush.linearGradient(
                    0.25F to Color(255, 255, 255).copy(alpha = 0.25F),
                    1.00F to Color(70, 83, 80).copy(alpha = 0.75F),
                    start = Offset.Infinite.copy(x = 0.0F),
                    end = Offset.Infinite.copy(y = 0.0F),
                )
            }
        }

        object JudgementCount {
            object Background {
                val exactPlus = Brush.horizontalGradient(
                    0.25F to Color(247, 174, 247).copy(alpha = 0.25F),
                    1.00F to Color(247, 174, 247).copy(alpha = 0.75F),
                )

                val exact = Brush.horizontalGradient(
                    0.25F to Color(140, 134, 255).copy(alpha = 0.25F),
                    1.00F to Color(140, 134, 255).copy(alpha = 0.75F),
                )

                val near = Brush.horizontalGradient(
                    0.25F to Color(114, 190, 206).copy(alpha = 0.25F),
                    1.00F to Color(114, 190, 206).copy(alpha = 0.75F),
                )

                val `break` = Brush.horizontalGradient(
                    0.25F to Color(225, 89, 95).copy(alpha = 0.25F),
                    1.00F to Color(225, 89, 95).copy(alpha = 0.75F),
                )
            }

            val early = Color(148, 170, 222)
            val late = Color(255, 149, 170)
        }
    }

    object SongCard {
        object Background {
            val perfectDive = Brush.linearGradient(
                0.00F to Color(237, 187, 255),
                1.00F to Color(255, 255, 255),
            )

            val fullLink = Brush.linearGradient(
                0.00F to Color(173, 150, 247),
                1.00F to Color(255, 255, 255),
            )

            val diveCleared = Brush.linearGradient(
                0.00F to Color(123, 245, 255),
                1.00F to Color(255, 255, 255),
            )

            val diveFailed = Brush.linearGradient(
                0.00F to Color(255, 105, 81),
                1.00F to Color(255, 255, 255),
            )

            val none = Brush.linearGradient(
                0.00F to Color(70, 83, 80),
                1.00F to Color(255, 255, 255),
            )
        }

        object Difficult {
            val minimal = Color(24, 211, 255)
            val evolved = Color(24, 199, 222)
            val ultimate = Color(206, 142, 255)
            val forbidden = Color(255, 44, 74)
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
            }
        }
    }

    object Ranking {
        object SongCard {
            object Background {
                val minimal
                    @Composable
                    get() = Brush.horizontalGradient(
                        0.0F to MaterialTheme.colorScheme.surface.copy(alpha = 0.5F),
                        1.0F to Color(24, 211, 255),
                    )

                val evolved
                    @Composable
                    get() = Brush.horizontalGradient(
                        0.0F to MaterialTheme.colorScheme.surface.copy(alpha = 0.5F),
                        1.0F to Color(24, 199, 222),
                    )

                val ultimate
                    @Composable
                    get() = Brush.horizontalGradient(
                        0.0F to MaterialTheme.colorScheme.surface.copy(alpha = 0.5F),
                        1.0F to Color(206, 142, 255),
                    )

                val forbidden
                    @Composable
                    get() = Brush.horizontalGradient(
                        0.0F to MaterialTheme.colorScheme.surface.copy(alpha = 0.5F),
                        1.0F to Color(255, 44, 74),
                    )
            }
        }
    }

    object PotentialWindow {
        val potentialIncrease = Color(83, 225, 230)
        val potentialDecrease = Color(206, 60, 60)
        val potentialKeep = Color(178, 178, 178)
    }
}