package cn.anling.inftool.setting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> ReadWriteProperty<Any?, T>.toComposeStated() = ComposeStatedReadWriteProperty(this)

class ComposeStatedReadWriteProperty<in T, V>(
    private val delegate: ReadWriteProperty<T, V>,
) : ReadWriteProperty<T, V> {
    private var state by mutableStateOf<V?>(null)

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val value = delegate.getValue(thisRef, property)
        if (state != value) {
            state = value
        }
        return state as V
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        state = value
        delegate.setValue(thisRef, property, value)
    }
}