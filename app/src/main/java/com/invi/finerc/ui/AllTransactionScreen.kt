package com.invi.finerc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.invi.finerc.StatsSection
import com.invi.finerc.models.Category
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.ui.component.DateDivider
import com.invi.finerc.ui.component.EmptyStateCard
import com.invi.finerc.ui.component.ErrorCard
import com.invi.finerc.ui.component.LoadingCard
import com.invi.finerc.ui.component.TransactionCard
import com.invi.finerc.ui.component.StatCard
import com.invi.finerc.ui.component.StatsSection
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    navController: NavController?
) {
    val context = LocalContext.current
    val db = remember { com.invi.finerc.transaction.SmsTransactionDatabase.getInstance(context) }
    val repo = remember { com.invi.finerc.transaction.SmsRepository(db.smsTransactionDao(), db.collectionDao()) }

    // Sorting options
    val sortOptions = listOf(
        "Date Descending" to Icons.Filled.ArrowDropUp,
        "Date Ascending" to Icons.Filled.ArrowDropDown,
        "Amount Descending" to Icons.Filled.ArrowDropUp,
        "Amount Ascending" to Icons.Filled.ArrowDropDown
    )

    // Loading / error / data
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var allTransactions by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }

    // Initial fetch (load everything; filter locally)
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            // If repository only has month-based methods, consider a broad range or a new DAO method for "all".
            // For now, read a reasonable window, or expose repo.getAllMessages()
            allTransactions = repo.getAllMessages()
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    // Derived facets from allTransactions
    val allYears by remember(allTransactions) {
        mutableStateOf(
            allTransactions.map {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.get(Calendar.YEAR)
            }.distinct().sortedDescending()
        )
    }
    // Month values as 1..12 paired to labels
    val allMonths by remember(allTransactions) {
        mutableStateOf(
            allTransactions.map {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.get(Calendar.MONTH) + 1 // 1-based
            }.distinct().sorted()
        )
    }
    val allCategories by remember(allTransactions) {
        mutableStateOf(allTransactions.map { it.category }.distinct())
    }
    val allPlaces by remember(allTransactions) {
        mutableStateOf(
            allTransactions.map { it.place }.filter { it.isNotBlank() }.distinct()
            .sorted()
        )
    }
    val allBanks by remember(allTransactions) {
        mutableStateOf(
            allTransactions.map { it.bankName }.filter { it.isNotBlank() }.distinct()
            .sorted()
        )
    }

    // Sort/filter UI state
    var isSortSheetOpen by rememberSaveable { mutableStateOf(false) }
    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }
    var selectedSortIndex by rememberSaveable { mutableIntStateOf(0) }

    var selectedYears by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var selectedMonths by rememberSaveable { mutableStateOf(setOf<Int>()) } // 1..12
    var selectedCategories by rememberSaveable { mutableStateOf(setOf<Category>()) }
    var selectedPlaces by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedBanks by rememberSaveable { mutableStateOf(setOf<String>()) }
    var amountLimit by rememberSaveable { mutableFloatStateOf(0f) }
    // Optional search inside sheets (CRED-like nicety for long lists)
    var placeQuery by rememberSaveable { mutableStateOf("") }
    var bankQuery by rememberSaveable { mutableStateOf("") }

    // 1) Filtering: compute from state so it always updates
    val filteredList by remember(
        allTransactions,
        selectedYears,
        selectedMonths,
        selectedCategories,
        selectedPlaces,
        selectedBanks,
        amountLimit
    ) {
        mutableStateOf(
            allTransactions.asSequence().filter { msg ->
                val cal = Calendar.getInstance().apply { timeInMillis = msg.date }
                val yearOk =
                    selectedYears.isEmpty() || selectedYears.contains(cal.get(Calendar.YEAR))
                val monthOk =
                    selectedMonths.isEmpty() || selectedMonths.contains(cal.get(Calendar.MONTH) + 1)
                val categoryOk =
                    selectedCategories.isEmpty() || selectedCategories.contains(msg.category)
                val placeOk = selectedPlaces.isEmpty() || selectedPlaces.any {
                    it.equals(
                        msg.place, ignoreCase = true
                    )
                }
                val bankOk = selectedBanks.isEmpty() || selectedBanks.any {
                    it.equals(
                        msg.bankName, ignoreCase = true
                    )
                }
                val amountOk = if (amountLimit > 0f) msg.amount <= amountLimit else true
                yearOk && monthOk && categoryOk && placeOk && bankOk && amountOk
            }.toList()
        )
    }

    // 2) Sorting after filtering (unchanged intent, ensure it reads filteredList + index)
    val displayedList by remember(filteredList, selectedSortIndex) {
        mutableStateOf(
            when (selectedSortIndex) {
            0 -> filteredList.sortedByDescending { it.date }
            1 -> filteredList.sortedBy { it.date }
            2 -> filteredList.sortedByDescending { it.amount }
            3 -> filteredList.sortedBy { it.amount }
            else -> filteredList
        })
    }

    val sortLabel = sortOptions[selectedSortIndex].first
    val sortIcon: ImageVector = sortOptions[selectedSortIndex].second

    val sortSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(16.dp),
        topBar = {
            Column(
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                StatsSection(
                    groupedData = filteredList.groupBy {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it.date
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.timeInMillis
                    }.map { (date, txns) ->
                        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                        format.format(date) to txns.sumOf { it.amount }
                    }
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { isSortSheetOpen = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = sortIcon,
                            contentDescription = "Sort",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = sortLabel.split(" ")[0])
                    }

                    Button(
                        onClick = { isFilterSheetOpen = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Filter")
                    }
                }
            }
        }) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Add StatsSection at the top ---
//            item {
//                StatsSection(
//                    groupedData = filteredList.groupBy {
//                        val calendar = Calendar.getInstance()
//                        calendar.timeInMillis = it.date
//                        calendar.set(Calendar.HOUR_OF_DAY, 0)
//                        calendar.set(Calendar.MINUTE, 0)
//                        calendar.set(Calendar.SECOND, 0)
//                        calendar.set(Calendar.MILLISECOND, 0)
//                        calendar.timeInMillis
//                    }.map { (date, txns) ->
//                        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
//                        format.format(date) to txns.sumOf { it.amount }
//                    }
//                )
//            }

            if (isLoading) {
                item { LoadingCard() }
            } else if (error != null) {
                item { ErrorCard(error = error!!) }
            } else if (displayedList.isEmpty()) {
                item { EmptyStateCard() }
            } else {
                items(displayedList, key = { it.id ?: "${it.address}-${it.date}" }) { message ->
                    TransactionCard(message = message, onClick = {
                        navController?.navigate("transactionDetail?transactionId=${message.id}")
                    })
                }
//                val groupedTransactions = displayedList.groupBy { message ->
//                        val calendar = Calendar.getInstance()
//                        calendar.timeInMillis = message.date
//                        calendar.set(Calendar.HOUR_OF_DAY, 0)
//                        calendar.set(Calendar.MINUTE, 0)
//                        calendar.set(Calendar.SECOND, 0)
//                        calendar.set(Calendar.MILLISECOND, 0)
//                        calendar.timeInMillis
//                    }.toList();
//
//                if (groupedTransactions.isEmpty() && displayedList.isNotEmpty()) {
//                    items(displayedList, key = { it.id ?: "${it.address}-${it.date}" }) { message ->
//                        TransactionCard(message = message, onClick = {
//                            navController?.navigate("transactionDetail?transactionId=${message.id}")
//                        })
//                    }
//                } else {
//                    groupedTransactions.forEach { (date, transactions) ->
//                        item { DateDivider(date = date) }
//                        items(
//                            transactions,
//                            key = { it.id ?: "${it.address}-${it.date}" }) { message ->
//                            TransactionCard(message = message, onClick = {
//                                navController?.navigate("transactionDetail?transactionId=${message.id}")
//                            })
//                        }
//                    }
//                }
            }
        }
    }

    // Sort Sheet
    if (isSortSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSortSheetOpen = false }, sheetState = sortSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Sort by", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                sortOptions.forEachIndexed { index, pair ->
                    val (label, icon) = pair
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSortIndex = index
                            isSortSheetOpen = false
                        }
                        .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.weight(1f))
                        RadioButton(
                            selected = selectedSortIndex == index, onClick = {
                                selectedSortIndex = index
                                isSortSheetOpen = false
                            })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // Filter Sheet (facets derived from data)
    if (isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false }, sheetState = filterSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Filters", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                // Year (multi-select)
                if (allYears.isNotEmpty()) {
                    Text("Year", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allYears.forEach { y ->
                            FilterChip(selected = selectedYears.contains(y), onClick = {
                                selectedYears =
                                    if (selectedYears.contains(y)) selectedYears - y else selectedYears + y
                            }, label = { Text("$y") })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Month (multi-select)
                if (allMonths.isNotEmpty()) {
                    Text("Month", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allMonths.forEach { m ->
                            FilterChip(selected = selectedMonths.contains(m), onClick = {
                                selectedMonths =
                                    if (selectedMonths.contains(m)) selectedMonths - m else selectedMonths + m
                            }, label = { Text(monthLabel(m)) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Category (multi-select, fix selection)
                if (allCategories.isNotEmpty()) {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allCategories) { c ->
                            AssistChip(onClick = {
                                selectedCategories =
                                    if (selectedCategories.contains(c)) selectedCategories - c else selectedCategories + c
                            }, label = { Text(c.name) }, leadingIcon = {
                                if (selectedCategories.contains(c)) {
                                    Icon(Icons.Filled.FilterList, contentDescription = null)
                                }
                            })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Places (multi-select)
                if (allPlaces.isNotEmpty()) {
                    Text("Places", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = placeQuery,
                        onValueChange = { placeQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search place") },
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(Modifier.height(8.dp))
                    val placesShown = remember(allPlaces, placeQuery) {
                        if (placeQuery.isBlank()) allPlaces
                        else allPlaces.filter { it.contains(placeQuery, ignoreCase = true) }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        placesShown.forEach { p ->
                            FilterChip(selected = selectedPlaces.contains(p), onClick = {
                                selectedPlaces =
                                    if (selectedPlaces.contains(p)) selectedPlaces - p else selectedPlaces + p
                            }, label = { Text(p) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Bank (multi-select)
                if (allBanks.isNotEmpty()) {
                    Text("Bank", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = bankQuery,
                        onValueChange = { bankQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search bank") },
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    Spacer(Modifier.height(8.dp))
                    val banksShown = remember(allBanks, bankQuery) {
                        if (bankQuery.isBlank()) allBanks
                        else allBanks.filter { it.contains(bankQuery, ignoreCase = true) }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        banksShown.forEach { b ->
                            FilterChip(selected = selectedBanks.contains(b), onClick = {
                                selectedBanks =
                                    if (selectedBanks.contains(b)) selectedBanks - b else selectedBanks + b
                            }, label = { Text(b) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Amount limit (RangeSlider could be used for min..max)
                Text("Amount limit", style = MaterialTheme.typography.labelLarge)
                val maxAmount = remember(allTransactions) {
                    (allTransactions.maxOfOrNull { it.amount } ?: 0.0).toFloat()
                        .coerceAtLeast(1000f)
                }
                Slider(
                    value = amountLimit.coerceIn(0f, maxAmount),
                    onValueChange = { amountLimit = it },
                    valueRange = 0f..maxAmount,
                    steps = 10
                )
                Text("≤ ₹${amountLimit.toInt()}", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f), onClick = {
                            selectedYears = setOf()
                            selectedMonths = setOf()
                            selectedCategories = setOf()
                            selectedPlaces = setOf()
                            selectedBanks = setOf()
                            amountLimit = 0f
                            placeQuery = ""
                            bankQuery = ""
                        }) { Text("Clear") }
                    Button(
                        modifier = Modifier.weight(1f), onClick = {
                            // Filters already apply reactively; just close.
                            isFilterSheetOpen = false
                        }) { Text("Apply") }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// Helper: 1..12 to short month label
private fun monthLabel(m: Int): String {
    return when (m) {
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
        else -> m.toString()
    }
}



