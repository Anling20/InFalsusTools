package cn.anling.inftool

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
            val minimal = Brush.verticalGradient(
                listOf(
                    Color(0, 111, 160),
                    Color(8, 174, 255),
                )
            )

            val evolved = Brush.verticalGradient(
                listOf(
                    Color(3, 182, 204),
                    Color(8, 230, 255),
                )
            )

            val ultimate = Brush.verticalGradient(
                listOf(
                    Color(90, 12, 131),
                    Color(206, 162, 255),
                )
            )

            val forbidden = Brush.verticalGradient(
                listOf(
                    Color(181, 20, 57),
                    Color(222, 48, 107),
                )
            )
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
}