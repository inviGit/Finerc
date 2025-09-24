package com.invi.finerc

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import com.invi.finerc.common.SmsScanner
import com.invi.finerc.transaction.SmsRepository
import com.invi.finerc.transaction.SmsTransactionDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@Preview
fun SettingsScreen() {
    val context = LocalContext.current
    val db = remember { SmsTransactionDatabase.getInstance(context) }
    val repo = remember { SmsRepository(db.smsTransactionDao(), db.collectionDao()) }
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }

    fun handleScanSms() {
        scope.launch {
            isScanning = true
            scanResult = null
            try {
                val scanner = SmsScanner(context)
                val messages = scanner.scanSmsMessages()
                repo.saveNewMessages(messages)

                // Convert to JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(messages)

                // Save to Downloads
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "sms_transactions_${
                    SimpleDateFormat(
                        "yyyy-MM-dd_HH-mm-ss",
                        Locale.getDefault()
                    ).format(Date())
                }.json"
                val file = File(downloadsDir, fileName)
                FileWriter(file).use { writer ->
                    writer.write(jsonString)
                }

                scanResult =
                    "Scan complete: ${messages.size} messages saved. JSON exported to Downloads."
            } catch (e: Exception) {
                scanResult = "Scan failed: ${e.message}"
            }
            isScanning = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            HeaderSection1()
        }

        // Profile Section
        item {
            ProfileSection()
        }

        // Settings Categories
        item {
            SettingsCategory(
                title = "Account",
                settings = listOf(
                    SettingItem(
                        "Personal Information",
                        Icons.Default.Person,
                        "Manage your profile"
                    ),
                    SettingItem("Security", Icons.Default.Security, "Password and authentication"),
                    SettingItem(
                        "Notifications",
                        Icons.Default.Notifications,
                        "Manage notifications"
                    )
                )
            )
        }

        item {
            SettingsCategory(
                title = "App Settings",
                settings = listOf(
                    SettingItem("Theme", Icons.Default.Palette, "Dark/Light mode"),
                    SettingItem("Language", Icons.Default.Language, "English"),
                    SettingItem("Currency", Icons.Default.AttachMoney, "INR")
                )
            )
        }

        item {
            SettingsCategory(
                title = "Data & Privacy",
                settings = listOf(
                    SettingItem("SCAN SMS", Icons.Default.Download, "Scan device transactions"),
                    SettingItem("Export Data", Icons.Default.Download, "Download your data"),
                    SettingItem(
                        "Privacy Policy",
                        Icons.Default.Info,
                        "Read our privacy policy"
                    ), // Changed from Policy
                    SettingItem(
                        "Terms of Service",
                        Icons.Default.Description,
                        "Read terms of service"
                    )
                ),
                onSettingClick = { setting ->
                    if (setting.title == "SCAN SMS") handleScanSms()
                }
            )
        }

        if (isScanning) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = Color(0xFF00D4AA))
                }
            }
        }
        if (scanResult != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(scanResult!!, color = Color.White)
                }
            }
        }

        item {
            SettingsCategory(
                title = "Support",
                settings = listOf(
                    SettingItem("Help Center", Icons.Default.Help, "Get help and support"),
                    SettingItem(
                        "Contact Us",
                        Icons.Default.Email,
                        "Reach out to us"
                    ), // Changed from ContactSupport
                    SettingItem("About", Icons.Default.Info, "App version 1.0.0")
                )
            )
        }

        // Logout Button
        item {
            LogoutButton()
        }
    }
}

@Composable
fun HeaderSection1() {
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
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ProfileSection() {
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
            // Profile Avatar
            Card(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF00D4AA)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Profile Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "john.doe@example.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Edit Button
            IconButton(
                onClick = { /* Handle edit profile */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color(0xFF00D4AA)
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
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
        // Icon
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2A2A2A)
            )
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

        // Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
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

        // Arrow
        Icon(
            imageVector = Icons.Default.ArrowForward, // Changed from ArrowForwardIos
            contentDescription = "Navigate",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun LogoutButton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEF4444),
                            Color(0xFFDC2626)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp, // Changed from Logout
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String
)