package com.invi.finerc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invi.finerc.models.Category
import java.text.NumberFormat
import java.util.Locale

@Composable
@Preview
fun CategoryScreen() {
    val categories = listOf(
        Category.FOOD,
        Category.SHOPPING,
        Category.TRAVEL,
        Category.ENTERTAINMENT,
        Category.HEALTHCARE,
        Category.EDUCATION,
        Category.LOAN_EMI,
        Category.INVESTMENT
    )

    // Sample spending data for each category
    val categorySpending = mapOf(
        Category.FOOD to 12500.0,
        Category.SHOPPING to 8900.0,
        Category.TRAVEL to 15600.0,
        Category.ENTERTAINMENT to 4200.0,
        Category.HEALTHCARE to 3200.0,
        Category.EDUCATION to 8000.0,
        Category.LOAN_EMI to 15000.0,
        Category.INVESTMENT to 25000.0
    )

    val totalSpending = categorySpending.values.sum()
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            HeaderSection(totalSpending = totalSpending)
        }

        // Category Cards
        items(categories) { category ->
            CategoryCard(
                category = category,
                spending = categorySpending[category] ?: 0.0,
                totalSpending = totalSpending,
                format = format
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HeaderSection(totalSpending: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00D4AA),
                            Color(0xFF00B894)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See where your money goes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Spending",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                                .format(totalSpending),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = "Pie Chart",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    spending: Double,
    totalSpending: Double,
    format: NumberFormat
) {
    val percentage = if (totalSpending > 0) (spending / totalSpending) * 100 else 0.0
    val categoryColors = getCategoryColors(category)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(categoryColors),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = category.displayName,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Category Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format("%.1f", percentage)}% of total spending",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = format.format(spending),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${String.format("%.1f", percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
//UTILITIES
fun getCategoryColors(category: Category): List<Color> {
    return when (category) {
        Category.FOOD -> listOf(Color(0xFFEF4444), Color(0xFFF97316))
        Category.SHOPPING -> listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        Category.TRAVEL -> listOf(Color(0xFF00D4AA), Color(0xFF00B894))
        Category.ENTERTAINMENT -> listOf(Color(0xFFEC4899), Color(0xFFF59E0B))
        Category.HEALTHCARE -> listOf(Color(0xFF10B981), Color(0xFF059669))
        Category.EDUCATION -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        Category.LOAN_EMI -> listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
        Category.INVESTMENT -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        else -> listOf(Color(0xFF6B7280), Color(0xFF4B5563))
    }
}

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.FOOD -> Icons.Default.Restaurant
        Category.SHOPPING -> Icons.Default.ShoppingCart
        Category.TRAVEL -> Icons.Default.DirectionsCar
        Category.ENTERTAINMENT -> Icons.Default.Movie
        Category.HEALTHCARE -> Icons.Default.LocalHospital
        Category.EDUCATION -> Icons.Default.School
        Category.LOAN_EMI -> Icons.Default.Receipt
        Category.INVESTMENT -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }
} 