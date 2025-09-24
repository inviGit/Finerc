package com.invi.finerc.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun MinimalBarChartSynced(
    heading: String,
    data: List<Pair<String, Double>>,
    onFilterChange: () -> Unit,
    filterOptions: List<String>,
    selectedFilter: Int,
    modifier: Modifier = Modifier
        .width(380.dp)
        .height(220.dp)
) {
    val computedMax = data.maxOfOrNull { it.second } ?: 0.0
    val maxValue = if (computedMax > 0.0) computedMax else 1.0
    val barWidth = 32.dp
    val barSpacing = 16.dp

    // Calculate total width needed for all bars
//    val totalBarsWidth = (data.size * barWidthPx) + ((data.size - 1) * barSpacingPx)
    val totalBarsWidth = (data.size * (barWidth + barSpacing)) - barSpacing
    val minWidth = 380.dp
    val scrollableWidth = maxOf(totalBarsWidth, minWidth)

    // Shared scroll state for synchronized scrolling
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = onFilterChange,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4AA)),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    filterOptions[selectedFilter],
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val yLabels = listOf(
                    maxValue.toInt().toString(),
                    (maxValue * 0.75).toInt().toString(),
                    (maxValue * 0.5).toInt().toString(),
                    (maxValue * 0.25).toInt().toString(),
                    "0"
                )

                yLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Chart area with synchronized scrolling
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Chart bars area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                ) {
                    Row(
                        modifier = Modifier
                            .width(scrollableWidth)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(barSpacing)
                    ) {
                        data.forEach { (_, value) ->
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .fillMaxHeight()
                            ) {
                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    if (size.height <= 0f) return@Canvas
                                    val chartHeight = size.height
                                    val safeValue = if (value.isFinite() && value > 0.0) value else 0.0
                                    val rawBarHeight =
                                        ((safeValue / maxValue.toFloat()) * chartHeight).toFloat()
                                    // Ensure non-zero height to avoid equal startY and endY in gradient
                                    val barHeight = rawBarHeight.coerceAtLeast(1f)
                                    val barTop: Float = (chartHeight - barHeight)

                                    // Gradient colors
                                    val gradientColors = listOf(
                                        Color(0xFF6366F1), // Indigo
                                        Color(0xFF8B5CF6)  // Purple
                                    )

                                    // Create gradient brush
                                    val brush = Brush.verticalGradient(
                                        colors = gradientColors,
                                        startY = barTop,
                                        endY = barTop + barHeight
                                    )

                                    // Draw rounded rectangle bar
                                    drawRoundRect(
                                        brush = brush,
                                        topLeft = Offset(0f, barTop),
                                        size = Size(size.width, barHeight),
                                        cornerRadius = CornerRadius(16.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }

                // X-axis labels with synchronized scrolling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(scrollState) // Same scroll state
                ) {
                    Row(
                        modifier = Modifier.width(scrollableWidth),
                        horizontalArrangement = Arrangement.spacedBy(barSpacing)
                    ) {
                        data.forEach { (label, _) ->
                            Box(
                                modifier = Modifier.width(barWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.take(2), // Show only first 2 characters
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalBarChart(
    data: List<Pair<String, Double>>, modifier: Modifier = Modifier
        .width(380.dp)
        .height(220.dp)
) {
    val computedMax = data.maxOfOrNull { it.second } ?: 0.0
    val maxValue = if (computedMax > 0.0) computedMax else 1.0
    val barWidth = 32.dp
    val barSpacing = 16.dp

    Row(
        modifier = modifier
    ) {
        // Y-axis labels
        Column(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val yLabels = listOf(
                maxValue.toInt().toString(),
                (maxValue * 0.75).toInt().toString(),
                (maxValue * 0.5).toInt().toString(),
                (maxValue * 0.25).toInt().toString(),
                "0"
            )

            yLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }

        // Chart area
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (size.height <= 0f) return@Canvas
                    val totalWidth = size.width
                    val chartHeight = size.height
                    val barWidthPx = barWidth.toPx()
                    val barSpacingPx = barSpacing.toPx()
                    val totalBarsWidth = (data.size * barWidthPx) + ((data.size - 1) * barSpacingPx)
                    val startX = (totalWidth - totalBarsWidth) / 2f

                    // Gradient colors
                    val gradientColors = listOf(
                        Color(0xFF6366F1), // Indigo
                        Color(0xFF8B5CF6)  // Purple
                    )

                    data.forEachIndexed { index, (_, value) ->
                        val safeValue = if (value.isFinite() && value > 0.0) value else 0.0
                        val rawBarHeight = ((safeValue / maxValue.toFloat()) * chartHeight).toFloat()
                        // Ensure non-zero height to avoid equal startY and endY in gradient
                        val barHeight = rawBarHeight.coerceAtLeast(1f)
                        val barLeft = startX + (index * (barWidthPx + barSpacingPx))
                        val barTop: Float = (chartHeight - barHeight)

                        // Create gradient brush
                        val brush = Brush.verticalGradient(
                            colors = gradientColors,
                            startY = barTop,
                            endY = barTop + barHeight
                        )

                        // Draw rounded rectangle bar
                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidthPx, barHeight),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                }
            }

            // X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { (label, _) ->
                    Text(
                        text = label.take(2), // Show only first 2 characters
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
