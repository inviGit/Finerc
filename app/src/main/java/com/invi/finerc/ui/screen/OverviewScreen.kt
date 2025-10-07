package com.invi.finerc.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.invi.finerc.common.AppUtils
import com.invi.finerc.domain.models.TransactionUiModel
import com.invi.finerc.ui.component.CustomFilterChip
import com.invi.finerc.ui.component.DateDivider
import com.invi.finerc.ui.component.DetailedHeader
import com.invi.finerc.ui.component.EmptyStateCard
import com.invi.finerc.ui.component.ErrorCard
import com.invi.finerc.ui.component.LoadingCard
import com.invi.finerc.ui.component.StatsSection
import com.invi.finerc.ui.component.TransactionCard
import com.invi.finerc.ui.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

@Composable
@Preview
fun OverviewScreen(
    navController: NavController? = null, viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterOptions = listOf("1W", "1M", "1Y")
    var selectedFilter by remember { mutableIntStateOf(2) }
    val filteredList = remember(uiState.transactions, selectedFilter) {
        applyFilterToTransactions(uiState.transactions, selectedFilter)
    }
    val txnCount = filteredList.size
    val totalAmount = filteredList.sumOf { it.amount }

    LaunchedEffect(uiState.transactions) {
        try {
            Log.d("OverviewScreen", "Successfully loaded transactions")
        } catch (e: Exception) {
            Log.e("OverviewScreen", "Error loading transactions", e)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                DetailedHeader(
                    totalAmount = totalAmount,
                    messageCount = txnCount,
                    isLoading = uiState.isLoading
                )
            }

            // Filter Chips
            item {
                OverviewFilterSection(
                    filterOptions = filterOptions,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    navigateToAllTransaction = { navController?.navigate("allTransactions") })
            }

            // Quick Stats Cards
            item {
                val totalSpent = filteredList.sumOf { it.amount }
                val avgTransaction =
                    if (filteredList.isNotEmpty()) totalSpent / filteredList.size else 0.0
                val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

                StatsSection(
                    totalSpent = totalSpent, avgTransaction = avgTransaction, format = format
                )
            }

            // Transactions List
            if (uiState.isLoading) {
                item {
                    LoadingCard()
                }
            } else if (uiState.error != null) {
                item {
                    ErrorCard(error = "Error: ${uiState.error}")
                }
            } else if (filteredList.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                // Group transactions by date (always up-to-date)
                val groupedTransactions = filteredList.groupBy { message ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = message.txnDate
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }.toList().sortedByDescending { it.first }

                Log.d("OverviewScreen", "Grouped transactions: ${groupedTransactions.size}")
                if (groupedTransactions.isEmpty() && filteredList.isNotEmpty()) {
                    // Fallback: show all transactions without grouping
                    Log.d("OverviewScreen", "Fallback: showing all transactions without grouping")
                    items(filteredList) { message ->
                        TransactionCard(message = message, onClick = {
                            navController?.navigate("transactionDetail?transactionId=${message.id}")
                        })
                    }
                } else {
                    groupedTransactions.forEach { (date, transactions) ->
                        // Date divider
                        item {
                            DateDivider(date = date)
                        }

                        // Transactions for this date
                        items(transactions) { message ->
                            TransactionCard(message = message, onClick = {
                                navController?.navigate("transactionDetail?transactionId=${message.id}")
                            })
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController?.navigate("editTransaction?transactionId=new")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF00D4AA)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction", tint = Color.Black)
        }
    }
}

fun applyFilterToTransactions(
    transactions: List<TransactionUiModel>,
    selectedFilter: Int
): List<TransactionUiModel> {
    val cal = Calendar.getInstance()
    val now = System.currentTimeMillis()
    cal.timeInMillis = now
    val zone = ZoneId.systemDefault()
    val startMillis = when (selectedFilter) {
        0 -> AppUtils.cutoffMillis(
            AppUtils.CutoffKind.WeekFromMonday, now, zone
        )          // 1W: from last Monday
        1 -> AppUtils.cutoffMillis(
            AppUtils.CutoffKind.MonthFromDay(1), now, zone
        )         // 1M: from 1st of month
        2 -> AppUtils.cutoffMillis(
            AppUtils.CutoffKind.YearFromJan1, now, zone
        )            // 1Y: from Jan 1
        else -> 0L
    }
    val filtered =
        if (startMillis == 0L) transactions else transactions.filter { it.txnDate >= startMillis }
    Log.d(
        "OverviewScreen",
        "Filtered ${transactions.size} transactions to ${filtered.size} transactions. IDs: ${filtered.map { it.id }} Dates: ${filtered.map { it.txnDate }}"
    )
    return filtered
}

@Composable
fun OverviewFilterSection(
    filterOptions: List<String>,
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit,
    navigateToAllTransaction: () -> Unit?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)
        ) {
            items(filterOptions.size) { index ->
                val isSelected = selectedFilter == index
                CustomFilterChip(
                    isSelected = isSelected,
                    onClick = { onFilterSelected(index) },
                    label = filterOptions[index]
                )
            }
        }
        Button(
            onClick = { navigateToAllTransaction() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D4AA)),
            modifier = Modifier.height(40.dp)
        ) {
            Text("View All", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

