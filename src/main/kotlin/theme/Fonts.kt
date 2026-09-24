package cn.anling.inftool.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import cn.anling.infalsustools.generated.resources.Exo2_Regular
import cn.anling.infalsustools.generated.resources.MapleMono_NF_CN_Regular
import cn.anling.infalsustools.generated.resources.NotoSans_Regular
import cn.anling.infalsustools.generated.resources.Res
import org.jetbrains.compose.resources.Font

@get:Composable
val maplemono: FontFamily
    get() = FontFamily(
        Font(
            resource = Res.font.MapleMono_NF_CN_Regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )

@get:Composable
val notosans: FontFamily
    get() = FontFamily(
        Font(
            resource = Res.font.NotoSans_Regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )

@get:Composable
val exo2: FontFamily
    get() = FontFamily(
        Font(
            resource = Res.font.Exo2_Regular,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )