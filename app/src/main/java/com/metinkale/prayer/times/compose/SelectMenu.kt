package com.metinkale.prayer.times.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight


@ExperimentalMaterial3Api
@Composable
fun <T> SelectMenu(
    label: String,
    value: T,
    items: List<T>,
    itemLabel: (T) -> String,
    onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemSubLabel: ((T) -> String)? = null,
) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = itemLabel(value),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = {
                        if (itemSubLabel == null) {
                            Text(
                                text = itemLabel(it),
                            )
                        } else {
                            Column {
                                Text(
                                    text = itemLabel(it),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = itemSubLabel(it),
                                )
                            }
                        }
                    },
                    onClick = {
                        onChange(it)
                        expanded = false
                    }
                )
            }
        }
    }
}
