package com.invi.finerc.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Payment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat

@Composable
fun StatsSection(
    totalSpent: Double,
    avgTransaction: Double,
    format: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Spent",
            value = format.format(totalSpent),
            icon = Icons.Default.Payment, // Changed from Payments
            gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Avg Transaction",
            value = format.format(avgTransaction),
            icon = Icons.Default.Analytics,
            gradient = listOf(Color(0xFFEF4444), Color(0xFFF97316)),
            modifier = Modifier.weight(1f)
        )
    }
}