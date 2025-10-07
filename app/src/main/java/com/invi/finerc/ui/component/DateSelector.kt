package com.invi.finerc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.invi.finerc.common.AppUtils
import java.time.LocalDate

@Composable
fun DateSelector(
    selectedYear: Int,
    selectedMonth: Int,
    onYearSelected: (Int) -> Unit = {},         // Optional (default = no-op)
    onMonthSelected: (Int) -> Unit = {},        // Optional (default = no-op)
    isMonthDisabled: Boolean = false,           // Optional (default = false)
    isYearDisabled: Boolean = false             // Optional (default = false)
) {
    val years = (2020..LocalDate.now().year).toList().reversed()
    val months = (1..12).toList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select Period",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Year Selector
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            items(years.size) { idx ->
                                val year = years[idx]
                                val isSelected = year == selectedYear
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFF00D4AA) else Color.Transparent)
                                        .padding(vertical = 8.dp)
                                        .clickable(enabled = !isYearDisabled) {
                                            if (!isYearDisabled) onYearSelected(year)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = year.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) Color.Black else if (isYearDisabled) Color.Gray else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Month Selector
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMonthDisabled) Color(0xFF1A1A1A) else Color(
                            0xFF2A2A2A
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            items(months.size) { idx ->
                                val month = months[idx]
                                val isSelected = month == selectedMonth
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFF00D4AA) else Color.Transparent)
                                        .padding(vertical = 8.dp)
                                        .clickable(enabled = !isMonthDisabled) {
                                            if (!isMonthDisabled) onMonthSelected(month)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = AppUtils.getMonthName(month),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) Color.Black else if (isMonthDisabled) Color.Gray else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
