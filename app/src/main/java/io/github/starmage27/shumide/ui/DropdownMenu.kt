package io.github.starmage27.shumide.ui

import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    label: String = "Dropdown menu",
    selectedElement: String = "",
    elements: List<String> = emptyList(),
    onSelected: (selectedElement: String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it && elements.isNotEmpty() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedElement,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isExpanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .wrapContentWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)
            ,
            enabled = (elements.isNotEmpty())
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            elements.forEach { element ->
                DropdownMenuItem(
                    text = { Text(
                        text = element,
                    ) },
                    onClick = {
                        isExpanded = false
                        onSelected(selectedElement)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun <reified T : Enum<T>> EnumDropdownMenu(
    modifier: Modifier = Modifier,
    label: String = "Dropdown menu",
    selectedElement: T,
    crossinline onSelected: (selectedElement: T) -> Unit = {}
) {
    val elements = enumValues<T>().toList()
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it && elements.isNotEmpty() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedElement.name,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isExpanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .wrapContentWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            enabled = elements.isNotEmpty()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            elements.forEach { element ->
                DropdownMenuItem(
                    text = { Text(element.name) },
                    onClick = {
                        isExpanded = false
                        onSelected(element)
                    }
                )
            }
        }
    }
}