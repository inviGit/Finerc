package com.invi.finerc.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invi.finerc.domain.models.TransactionItemModel
import com.invi.finerc.domain.models.TransactionUiModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionCard(
    message: TransactionUiModel,
    items: List<TransactionItemModel> = emptyList(),
    onClick: (() -> Unit)? = null
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Original Card Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (items.isEmpty()) {
                            onClick?.invoke()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Card(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(message.category.colors),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = message.category.icon,
                            contentDescription = message.category.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = message.place.ifEmpty { "Online Transaction" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = message.note ?: "NA",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Amount
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = format.format(message.amount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (message.txnType.name == "SENT") Color(0xFFEF4444) else Color(
                            0xFF00D4AA
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat(
                            "dd MMM",
                            Locale.getDefault()
                        ).format(Date(message.txnDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Expandable Bottom Section - Only show if items exist
            if (items.isNotEmpty()) {
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )

                // Expand/Collapse Button Section
                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing
                    ),
                    label = "rotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .background(Color(0xFF0F0F0F))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${items.size} item${if (items.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }

                // Expandable Items List
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(
                        animationSpec = tween(300)
                    ) + expandVertically(
                        animationSpec = tween(300)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(300)
                    ) + shrinkVertically(
                        animationSpec = tween(300)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F0F))
                    ) {
                        items.forEach { item ->
                            TransactionItemRow(
                                item = item,
                                format = format
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    item: TransactionItemModel,
    format: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Order: ${item.orderId} • Qty: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(item.orderDate)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // Amount
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = format.format(item.totalOwed),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = item.orderStatus,
                style = MaterialTheme.typography.bodySmall,
                color = when (item.orderStatus.lowercase()) {
                    "delivered" -> Color(0xFF00D4AA)
                    "pending" -> Color(0xFFFFA500)
                    "cancelled" -> Color(0xFFEF4444)
                    else -> Color.Gray
                },
                fontWeight = FontWeight.Medium
            )
        }
    }

    HorizontalDivider(
        color = Color.Gray.copy(alpha = 0.2f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 16.dp)
    )
}
