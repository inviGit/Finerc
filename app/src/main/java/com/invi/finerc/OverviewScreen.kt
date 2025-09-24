package com.invi.finerc

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.transaction.DatabaseHelper
import com.invi.finerc.transaction.SmsRepository
import com.invi.finerc.transaction.SmsTransactionDatabase
import com.invi.finerc.ui.component.CustomFilterChip
import com.invi.finerc.ui.component.DateDivider
import com.invi.finerc.ui.component.EmptyStateCard
import com.invi.finerc.ui.component.ErrorCard
import com.invi.finerc.ui.component.LoadingCard
import com.invi.finerc.ui.component.StatsSection
import com.invi.finerc.ui.component.StatItem
import com.invi.finerc.ui.component.TransactionCard
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

@Composable
@Preview
fun OverviewScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val db = SmsTransactionDatabase.getInstance(context)
    val repo = SmsRepository(db.smsTransactionDao(), db.collectionDao())
    val dbHelper = DatabaseHelper(context)

    var smsList by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var messageCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            Log.d("OverviewScreen", "Starting database initialization...")
            val initSuccess = dbHelper.initializeDatabase()
            Log.d("OverviewScreen", "Database initialization success: $initSuccess")

            val now = LocalDate.now()
            val year = now.year
            val monthZeroBased = now.monthValue

            smsList = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                repo.getMessagesForYear(year)
                    .filter { it.transactionType != SMSType.DRAFT }
            }
            messageCount = smsList.size
            isLoading = false
            Log.d("OverviewScreen", "Successfully loaded $messageCount messages")
        } catch (e: Exception) {
            error = e.message
            isLoading = false
            Log.e("OverviewScreen", "Error loading messages", e)
        }
    }

    val filterOptions = listOf("1W", "1M", "1Y")
    var selectedFilter by remember { mutableIntStateOf(2) }
    val now = System.currentTimeMillis()

    // Log filter changes
    LaunchedEffect(selectedFilter) {
        Log.d("OverviewScreen", "Filter changed to: ${filterOptions[selectedFilter]}")
    }

    // Calculate filtered list based on selected filter
    val filteredList = remember(smsList, selectedFilter, now) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        val startMillis = when (selectedFilter) {
            0 -> { // 1W: from last Monday
                // Move to Monday of current week; if today is before Monday in this locale, adjust accordingly
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            1 -> { // 1M: from first date of current month
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            2 -> { // 1Y: from Jan 1 of current year
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }
        val filtered = if (startMillis == 0L) smsList else smsList.filter { it.date >= startMillis }
        Log.d("OverviewScreen", "Filtered ${smsList.size} messages to ${filtered.size} messages. IDs: ${filtered.map { it.id }} Dates: ${filtered.map { it.date }}")
        filtered
    }

    val totalAmount = filteredList.sumOf { it.amount }
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

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
                HeaderSection(
                    totalAmount = totalAmount,
                    messageCount = messageCount,
                    isLoading = isLoading
                )
            }

            // Filter Chips
            item {
                OverviewFilterSection(
                    filterOptions = filterOptions,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    navigateToAllTransaction = { navController?.navigate("allTransactions") }
                )
            }

            // Quick Stats Cards
            item {
                val totalSpent = filteredList.sumOf { it.amount }
                val avgTransaction = if (filteredList.isNotEmpty()) totalSpent / filteredList.size else 0.0
                val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

                StatsSection(
                    totalSpent = totalSpent,
                    avgTransaction = avgTransaction,
                    format = format)
            }

//            // Recent Transactions Header
//            item {
//                Text(
//                    text = "Recent Transactions",
//                    style = MaterialTheme.typography.headlineSmall,
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
//                )
//            }

            // Transactions List
            if (isLoading) {
                item {
                    LoadingCard()
                }
            } else if (error != null) {
                item {
                    ErrorCard(error = error!!)
                }
            } else if (filteredList.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                // Group transactions by date (always up-to-date)
                val groupedTransactions = filteredList
                    .groupBy { message ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = message.date
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }
                    .toList()
                    .sortedByDescending { it.first }

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

@Composable
fun HeaderSection(
    totalAmount: Double,
    messageCount: Int,
    isLoading: Boolean
) {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00D4AA),
                            Color(0xFF00B894)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Spending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (isLoading) "Loading..." else format.format(totalAmount),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalance, // Changed from AccountBalanceWallet
                        contentDescription = "Wallet",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Transactions",
                        value = messageCount.toString(),
                        icon = Icons.Default.Receipt
                    )
                    StatItem(
                        label = "This Month",
                        value = "₹${String.format("%.0f", totalAmount)}",
                        icon = Icons.Default.TrendingUp
                    )
                }
            }
        }
    }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
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

