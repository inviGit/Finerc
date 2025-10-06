package com.invi.finerc.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearSelectors(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    months: List<Pair<Int, String>>,
    years: List<Int>
) {
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedMonth,
            onExpandedChange = { expandedMonth = !expandedMonth },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = months.find { it.first == selectedMonth }?.second ?: "Select Month",
                onValueChange = {},
                readOnly = true,
                label = { Text("Month") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }
            ) {
                months.forEach { (monthValue, monthName) ->
                    DropdownMenuItem(
                        text = { Text(monthName) },
                        onClick = {
                            onMonthSelected(monthValue)
                            expandedMonth = false
                        }
                    )
                }
            }
        }

        // Year Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedYear,
            onExpandedChange = { expandedYear = !expandedYear },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = selectedYear.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Year") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedYear,
                onDismissRequest = { expandedYear = false }
            ) {
                years.reversed().forEach { yearValue ->
                    DropdownMenuItem(
                        text = { Text(yearValue.toString()) },
                        onClick = {
                            onYearSelected(yearValue)
                            expandedYear = false
                        }
                    )
                }
            }
        }
    }
}
