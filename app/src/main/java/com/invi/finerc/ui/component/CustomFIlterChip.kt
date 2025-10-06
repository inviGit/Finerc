package com.invi.finerc.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun CustomFilterChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String,
    selectedContainerColor: Color = Color(0xFF00D4AA),
    unselectedContainerColor: Color = Color(0xFF1A1A1A),
    selectedLabelColor: Color = Color.Black,
    unselectedLabelColor: Color = Color.White,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = selectedContainerColor,
            containerColor = unselectedContainerColor,
            selectedLabelColor = selectedLabelColor,
            labelColor = unselectedLabelColor
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(width = 0.dp, color = Color.Transparent),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .height(36.dp)
    )
}