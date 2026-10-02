package cn.anling.inftool

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.anling.infalsustools.generated.resources.Res
import cn.anling.infalsustools.generated.resources.banner_story
import cn.anling.infalsustools.generated.resources.head_story
import cn.anling.inftool.ClearType.DiveFailed
import cn.anling.inftool.Difficult.ULT
import cn.anling.inftool.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import java.text.DecimalFormat

@Composable
fun B30(
    darkTheme: Boolean,
    onSizeChanged: (IntSize) -> Unit,
    username: String,
    data: List<ProcessedDiveRecord>,
) {
    AppTheme(darkTheme) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
        ) {
            val sortedData = data.sortedByDescending {
                it.potential
            }
            val potential = remember(Unit) { data.potential() }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .onSizeChanged(onSizeChanged)
                    .padding(24.dp),
            ) {
                // Head
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.head_story),
                        contentDescription = "Head image",
                        modifier = Modifier.size(256.dp),
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = username,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 48.sp,
                            maxLines = 1,
                        )
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            val formater = remember(Unit) {
                                DecimalFormat("0.000")
                            }
                            Image(
                                painter = painterResource(Res.drawable.banner_story),
                                contentDescription = "Banner image",
                                modifier = Modifier.size(256.dp, 64.dp),
                            )
                            Text(
                                text = formater.format(potential),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 24.sp,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Content
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    maxItemsInEachRow = 5,
                ) {
                    repeat(30) { index ->
                        val diveRecord = sortedData.getOrNull(index)
                        // 物量有误, 待修
                        SongCard(
                            clearType = diveRecord?.clearType ?: DiveFailed,
                            grading = diveRecord?.grading,
                            difficult = diveRecord?.difficult ?: ULT,
                            level = diveRecord?.level ?: 0.0,
                            name = diveRecord?.song?.title ?: "",
                            position = index + 1,
                            image = diveRecord?.song?.name,
                            score = diveRecord?.score ?: 0,
                            potential = diveRecord?.potential ?: 0.0,
                            exactPlus = diveRecord?.count?.exactPlus,
                            exact = diveRecord?.count?.exact,
                            near = diveRecord?.count?.near,
                            `break` = diveRecord?.count?.`break`,
                        )
                    }
                }
            }
        }
    }
}