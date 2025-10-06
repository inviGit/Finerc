package com.invi.finerc.ui.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.GsonBuilder
import com.invi.finerc.common.SmsScanner
import com.invi.finerc.ui.component.PdfPasswordDialog
import com.invi.finerc.ui.component.TitleHeader
import com.invi.finerc.ui.viewmodel.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe PDF processing state from ViewModel
    val pdfProcessingState by viewModel.pdfProcessingState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var scanResult by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pendingExcelUri by remember { mutableStateOf<Uri?>(null) }
    val excelProcessingState by viewModel.excelProcessingState.collectAsState()

    val excelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            pendingExcelUri = uri
        }
    )

    LaunchedEffect(pendingExcelUri) {
        pendingExcelUri?.let { uri ->
            // Call ViewModel method to parse and process excel
            viewModel.processExcelFile(uri)
            pendingExcelUri = null
        }
    }

    fun handleScanSms() {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { scanResult = null }

            try {
                val scanner = SmsScanner(context)
                val messages = scanner.scanSmsMessages()

                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(messages)

                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "sms_transactions_${
                    SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
                }.json"
                val file = File(downloadsDir, fileName)
                FileWriter(file).use { writer -> writer.write(jsonString) }

                withContext(Dispatchers.Main) {
                    scanResult = "✅ Scan complete: ${messages.size} messages saved to Downloads."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    scanResult = "❌ Scan failed: ${e.message}"
                }
            }
        }
    }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri ?: return@rememberLauncherForActivityResult
            pendingPdfUri = uri
            showPasswordDialog = true
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { granted ->
            if (granted.values.all { it }) {
                pdfPicker.launch(arrayOf("application/pdf"))
            } else {
                scanResult = "❌ Permission denied"
            }
        }
    )

    fun handleUploadPdf() {
        if (Build.VERSION.SDK_INT < 33) {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            pdfPicker.launch(arrayOf("application/pdf"))
        }
    }

    fun handleUploadExcel() {
        // MIME type for Excel files (XLSX)
        excelPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    }

    // Password Dialog
    if (showPasswordDialog && pendingPdfUri != null) {
        PdfPasswordDialog(
            onDismiss = {
                showPasswordDialog = false
                pendingPdfUri = null
            },
            onConfirm = { password ->
                showPasswordDialog = false
                // Process in ViewModel - survives screen changes
                viewModel.processPdfStatement(pendingPdfUri!!, password)
                pendingPdfUri = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TitleHeader("Settings", "Customize your experience")
        }

        item {
            SettingsCategory(
                title = "Data Management",
                settings = listOf(
                    SettingItem("Scan SMS", Icons.Default.Email, "Import transactions from SMS"),
                    SettingItem("Upload Bank PDF", Icons.Default.FileUpload, "Import bank statement"),
                    SettingItem("Upload Excel", Icons.Default.FileUpload, "Import order items from Excel"),  // New
                    SettingItem("Export Data", Icons.Default.Download, "Download your data")
                ),
                onSettingClick = { setting ->
                    when (setting.title) {
                        "Scan SMS" -> handleScanSms()
                        "Upload Bank PDF" -> handleUploadPdf()
                        "Upload Excel" -> handleUploadExcel()
                    }
                }
            )
        }

        // PDF Processing Status (from ViewModel state)
        when (val state = pdfProcessingState) {
            is TransactionViewModel.PdfProcessingState.Processing -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00D4AA))

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = state.progress,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Processing in background...",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(onClick = { viewModel.cancelPdfProcessing() }) {
                                Text("Cancel", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
            is TransactionViewModel.PdfProcessingState.Success -> {
                item {
                    ResultCard(
                        message = state.message,
                        isSuccess = true,
                        onDismiss = { viewModel.resetPdfProcessingState() }
                    )
                }
            }
            is TransactionViewModel.PdfProcessingState.Error -> {
                item {
                    ResultCard(
                        message = state.message,
                        isSuccess = false,
                        onDismiss = { viewModel.resetPdfProcessingState() }
                    )
                }
            }
            is TransactionViewModel.PdfProcessingState.Cancelled -> {
                item {
                    ResultCard(
                        message = "⚠️ PDF processing cancelled",
                        isSuccess = false,
                        onDismiss = { viewModel.resetPdfProcessingState() }
                    )
                }
            }
            TransactionViewModel.PdfProcessingState.Idle -> { /* No card */ }
        }

        when (val state = excelProcessingState) {
            is TransactionViewModel.ExcelProcessingState.Processing -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00D4AA))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.progress,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            is TransactionViewModel.ExcelProcessingState.Success -> {
                item {
                    ResultCard(
                        message = state.message,
                        isSuccess = true,
                        onDismiss = { viewModel._excelProcessingState.value = TransactionViewModel.ExcelProcessingState.Idle }
                    )
                }
            }
            is TransactionViewModel.ExcelProcessingState.Error -> {
                item {
                    ResultCard(
                        message = state.message,
                        isSuccess = false,
                        onDismiss = { viewModel._excelProcessingState.value = TransactionViewModel.ExcelProcessingState.Idle }
                    )
                }
            }
            TransactionViewModel.ExcelProcessingState.Idle -> { /* No card */ }
        }

        // SMS Scan Result
        if (scanResult != null) {
            item {
                ResultCard(
                    message = scanResult!!,
                    isSuccess = scanResult!!.contains("✅"),
                    onDismiss = { scanResult = null }
                )
            }
        }
    }
}

@Composable
fun ResultCard(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) Color(0xFF1A4D2E) else Color(0xFF4D1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    settings: List<SettingItem>,
    onSettingClick: (SettingItem) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            settings.forEach { setting ->
                SettingRow(setting = setting, onClick = { onSettingClick(setting) })
            }
        }
    }
}


@Composable
fun SettingRow(setting: SettingItem, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = setting.title,
                    tint = Color(0xFF00D4AA),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = setting.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String
)
