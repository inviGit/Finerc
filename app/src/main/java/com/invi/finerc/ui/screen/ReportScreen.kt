package com.invi.finerc.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.invi.finerc.common.AppUtils
import com.invi.finerc.domain.models.TransactionUiModel
import com.invi.finerc.domain.models.getCategoryByPlace
import com.invi.finerc.ui.component.CompactStatCard
import com.invi.finerc.ui.component.DateSelector
import com.invi.finerc.ui.component.MinimalBarChartSynced
import com.invi.finerc.ui.component.StatsSection
import com.invi.finerc.ui.component.TitleHeader
import com.invi.finerc.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
@Preview
fun ReportScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
    val filterOptions = listOf("1M", "1Y")
    var selectedFilter by remember { mutableStateOf(0) }
    val isMonthDisabled = selectedFilter == 1
    val isYearDisabled = false

    LaunchedEffect(uiState.transactions) {
        try {
            Log.d("ReportScreen", "Successfully loaded transactions")
        } catch (e: Exception) {
            Log.e("ReportScreen", "Error loading messages", e)
        }
    }

    // Filter smsList by selected year and month
    val filteredTransactionList =
        remember(uiState.transactions, selectedYear, selectedMonth, selectedFilter) {
            val transactions = uiState.transactions
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val zone = ZoneId.systemDefault()

            val filtered = when {
                // Filter by specific month and year (if both are selected)
//            selectedYear != null && selectedMonth != null -> {
//                transactions.filter { transaction ->
//                    cal.timeInMillis = transaction.txnDate
//                    val txnYear = cal.get(Calendar.YEAR)
//                    val txnMonth = cal.get(Calendar.MONTH) // 0-based (0 = January)
//                    txnYear == selectedYear && txnMonth == selectedMonth
//                }
//            }
//
//            // Filter by year only (if only year is selected)
//            selectedYear != null && selectedMonth == null -> {
//                transactions.filter { transaction ->
//                    cal.timeInMillis = transaction.txnDate
//                    cal.get(Calendar.YEAR) == selectedYear
//                }
//            }

                // Filter by time period (1M or 1Y)
                selectedFilter == 0 || selectedFilter == 1 -> {
                    val startMillis = when (selectedFilter) {
                        0 -> AppUtils.cutoffMillis(AppUtils.CutoffKind.MonthFromDay(1), now, zone)
                        1 -> AppUtils.cutoffMillis(AppUtils.CutoffKind.YearFromJan1, now, zone)
                        else -> 0L
                    }
                    transactions.filter { it.txnDate >= startMillis }
                }

                // No filter applied - show all
                else -> transactions
            }
            Log.d(
                "ReportScreen",
                "Filtered ${transactions.size} transactions to ${filtered.size} messages. IDs: ${filtered.map { it.id }} Dates: ${filtered.map { it.txnDate }}"
            )
            filtered
        }

    // Flat data for chart (filtered messages)
    val allChartData = remember(filteredTransactionList) {
        filteredTransactionList.groupBy { it.txnDate }.map { (date, txns) ->
            SimpleDateFormat(
                "dd MMM", Locale.getDefault()
            ).format(Date(date)) to txns.sumOf { it.amount }
        }.sortedBy { it.first }
    }

    // Top 5 categories by spend with percentage
    val topCategories = remember(filteredTransactionList) {
        val total = filteredTransactionList.sumOf { it.amount }
        filteredTransactionList.groupBy { it.category }.map { (cat, txns) ->
            val sum = txns.sumOf { it.amount }
            val percent = if (total > 0) sum / total else 0.0
            Triple(cat, sum, percent)
        }.sortedByDescending { it.second }.take(5)
    }

    // Top 5 places by spend with percentage
    val topPlaces = remember(filteredTransactionList) {
        val total = filteredTransactionList.sumOf { it.amount }
        filteredTransactionList.groupBy { it.place }.map { (place, txns) ->
            val sum = txns.sumOf { it.amount }
            val percent = if (total > 0) sum / total else 0.0
            Triple(place ?: "", sum, percent)
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
        item { TitleHeader("Spending Analytics", "Track your spending patterns") }

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
                data = allChartData,
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
            ReportStatsSection(filteredTransactionList)
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
                                    text = category?.displayName.orEmpty(),
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
                                    color = category?.color ?: Color(0xFFEF4444),
                                    trackColor = category?.color?.copy(alpha = 0.2f) ?: Color(
                                        0xFFEF4444
                                    ).copy(alpha = 0.2f)
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
                                    text = place.orEmpty(),
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
                                    color = getCategoryByPlace(place.orEmpty()).color,
                                    trackColor = getCategoryByPlace(place.orEmpty()).color.copy(
                                        alpha = 0.2f
                                    )
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
fun ReportStatsSection(filteredList: List<TransactionUiModel>) {
    val totalSpent = filteredList.sumOf { it.amount }
    val avgTransaction = if (filteredList.isNotEmpty()) totalSpent / filteredList.size else 0.0
    val maxSpent = filteredList.maxOfOrNull { it.amount } ?: 0.0
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsSection(
            totalSpent = totalSpent,
            avgTransaction = avgTransaction,
            format = format
        )
        CompactStatCard(
            title = "Highest Spending",
            value = "₹${String.format("%.0f", maxSpent)}",
            icon = Icons.Default.TrendingUp,
            gradient = listOf(Color(0xFFDD5E89), Color(0xFFF7BB97)),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

