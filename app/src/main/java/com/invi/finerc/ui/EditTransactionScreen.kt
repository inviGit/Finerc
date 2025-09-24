package com.invi.finerc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.invi.finerc.models.Category
import com.invi.finerc.models.SMSType
import com.invi.finerc.models.SmsMessageModel
import com.invi.finerc.transaction.SmsRepository
import com.invi.finerc.transaction.SmsTransactionDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(transactionId: Long, navController: NavHostController) {

    val context = LocalContext.current
    val db = remember { SmsTransactionDatabase.getInstance(context) }
    val repo = remember { SmsRepository(db.smsTransactionDao(), db.collectionDao()) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(transactionId != 0L) }
    var error by remember { mutableStateOf<String?>(null) }

    // State for all fields
    var place by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.FOOD) }
    var transactionType by remember { mutableStateOf(SMSType.SENT) }
    var bankName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    // Dropdown state
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    // Date/Time picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDateMillis by remember { mutableStateOf(dateMillis) }
    var tempHour by remember {
        mutableStateOf(java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }
            .get(java.util.Calendar.HOUR_OF_DAY))
    }
    var tempMinute by remember {
        mutableStateOf(java.util.Calendar.getInstance().apply { timeInMillis = dateMillis }
            .get(java.util.Calendar.MINUTE))
    }

    val dateFormat =
        remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault()) }

    val dateText = remember(dateMillis) { dateFormat.format(java.util.Date(dateMillis)) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = tempDateMillis)
    val timePickerState = rememberTimePickerState(
        initialHour = tempHour, initialMinute = tempMinute
    )
    var originalPlace by remember { mutableStateOf<String?>(null) }
    var originalCategory by remember { mutableStateOf<Category?>(null) }
    var loadedMessage by remember { mutableStateOf<SmsMessageModel?>(null) }
    var showBulkUpdateDialog by remember { mutableStateOf(false) }
    var bulkUpdateSelected: Set<Long> by remember { mutableStateOf<Set<Long>>(setOf()) }
    var bulkUpdateTransactions by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }

    // Form validation
    val isFormValid =
        place.isNotBlank() && bankName.isNotBlank() && amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0

    LaunchedEffect(transactionId) {
        if (transactionId != 0L) {
            isLoading = true
            try {
                val msg = repo.getMessage(transactionId)
                if (msg != null) {
                    loadedMessage = msg
                    place = msg.place
                    originalPlace = msg.place
                    category = msg.category
                    originalCategory = msg.category
                    transactionType = msg.transactionType
                    bankName = msg.bankName
                    amount = msg.amount.toString()
                    dateMillis = msg.date
                    note = msg.note
                } else {
                    error = "Transaction not found"
                }
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00D4AA),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Loading transaction...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        } else if (error != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Oops! Something went wrong",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    error!!,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D4AA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go Back", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with back button
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                                    text = if (transactionId == 0L) "Add Transaction" else "Edit Transaction",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (transactionId == 0L) "Create a new transaction" else "Modify transaction details",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Date/Time Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Transaction Date & Time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            OutlinedTextField(
                                value = dateText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("When did this transaction occur?") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF00D4AA)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            Icons.Default.EditCalendar,
                                            contentDescription = "Edit Date",
                                            tint = Color(0xFF00D4AA)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D4AA),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFF00D4AA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFF00D4AA),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Transaction Details Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Transaction Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedTextField(
                                value = place,
                                onValueChange = { place = it },
                                label = { Text("Where was this transaction?") },
                                placeholder = { Text("e.g., McDonald's, Amazon, etc.") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Place,
                                        contentDescription = "Place",
                                        tint = Color(0xFF00D4AA)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D4AA),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFF00D4AA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFF00D4AA),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                isError = place.isBlank()
                            )
                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    if (it.all { c -> c.isDigit() || c == '.' }) amount = it
                                },
                                label = { Text("Transaction Amount") },
                                placeholder = { Text("0.00") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.CurrencyRupee,
                                        contentDescription = "Amount",
                                        tint = Color(0xFF00D4AA)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D4AA),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFF00D4AA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFF00D4AA),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                isError = amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0
                            )
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank or Payment Method") },
                                placeholder = { Text("e.g., HDFC Bank, PayTM, etc.") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.AccountBalance,
                                        contentDescription = "Bank",
                                        tint = Color(0xFF00D4AA)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D4AA),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFF00D4AA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFF00D4AA),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                isError = bankName.isBlank()
                            )

                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                label = { Text("Note") },
                                placeholder = { Text("Optional note or description") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.EditCalendar,
                                        contentDescription = "Note",
                                        tint = Color(0xFF00D4AA)
                                    )
                                },
                                singleLine = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00D4AA),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = Color(0xFF00D4AA),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color(0xFF00D4AA),
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Categories Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Classification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                                OutlinedTextField(
                                    value = category.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Transaction Category") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Category,
                                            contentDescription = null,
                                            tint = Color(0xFF00D4AA)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            if (categoryExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFF00D4AA)
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00D4AA),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        cursorColor = Color(0xFF00D4AA),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFF00D4AA),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    modifier = Modifier.background(Color(0xFF2A2D35))
                                ) {
                                    Category.entries.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                            Text(
                                                cat.displayName, color = Color.White
                                            )
                                        }, onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }, modifier = Modifier.background(
                                            if (cat == category) Color(0xFF00D4AA).copy(alpha = 0.1f)
                                            else Color.Transparent
                                        )
                                        )
                                    }
                                }
                            }
                            // Transaction Type Dropdown
                            ExposedDropdownMenuBox(
                                expanded = typeExpanded,
                                onExpandedChange = { typeExpanded = !typeExpanded }) {
                                OutlinedTextField(
                                    value = transactionType.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Transaction Type") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.SwapHoriz,
                                            contentDescription = null,
                                            tint = Color(0xFF00D4AA)
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            if (typeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFF00D4AA)
                                        )
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00D4AA),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        cursorColor = Color(0xFF00D4AA),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedLabelColor = Color(0xFF00D4AA),
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = typeExpanded,
                                    onDismissRequest = { typeExpanded = false },
                                    modifier = Modifier.background(Color(0xFF2A2D35))
                                ) {
                                    SMSType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                            Text(
                                                type.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() },
                                                color = Color.White
                                            )
                                        }, onClick = {
                                            transactionType = type
                                            typeExpanded = false
                                        }, modifier = Modifier.background(
                                            if (type == transactionType) Color(0xFF00D4AA).copy(
                                                alpha = 0.1f
                                            )
                                            else Color.Transparent
                                        )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                // Save Button
                item {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .height(56.dp)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Button(
                            onClick = {
                                // If place or category changed, show bulk update dialog
                                if (transactionId != 0L && originalPlace != null && originalCategory != null && (place != originalPlace || category != originalCategory)) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            showBulkUpdateDialog = true
                                        } catch (e: Exception) {
                                            error = e.message
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            val base = loadedMessage
                                            val msgToSave = if (base != null) {
                                                base.copy(
                                                    date = dateMillis,
                                                    place = place,
                                                    transactionType = transactionType,
                                                    bankName = bankName,
                                                    category = category,
                                                    amount = amount.toDoubleOrNull() ?: base.amount,
                                                    note = note
                                                )
                                            } else {
                                                val id = if (transactionId == 0L) null else transactionId
                                                SmsMessageModel(
                                                    id = id,
                                                    address = "",
                                                    body = "",
                                                    date = dateMillis,
                                                    place = place,
                                                    transactionType = transactionType,
                                                    bankName = bankName,
                                                    category = category,
                                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                                    percentage = 0.0,
                                                    note = note
                                                )
                                            }
                                            repo.saveMessages(listOf(msgToSave))
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            error = e.message
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth(),
//                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00D4AA),
                                disabledContainerColor = Color(0xFF00D4AA).copy(alpha = 0.3f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Icon(
                                    if (isLoading) Icons.Default.HourglassEmpty else Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isLoading) "Saving..." else "Save Transaction",
                                    color = Color.Black,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                    }

                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = {
                tempDateMillis = datePickerState.selectedDateMillis ?: tempDateMillis
                dateMillis = tempDateMillis
                showDatePicker = false
                showTimePicker = true
            }) {
                Text("Continue", color = Color(0xFF00D4AA))
            }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }, title = {
            Text("Select Date", color = Color.White, fontWeight = FontWeight.SemiBold)
        }, text = {
            DatePicker(state = datePickerState)
        }, containerColor = Color(0xFF2A2D35), shape = RoundedCornerShape(16.dp)
        )
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false }, confirmButton = {
            TextButton(onClick = {
                val cal = Calendar.getInstance()
                cal.timeInMillis = dateMillis
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                dateMillis = cal.timeInMillis
                showTimePicker = false
            }) {
                Text("Done", color = Color(0xFF00D4AA))
            }
        }, dismissButton = {
            TextButton(onClick = { showTimePicker = false }) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }, title = {
            Text("Select Time", color = Color.White, fontWeight = FontWeight.SemiBold)
        }, text = {
            TimePicker(state = timePickerState)
        }, containerColor = Color(0xFF2A2D35), shape = RoundedCornerShape(16.dp)
        )
    }

    // Show EnhancedBulkUpdateDialog when needed
    EnhancedBulkUpdateDialog(
        showBulkUpdateDialog = showBulkUpdateDialog,
        onDismiss = { showBulkUpdateDialog = false },
        onConfirm = { selectedTxns, selectedMonth, selectedYear ->
            // Handle the bulk update logic here
            scope.launch {
                isLoading = true
                try {
                    val updatedTxns = selectedTxns.map { txn ->
                        txn.copy(
                            place = place, category = category
                        )
                    }
                    repo.saveMessages(updatedTxns)
                    showBulkUpdateDialog = false
                    navController.popBackStack()
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        },
        onSaveCurrentOnly = {
            scope.launch {
                isLoading = true
                try {
                    val base = loadedMessage
                    val msgToSave = if (base != null) {
                        base.copy(
                            date = dateMillis,
                            place = place,
                            transactionType = transactionType,
                            bankName = bankName,
                            category = category,
                            amount = amount.toDoubleOrNull() ?: base.amount,
                            note = note
                        )
                    } else {
                        val id = if (transactionId == 0L) null else transactionId
                        SmsMessageModel(
                            id = id,
                            address = "",
                            body = "",
                            date = dateMillis,
                            place = place,
                            transactionType = transactionType,
                            bankName = bankName,
                            category = category,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            percentage = 0.0,
                            note = note
                        )
                    }
                    repo.saveMessages(listOf(msgToSave))
                    showBulkUpdateDialog = false
                    navController.popBackStack()
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        },
        originalPlace = originalPlace ?: "",
        originalCategory = originalCategory ?: Category.OTHERS,
        newPlace = place
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBulkUpdateDialog(
    showBulkUpdateDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<SmsMessageModel>, Int, Int) -> Unit,
    onSaveCurrentOnly: () -> Unit,
    originalPlace: String,
    originalCategory: Category,
    newPlace: String,
) {
    val context = LocalContext.current
    val db = remember { SmsTransactionDatabase.getInstance(context) }
    val repo = remember { SmsRepository(db.smsTransactionDao(), db.collectionDao()) }
    val scope = rememberCoroutineScope()

    var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var bulkUpdateTransactions by remember { mutableStateOf<List<SmsMessageModel>>(emptyList()) }
    var bulkUpdateSelected by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isLoadingTransactions by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val months = listOf(
        1 to "January",
        2 to "February",
        3 to "March",
        4 to "April",
        5 to "May",
        6 to "June",
        7 to "July",
        8 to "August",
        9 to "September",
        10 to "October",
        11 to "November",
        12 to "December"
    )

    val years = (2020..LocalDate.now().year + 1).toList()

    fun reload() {
        scope.launch {
            isLoadingTransactions = true
            try {
                // Compute start and end for selected month/year
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, selectedYear)
                cal.set(Calendar.MONTH, selectedMonth - 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis

                val txns = repo.getAllMessages()
                    .filter { it.place == originalPlace && it.category == originalCategory }
                    .filter { it.date in start until end }

                bulkUpdateTransactions = txns
                bulkUpdateSelected = txns.mapNotNull { it.id }.toSet()
            } catch (e: Exception) {
                bulkUpdateTransactions = emptyList()
                bulkUpdateSelected = emptySet()
            } finally {
                isLoadingTransactions = false
            }
        }
    }

    // Initial load when dialog becomes visible
    LaunchedEffect(showBulkUpdateDialog) {
        if (showBulkUpdateDialog) {
            reload()
        }
    }

    // Reload when month/year changes and dialog is visible
    LaunchedEffect(selectedMonth, selectedYear) {
        if (showBulkUpdateDialog) {
            reload()
        }
    }

    if (showBulkUpdateDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, confirmButton = {
            TextButton(
                onClick = {
                    val selectedTransactions = bulkUpdateTransactions.filter {
                        bulkUpdateSelected.contains(it.id)
                    }
                    onConfirm(selectedTransactions, selectedMonth, selectedYear)
                }, enabled = bulkUpdateSelected.isNotEmpty()
            ) {
                Text(
                    "Update ${bulkUpdateSelected.size} Transaction${if (bulkUpdateSelected.size == 1) "" else "s"}",
                    color = if (bulkUpdateSelected.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }, dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSaveCurrentOnly) {
                    Text("Save current")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }, title = {
            Text(
                "Bulk Update Similar Transactions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }, text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Update summary card
                Card(
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Update Details",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("From:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(
                                originalPlace.ifEmpty { "No Place" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("To:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(
                                newPlace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Category:", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            Text(
                                originalCategory.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Month and Year selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedMonth,
                        onExpandedChange = { expandedMonth = !expandedMonth },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = months.find { it.first == selectedMonth }?.second
                            ?: "Select Month",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMonth,
                            onDismissRequest = { expandedMonth = false }) {
                            months.forEach { (monthValue, monthName) ->
                                DropdownMenuItem(text = { Text(monthName) }, onClick = {
                                    selectedMonth = monthValue
                                    expandedMonth = false
                                })
                            }
                        }
                    }

                    // Year Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedYear,
                        onExpandedChange = { expandedYear = !expandedYear },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
                            years.reversed().forEach { year ->
                                DropdownMenuItem(text = { Text(year.toString()) }, onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                })
                            }
                        }
                    }
                }

                // Transactions list
                if (isLoadingTransactions) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text(
                                "Loading transactions...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else if (bulkUpdateTransactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    "No similar transactions found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "for ${months.find { it.first == selectedMonth }?.second} $selectedYear",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Select all/none header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Found ${bulkUpdateTransactions.size} similar transaction${if (bulkUpdateTransactions.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        bulkUpdateSelected =
                                            if (bulkUpdateSelected.size == bulkUpdateTransactions.size) {
                                                emptySet()
                                            } else {
                                                bulkUpdateTransactions.mapNotNull { it.id }.toSet()
                                            }
                                    }) {
                                    Text(
                                        if (bulkUpdateSelected.size == bulkUpdateTransactions.size) "Deselect All" else "Select All",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Transaction list
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 240.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(bulkUpdateTransactions) { txn ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            bulkUpdateSelected =
                                                (if (bulkUpdateSelected.contains(txn.id)) {
                                                    bulkUpdateSelected - txn.id
                                                } else {
                                                    bulkUpdateSelected + txn.id
                                                }) as Set<Long>
                                        }, colors = CardDefaults.cardColors(
                                        containerColor = if (bulkUpdateSelected.contains(txn.id)) MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.3f
                                        )
                                        else MaterialTheme.colorScheme.surface
                                    ), elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (bulkUpdateSelected.contains(txn.id)) 2.dp else 1.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = bulkUpdateSelected.contains(txn.id),
                                            onCheckedChange = { checked ->
                                                bulkUpdateSelected = (if (checked) {
                                                    bulkUpdateSelected + txn.id
                                                } else {
                                                    bulkUpdateSelected - txn.id
                                                }) as Set<Long>
                                            })

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    txn.bankName.ifEmpty { "Unknown Bank" },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    "₹${String.format("%.0f", txn.amount)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (txn.transactionType == SMSType.SENT) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    txn.place.ifEmpty { "No Place" },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.7f
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    SimpleDateFormat(
                                                        "dd MMM", Locale.getDefault()
                                                    ).format(Date(txn.date)),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.5f
                                                    )
                                                )
                                            }

                                            Text(
                                                text = txn.transactionType.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (txn.transactionType == SMSType.SENT) MaterialTheme.colorScheme.error.copy(
                                                    alpha = 0.7f
                                                )
                                                else MaterialTheme.colorScheme.primary.copy(
                                                    alpha = 0.7f
                                                ),
                                                modifier = Modifier
                                                    .background(
                                                        color = if (txn.transactionType == SMSType.SENT) MaterialTheme.colorScheme.error.copy(
                                                            alpha = 0.1f
                                                        )
                                                        else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.1f
                                                        ), shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, modifier = Modifier.widthIn(max = 480.dp)
        )
    }
}