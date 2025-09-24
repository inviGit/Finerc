package com.invi.finerc

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.models.getCategoryByPlace
import com.invi.finerc.transaction.DatabaseHelper
import com.invi.finerc.transaction.SmsRepository
import com.invi.finerc.transaction.SmsTransactionDatabase
import com.invi.finerc.ui.component.CategoryCard
import com.invi.finerc.ui.component.CompactStatCard
import com.invi.finerc.ui.component.DateSelector
import com.invi.finerc.ui.component.MinimalBarChartSynced
import com.invi.finerc.ui.component.StatCard
import com.invi.finerc.ui.component.StatsSection
import com.invi.finerc.ui.component.TextDivider
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

enum class FilterType {
    DAY, WEEK, MONTH, YEAR
}

@Composable
@Preview
fun ReportScreen() {
    val context = LocalContext.current
    val db = SmsTransactionDatabase.getInstance(context)
    val repo = SmsRepository(db.smsTransactionDao(), db.collectionDao())
    val dbHelper = DatabaseHelper(context)

    var smsList by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            Log.d("OverviewScreen", "Starting database initialization...")
            val initSuccess = dbHelper.initializeDatabase()
            Log.d("OverviewScreen", "Database initialization success: $initSuccess")

            smsList = repo.getAllMessages().filter { it.transactionType != SMSType.DRAFT }
        } catch (e: Exception) {
            Log.e("OverviewScreen", "Error loading messages", e)
        }
    }

    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }

    // Chart filter toggle (1M, 1Y)
    val filterOptions = listOf("1M", "1Y")
    var selectedFilter by remember { mutableStateOf(0) }

    // Determine if selectors should be disabled
    val isMonthDisabled = selectedFilter == 1
    val isYearDisabled = false

    // Filter smsList by selected year and month
    val filteredSmsList = remember(smsList, selectedYear, selectedMonth, selectedFilter) {
        when (selectedFilter) {
            0 -> smsList.filter { msg ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = msg.date
                val msgYear = cal.get(java.util.Calendar.YEAR)
                val msgMonth = cal.get(java.util.Calendar.MONTH) + 1
                msgYear == selectedYear && msgMonth == selectedMonth
            }

            1 -> smsList.filter { msg ->
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = msg.date
                val msgYear = cal.get(java.util.Calendar.YEAR)
                msgYear == selectedYear
            }

            else -> smsList
        }
    }

    // Flat data for chart (filtered messages)
    val dateFormat =
        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
    val allChartData = remember(filteredSmsList) {
        filteredSmsList.groupBy { it.date }.map { (date, txns) ->
                dateFormat.format(java.util.Date(date)) to txns.sumOf { it.amount }
            }.sortedBy { it.first }
    }

    val now = System.currentTimeMillis()
    val filteredChartData = remember(allChartData, selectedFilter, now) {
        val filterMillis = when (selectedFilter) {
            0 -> 31L * 24 * 60 * 60 * 1000 // 1M
            1 -> 365L * 24 * 60 * 60 * 1000 // 1Y
            2 -> Long.MAX_VALUE // ALL
            else -> Long.MAX_VALUE
        }
        val cutoff = now - filterMillis
        allChartData.filter { (label, _) ->
            try {
                val parsedDate = dateFormat.parse(label)?.time ?: now
                parsedDate >= cutoff
            } catch (e: Exception) {
                true
            }
        }
    }

    // Top 5 categories by spend with percentage
    val topCategories = remember(filteredSmsList) {
        val total = filteredSmsList.sumOf { it.amount }
        filteredSmsList.groupBy { it.category }.map { (cat, txns) ->
                val sum = txns.sumOf { it.amount }
                val percent = if (total > 0) sum / total else 0.0
                Triple(cat, sum, percent)
            }.sortedByDescending { it.second }.take(5)
    }

    // Top 5 places by spend with percentage
    val topPlaces = remember(filteredSmsList) {
        val total = filteredSmsList.sumOf { it.amount }
        filteredSmsList.groupBy { it.place }.map { (place, txns) ->
                val sum = txns.sumOf { it.amount }
                val percent = if (total > 0) sum / total else 0.0
                Triple(place, sum, percent)
            }.filter { it.first.isNotBlank() }.sortedByDescending { it.second }.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item { HeaderSection() }

        // Year and Month Selector
        item {
            DateSelector(
                selectedYear = selectedYear,
                onYearSelected = { if (!isYearDisabled) selectedYear = it },
                selectedMonth = selectedMonth,
                onMonthSelected = { if (!isMonthDisabled) selectedMonth = it },
                isMonthDisabled = isMonthDisabled,
                isYearDisabled = isYearDisabled
            )
        }

        // Chart Section
        item {
            MinimalBarChartSynced(
                heading = "Spending Trend",
                data = filteredChartData,
                onFilterChange = { selectedFilter = (selectedFilter + 1) % filterOptions.size },
                filterOptions = filterOptions,
                selectedFilter = selectedFilter,
                modifier = Modifier
                    .width(380.dp)
                    .height(220.dp)
            )
        }

        // Stats Cards
        item {
            StatsSection(groupedData = filteredChartData)
        }

        // Top Categories
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Top Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    topCategories.forEach { (category, amount, percent) ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Middle: Category Name
                            Column(
                                modifier = Modifier
                                    .weight(1f),
//                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Progress bar for percentage
                                LinearProgressIndicator(
                                    progress = { (percent / 100f).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = category.color,
                                    trackColor = category.color.copy(alpha = 0.2f)
                                )
                            }

                            // Right: Total Amount and Percentage
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "₹$amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${String.format("%.1f", percent)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Places
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Top Places",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    topPlaces.forEach { (place, amount, percent) ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Middle: Category Name
                            Column(
                                modifier = Modifier
                                    .weight(1f),
//                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = place,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Progress bar for percentage
                                LinearProgressIndicator(
                                    progress = { (percent / 100f).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = getCategoryByPlace(place).color,
                                    trackColor = getCategoryByPlace(place).color.copy(alpha = 0.2f)
                                )
                            }

                            // Right: Total Amount and Percentage
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "₹$amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${String.format("%.1f", percent)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
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
fun HeaderSection() {
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
                            Color(0xFF6366F1), Color(0xFF8B5CF6)
                        )
                    ), shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Spending Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your spending patterns",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatsSection(groupedData: List<Pair<String, Double>>) {
    val totalSpent = groupedData.sumOf { it.second }
    val avgSpent = if (groupedData.isNotEmpty()) totalSpent / groupedData.size else 0.0
    val maxSpent = groupedData.maxOfOrNull { it.second } ?: 0.0
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsSection(totalSpent = totalSpent,
            avgTransaction = avgSpent,
            format = format)
        CompactStatCard(title = "Highest Spending",
            value = "₹${String.format("%.0f", maxSpent)}",
            icon = Icons.Default.TrendingUp,
            gradient = listOf(Color(0xFFDD5E89), Color(0xFFF7BB97)),
            modifier = Modifier.fillMaxWidth())
    }
}

fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "Jan"
    }
}