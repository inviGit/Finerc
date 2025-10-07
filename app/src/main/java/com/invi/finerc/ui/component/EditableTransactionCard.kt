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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invi.finerc.data.entity.TransactionItemEntity
import com.invi.finerc.domain.models.TransactionItemModel
import com.invi.finerc.domain.models.TransactionUiModel
import java.text.NumberFormat
import java.util.Locale


@Composable
fun EditableTransactionCard(
    transaction: TransactionUiModel,
    excludedItems: Set<Long>,
    onItemToggle: (Long, Boolean) -> Unit,
    onRemoveTransaction: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    var expanded by remember { mutableStateOf(false) }
    val items = transaction.transactionItems.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Transaction Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transaction Icon
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
                                brush = Brush.linearGradient(transaction.category.colors),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transaction.category.icon,
                            contentDescription = transaction.category.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Transaction Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.place.ifEmpty { "Online Transaction" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note ?: "NA",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Amount & Remove Button
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = format.format(transaction.amount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (transaction.txnType.name == "SENT") Color(0xFFEF4444) else Color(
                            0xFF00D4AA
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onRemoveTransaction,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable Items Section
            if (items.isNotEmpty()) {
                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )

                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(300, easing = LinearOutSlowInEasing),
                    label = "rotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .background(Color(0xFF0F0F0F))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${items.size} item${if (items.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0F0F))
                            .padding(vertical = 4.dp)
                    ) {
                        items.forEachIndexed { index, item ->
                            EditableTransactionItemRow(
                                item = item,
                                isExcluded = excludedItems.contains(item.itemId),
                                onToggle = { isExcluded ->
                                    onItemToggle(item.itemId, isExcluded)
                                },
                                format = format,
                                isLast = index == items.size - 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditableTransactionItemRow(
    item: TransactionItemModel,
    isExcluded: Boolean,
    onToggle: (Boolean) -> Unit,
    format: NumberFormat,
    isLast: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExcluded) }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = !isExcluded,
                onCheckedChange = { checked -> onToggle(!checked) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00D4AA),
                    uncheckedColor = Color.Gray,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Item Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isExcluded) Color.Gray else Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isExcluded) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order: ${item.orderId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount
            Text(
                text = format.format(item.totalOwed),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isExcluded) Color.Gray else Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (!isLast) {
            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.15f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
