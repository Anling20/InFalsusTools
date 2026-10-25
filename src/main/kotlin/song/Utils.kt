package cn.anling.inftool.song

import cn.anling.inftool.Difficult
import cn.anling.inftool.Difficult.*
import cn.anling.inftool.DiveRecord

fun ConstantTable.selectLevel(record: DiveRecord): Double = this[record.id]?.run {
    when (record.difficult) {
        MIN -> minimal
        EVO -> evolved
        ULT -> ultimate
        FBD -> forbidden
    }
} ?: 0.0

fun ConstantTable.selectLevel(id: Int, difficult: Difficult): Double = this[id]?.run {
    when (difficult) {
        MIN -> minimal
        EVO -> evolved
        ULT -> ultimate
        FBD -> forbidden
    }
} ?: 0.0