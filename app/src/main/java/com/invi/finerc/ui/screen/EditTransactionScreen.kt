package com.invi.finerc.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.invi.finerc.domain.models.Category
import com.invi.finerc.domain.models.TransactionStatus
import com.invi.finerc.domain.models.TransactionType
import com.invi.finerc.ui.component.DropdownSelector
import com.invi.finerc.ui.component.TimePickerDialogSample
import com.invi.finerc.ui.viewmodel.EditTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//@Composable
//fun CollectionDetailScreen(
//    collectionId: Long,
//    navController: NavController?,
//    viewModel: CollectionDetailViewModel = hiltViewModel()
//) {
//    val collection by viewModel.collectionWithTransaction.collectAsState()

@Composable
fun EditTransactionScreen(
    transactionId: Long,
    navController: NavHostController,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsState()

    var place: String? by remember { mutableStateOf("") }
    var category: Category? by remember { mutableStateOf(Category.FOOD) }
    var txnType: TransactionType? by remember { mutableStateOf(TransactionType.DEBIT) }
    var bankName: String? by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var txnDate by remember { mutableStateOf(0L) }
    var description: String? by remember { mutableStateOf("") }
    var note: String? by remember { mutableStateOf("") }

    LaunchedEffect(transaction) {
        transaction?.let {
            place = it.place
            category = it.category
            txnType = it.txnType
            bankName = it.bankName
            amount = it.amount.toString()
            txnDate = it.txnDate
            description = it.description
            note = it.note
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(value = place ?: "", onValueChange = { place = it }, label = { Text("Place") })

        DropdownSelector(
            label = "Category",
            selectedValue = category,
            options = Category.values().toList(),
            onValueChange = { category = it },
            toDisplayString = { it?.name.orEmpty() }
        )

        DropdownSelector(
            label = "Transaction Type",
            selectedValue = txnType,
            options = TransactionType.values().toList(),
            onValueChange = { txnType = it },
            toDisplayString = { it?.name.orEmpty() }
        )

        OutlinedTextField(value = bankName.orEmpty(), onValueChange = { bankName = it }, label = { Text("Bank Name") })

        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        // Time picker button (shows current selected time)
        val formattedTime = remember(txnDate) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(txnDate))
        }
        Text(text = "Transaction Time: $formattedTime")
        TimePickerDialogSample (initialTimeMillis = txnDate, onTimeSelected = { txnDate = it })

        OutlinedTextField(value = description.orEmpty(), onValueChange = { description = it }, label = { Text("Description") })

        OutlinedTextField(value = note.orEmpty(), onValueChange = { note = it }, label = { Text("Note") })

        Button(onClick = {
            val amountDouble = amount.toDoubleOrNull() ?: 0.0
            viewModel.saveTransaction(
                place.orEmpty(),
                category ?: Category.OTHERS,
                txnType ?: TransactionType.DEBIT,
                bankName.orEmpty(),
                amountDouble,
                txnDate,
                description.orEmpty(),
                note.orEmpty(),
                status = TransactionStatus.ACTIVE
            )
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}