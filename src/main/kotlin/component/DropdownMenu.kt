package cn.anling.inftool.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    text: String,
    label: String,
    content: @Composable (ColumnScope.((Boolean) -> Unit) -> Unit),
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TextFieldDefaults.colors().focusedContainerColor.copy(alpha = 0.6F),
                unfocusedContainerColor = TextFieldDefaults.colors().unfocusedContainerColor.copy(alpha = 0.6F),
            ),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            content { expanded = it }
        }
    }
}