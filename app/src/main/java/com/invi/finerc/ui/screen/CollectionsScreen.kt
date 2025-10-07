package com.invi.finerc.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.invi.finerc.data.entity.CollectionEntity
import com.invi.finerc.domain.models.CollectionModel
import com.invi.finerc.ui.component.CollectionCard
import com.invi.finerc.ui.viewmodel.CollectionsViewModel

@Composable
fun CollectionsScreen(
    navController: NavController? = null,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val collections by viewModel.collections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var collectionToDelete by remember { mutableStateOf<CollectionModel?>(null) }
    var showFabMenu by remember { mutableStateOf(false) }

    Box(Modifier
        .fillMaxSize()
        .background(Color(0xFF0A0A0A))) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Collections",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth(), Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color(0xFF00D4AA)
                            )
                        }
                    }
                } else if (collections.isEmpty()) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Group,
                                    null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No collections found",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Add a new collection to get started!",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(collections) { collection ->
                        CollectionCard(
                            collection = collection,
                            onClick = { navController?.navigate("collectionDetail/${collection.id}") },
                            onEdit = { navController?.navigate("collectionEdit/${collection.id}") },
                            onDelete = {
                                collections.find { it.id == collection.id }?.let {
                                    collectionToDelete = it
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
        // FAB menu
        Box(Modifier
            .fillMaxSize()
            .padding(16.dp), Alignment.BottomEnd) {
            if (showFabMenu) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 70.dp)
                ) {
                    Button(
                        onClick = { showAddGroupDialog = true },
                        colors = ButtonDefaults.buttonColors(Color.White)
                    ) {
                        Text("Add New Collection", color = Color.Black)
                    }
                }
            }
            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu },
                containerColor = Color(0xFF00D4AA)
            ) {
                Icon(
                    if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                    "Add Menu",
                    tint = Color.Black
                )
            }
        }
        if (showAddGroupDialog) {
            AddCollectionDialog(
                onDismiss = { showAddGroupDialog = false },
                onConfirm = { name ->
                    viewModel.createCollection(name) {
                        showAddGroupDialog = false
                    }
                }
            )
        }
        if (showDeleteDialog && collectionToDelete != null) {
            DeleteCollectionDialog(
                collection = collectionToDelete!!,
                onDismiss = { showDeleteDialog = false; collectionToDelete = null },
                {}
//                onConfirm = { c -> viewModel.deleteCollection(c.id) { showDeleteDialog = false; collectionToDelete = null } }
            )
        }
    }
}


@Composable
fun AddCollectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onConfirm(collectionName)
                    }
                },
                enabled = collectionName.isNotBlank()
            ) {
                Text("Create", color = Color(0xFF00D4AA))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = {
            Text(
                "Create New Collection",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Enter a name for your collection:",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D4AA),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF00D4AA),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color(0xFF2A2D35),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DeleteCollectionDialog(
    collection: CollectionModel,
    onDismiss: () -> Unit,
    onConfirm: (CollectionEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {},
//                onClick = { onConfirm(collection) },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        title = {
            Text(
                "Delete Collection",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Are you sure you want to delete \"${collection.name}\"?",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "This action cannot be undone and will remove all associated transactions from this collection.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = Color(0xFF2A2D35),
        shape = RoundedCornerShape(16.dp)
    )
}

//@OptIn(UiToolingDataApi::class)
//@Composable
//fun GroupDetailScreen(
//    collectionId: Long,
//    navController: NavController? = null
//) {
//    val context = LocalContext.current
//    val db = remember { TransactionDatabase.getInstance(context) }
//    val repo = remember { TransactionRepository(db.transactionDao(), db.collectionDao()) }
//    val scope = rememberCoroutineScope()
//
//    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
//    var showFabOptions by remember { mutableStateOf(false) }
//    var showAddTransactionDialog by remember { mutableStateOf(false) }
//
//    var collectionName by remember { mutableStateOf("") }
//    var transactions by remember { mutableStateOf<List<TransactionUiModel>>(emptyList()) }
//    var messageCount by remember { mutableStateOf(0) }
//    var totalSpent by remember { mutableStateOf(0.0) }
//
//    // Load collection details and transactions
//    LaunchedEffect(collectionId) {
//        try {
//            val collection = repo.getCollectionById(collectionId)
//            collectionName = collection?.name ?: "Collection $collectionId"
//            val txns = repo.getTransactionsForCollection(collectionId)
//            transactions = txns
//            messageCount = txns.size
//            totalSpent = txns.sumOf { it.amount }
//        } catch (_: Exception) { }
//    }
//
//    // UI group representation for existing FAB component
//    val uiGroup = remember(collectionId, collectionName, messageCount, totalSpent) {
//        SpendGroup(
//            id = collectionId,
//            name = collectionName,
//            coverColor = Color(0xFF00D4AA),
//            totalSpent = totalSpent,
//            transactionCount = messageCount
//        )
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF0A0A0A))
//            .padding(16.dp)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(20.dp)
//        ) {
//            // --- Group Header replaced with OverviewScreen's HeaderSection ---
//            DetailedHeader(
//                totalAmount = totalSpent,
//                messageCount = messageCount,
//                isLoading = false
//            )
//
//            // Transactions List
//            Text("Transactions", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
//            LazyColumn(
//                modifier = Modifier.fillMaxWidth().weight(1f),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                if (transactions.isEmpty()) {
//                    item {
//                        Text("No transactions found.", color = Color.Gray)
//                    }
//                } else {
//                    items(transactions) { txn ->
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
//                        ) {
//                            Row(
//                                modifier = Modifier.padding(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(Icons.Default.Receipt, contentDescription = null, tint = uiGroup.coverColor, modifier = Modifier.size(32.dp))
//                                Spacer(modifier = Modifier.width(12.dp))
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(txn.place, color = Color.White, fontWeight = FontWeight.Bold)
//                                    Text(txn.category.displayName, color = Color.Gray)
//                                    Text(format.format(txn.amount), color = Color.White)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        EnhancedFloatingActionButton (
//            group = uiGroup,
//            showFabOptions = showFabOptions,
//            onFabToggle = { showFabOptions = !showFabOptions },
//            onAddExisting = {
//                showAddTransactionDialog = true
//                showFabOptions = false
//            },
//            onAddNew = {
//                // TODO: Implement add new transaction logic
//                showFabOptions = false
//            }
//        )
//    }
//
//    if (showAddTransactionDialog) {
//        // Build a lightweight CollectionEntity from collectionId/name
//        val tempCollection = CollectionEntity(id = collectionId, name = collectionName)
//        AddTransactionToCollectionDialog(
//            collection = tempCollection,
//            onDismiss = { showAddTransactionDialog = false },
//            onConfirm = { selected ->
//                // Persist selections then refresh
//                val ids = selected.mapNotNull { it.id }
//                if (ids.isNotEmpty()) {
//                    scope.launch {
//                        ids.forEach { txId ->
//                            repo.addTransactionToCollection(collectionId, txId)
//                        }
//                        val txns = repo.getTransactionsForCollection(collectionId)
//                        transactions = txns
//                        messageCount = txns.size
//                        totalSpent = txns.sumOf { it.amount }
//                        showAddTransactionDialog = false
//                    }
//                } else {
//                    showAddTransactionDialog = false
//                }
//            }
//        )
//    }
//}

//@Composable
//fun AddTransactionToCollectionDialog(
//    collection: CollectionEntity,
//    onDismiss: () -> Unit,
//    onConfirm: (List<TransactionUiModel>) -> Unit
//) {
//    val context = LocalContext.current
//    val db = remember { TransactionDatabase.getInstance(context) }
//    val repo = remember { TransactionRepository(db.transactionDao(), db.collectionDao()) }
//    val scope = rememberCoroutineScope()
//
//    var allTransactions by remember { mutableStateOf<List<TransactionUiModel>>(emptyList()) }
//    var selectedTransactions by remember { mutableStateOf<Set<Long>>(emptySet()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    // Load all transactions
//    LaunchedEffect(Unit) {
//        try {
//            allTransactions = repo.getAllTransactions()
//            isLoading = false
//        } catch (e: Exception) {
//            isLoading = false
//        }
//    }
//
//        AlertDialog(
//        onDismissRequest = onDismiss,
//            confirmButton = {
//            TextButton(
//                onClick = {
//                    val selected = allTransactions.filter { selectedTransactions.contains(it.id) }
//                    onConfirm(selected)
//                },
//                enabled = selectedTransactions.isNotEmpty()
//            ) {
//                Text(
//                    "Add ${selectedTransactions.size} Transaction${if (selectedTransactions.size == 1) "" else "s"}",
//                    color = if (selectedTransactions.isNotEmpty()) Color(0xFF00D4AA) else Color.Gray
//                )
//            }
//            },
//            dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel", color = Color.Gray)
//            }
//        },
//        title = {
//            Text(
//                "Add Transactions to ${collection.name}",
//                color = Color.White,
//                fontWeight = FontWeight.Bold
//            )
//        },
//        text = {
//            Column(
//                modifier = Modifier.heightIn(max = 400.dp)
//            ) {
//                if (isLoading) {
//                    Box(
//                        modifier = Modifier.fillMaxWidth(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator(color = Color(0xFF00D4AA))
//                    }
//                } else if (allTransactions.isEmpty()) {
//                    Text(
//                        "No transactions found",
//                        color = Color.Gray,
//                        modifier = Modifier.padding(16.dp)
//                    )
//                } else {
//                    // Select all/none header
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            "Select transactions to add:",
//                            color = Color.White.copy(alpha = 0.8f),
//                            fontSize = 14.sp
//                        )
//                        TextButton(
//                            onClick = {
//                                selectedTransactions = if (selectedTransactions.size == allTransactions.size) {
//                                    emptySet()
//                                } else {
//                                    allTransactions.mapNotNull { it.id }.toSet()
//                                }
//                            }
//                        ) {
//                            Text(
//                                if (selectedTransactions.size == allTransactions.size) "Deselect All" else "Select All",
//                                fontSize = 12.sp,
//                                color = Color(0xFF00D4AA)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Transaction list
//                    LazyColumn(
//                        modifier = Modifier.heightIn(max = 300.dp),
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        items(allTransactions) { transaction ->
//                            TransactionSelectionCard(
//                                transaction = transaction,
//                                isSelected = selectedTransactions.contains(transaction.id),
//                                onToggle = { isSelected ->
//                                    selectedTransactions = (if (isSelected) {
//                                        selectedTransactions + transaction.id
//                                    } else {
//                                        selectedTransactions - transaction.id
//                                    }) as Set<Long>
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        },
//        containerColor = Color(0xFF2A2D35),
//        shape = RoundedCornerShape(16.dp)
//    )
//}

//@Composable
//fun TransactionSelectionCard(
//    transaction: TransactionUiModel,
//    isSelected: Boolean,
//    onToggle: (Boolean) -> Unit
//) {
//    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onToggle(!isSelected) },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) Color(0xFF00D4AA).copy(alpha = 0.1f) else Color(0xFF1A1A1A)
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isSelected) 2.dp else 1.dp
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Checkbox(
//                checked = isSelected,
//                onCheckedChange = onToggle,
//                colors = CheckboxDefaults.colors(
//                    checkedColor = Color(0xFF00D4AA),
//                    uncheckedColor = Color.White.copy(alpha = 0.3f)
//                )
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(2.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        transaction.place.ifEmpty { "Unknown Place" },
//                        color = Color.White,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 14.sp
//                    )
//                    Text(
//                        format.format(transaction.amount),
//                        color = if (transaction.transactionType == TransactionType.DEBIT) Color(0xFFEF4444) else Color(0xFF00D4AA),
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 14.sp
//                    )
//                }
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        transaction.bankName.ifEmpty { "Unknown Bank" },
//                        color = Color.Gray,
//                        fontSize = 12.sp
//                    )
//                    Text(
//                        transaction.category.displayName,
//                        color = Color.Gray,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        }
//    }
//}

//@OptIn(UiToolingDataApi::class)
//@Composable
//fun EnhancedFloatingActionButton(
//    group: SpendGroup, // Assuming you have a group object
//    showFabOptions: Boolean,
//    onFabToggle: () -> Unit,
//    onAddExisting: () -> Unit,
//    onAddNew: () -> Unit
//) {
//    val transition = updateTransition(targetState = showFabOptions, label = "fab_transition")
//
//    val fabRotation by transition.animateFloat(
//        transitionSpec = { tween(durationMillis = 300, easing = FastOutSlowInEasing) },
//        label = "fab_rotation"
//    ) { expanded ->
//        if (expanded) 45f else 0f
//    }
//
//    val optionsAlpha by transition.animateFloat(
//        transitionSpec = { tween(durationMillis = 200, delayMillis = if (showFabOptions) 50 else 0) },
//        label = "options_alpha"
//    ) { expanded ->
//        if (expanded) 1f else 0f
//    }
//
//    val optionsScale by transition.animateFloat(
//        transitionSpec = {
//            spring(
//                dampingRatio = Spring.DampingRatioMediumBouncy,
//                stiffness = Spring.StiffnessLow
//            )
//        },
//        label = "options_scale"
//    ) { expanded ->
//        if (expanded) 1f else 0.8f
//    }
//
//    val backgroundAlpha by transition.animateFloat(
//        transitionSpec = { tween(durationMillis = 200) },
//        label = "background_alpha"
//    ) { expanded ->
//        if (expanded) 0.3f else 0f
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        // Background overlay when FAB is expanded
//        if (showFabOptions) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = backgroundAlpha))
//                    .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = null
//                    ) { onFabToggle() }
//            )
//        }
//
//        // FAB Options Container
//        AnimatedVisibility(
//            visible = showFabOptions,
//            enter = fadeIn(animationSpec = tween(200, delayMillis = 50)) +
//                    slideInVertically(
//                        initialOffsetY = { it / 2 },
//                        animationSpec = spring(
//                            dampingRatio = Spring.DampingRatioMediumBouncy,
//                            stiffness = Spring.StiffnessLow
//                        )
//                    ),
//            exit = fadeOut(animationSpec = tween(150)) +
//                    slideOutVertically(
//                        targetOffsetY = { it / 2 },
//                        animationSpec = tween(150)
//                    ),
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(end = 16.dp, bottom = 88.dp)
//        ) {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                horizontalAlignment = Alignment.End
//            ) {
//                // Add New Transaction Option
//                FabOptionItem(
//                    text = "Add New Transaction",
//                    icon = Icons.Default.Add,
//                    backgroundColor = Color(0xFF00D4AA),
//                    iconTint = Color.Black,
//                    onClick = onAddNew,
//                    scale = optionsScale,
//                    alpha = optionsAlpha,
//                    animationDelay = 0
//                )
//
//                // Add Existing Transaction Option
//                FabOptionItem(
//                    text = "Add Existing Transaction",
//                    icon = Icons.Default.Edit,
//                    backgroundColor = Color(0xFF6366F1),
//                    iconTint = Color.White,
//                    onClick = onAddExisting,
//                    scale = optionsScale,
//                    alpha = optionsAlpha,
//                    animationDelay = 50
//                )
//            }
//        }
//
//        // Main FAB
//        FloatingActionButton(
//            onClick = onFabToggle,
////            containerColor = group.coverColor,
//            elevation = FloatingActionButtonDefaults.elevation(
//                defaultElevation = 8.dp,
//                pressedElevation = 12.dp
//            ),
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//                .graphicsLayer {
//                    rotationZ = fabRotation
//                    scaleX = if (showFabOptions) 1.1f else 1f
//                    scaleY = if (showFabOptions) 1.1f else 1f
//                }
//                .shadow(
//                    elevation = 8.dp,
//                    shape = CircleShape,
////                    ambientColor = group.coverColor.copy(alpha = 0.3f),
////                    spotColor = group.coverColor.copy(alpha = 0.3f)
//                )
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = if (showFabOptions) "Close" else "Add",
//                tint = Color.Black,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}

//@Composable
//private fun FabOptionItem(
//    text: String,
//    icon: ImageVector,
//    backgroundColor: Color,
//    iconTint: Color,
//    onClick: () -> Unit,
//    scale: Float,
//    alpha: Float,
//    animationDelay: Int
//) {
//    val itemTransition = updateTransition(targetState = alpha > 0f, label = "item_transition")
//
//    val itemScale by itemTransition.animateFloat(
//        transitionSpec = {
//            tween(
//                durationMillis = 300,
//                delayMillis = animationDelay
//            )
//        },
//        label = "item_scale"
//    ) { visible ->
//        if (visible) 1f else 0.8f
//    }
//
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        modifier = Modifier
//            .graphicsLayer {
//                this.alpha = alpha
//                this.scaleX = itemScale
//                this.scaleY = itemScale
//            }
//    ) {
//        // Label
//        Card(
//            modifier = Modifier
//                .wrapContentSize(),
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0xFF2A2D36)
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text(
//                text = text,
//                color = Color.White,
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier
//                    .padding(horizontal = 12.dp, vertical = 8.dp)
//            )
//        }
//
//        // FAB Button
//        FloatingActionButton(
//            onClick = onClick,
//            containerColor = backgroundColor,
//            elevation = FloatingActionButtonDefaults.elevation(
//                defaultElevation = 6.dp,
//                pressedElevation = 8.dp
//            ),
//            modifier = Modifier
//                .size(56.dp)
//                .shadow(
//                    elevation = 6.dp,
//                    shape = CircleShape,
//                    ambientColor = backgroundColor.copy(alpha = 0.3f),
//                    spotColor = backgroundColor.copy(alpha = 0.3f)
//                )
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = text,
//                tint = iconTint,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    }
//}


//@Composable
//fun CollectionCard(collection: CollectionEntity, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.linearGradient(
//                        colors = listOf(Color(0xFF00D4AA), Color(0xFF00D4AA).copy(alpha = 0.8f))
//                    ),
//                    shape = RoundedCornerShape(16.dp)
//                )
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxSize(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Group,
//                        contentDescription = "Collection",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                    Column {
//                        Text(
//                            text = collection.name,
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 16.sp
//                        )
//                        Text(
//                            text = "Tap to view details",
//                            color = Color.White.copy(alpha = 0.9f),
//                            fontSize = 14.sp
//                        )
//                    }
//                }
//                Icon(
//                    Icons.Default.TrendingUp,
//                    contentDescription = "View Details",
//                    tint = Color.White.copy(alpha = 0.7f),
//                    modifier = Modifier.size(24.dp)
//                )
//            }
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun SpendGroupCardPreview() {
//    val sampleGroup = SpendGroup(
//        id = 12L,
//        name = "Friends Getaway",
//        coverColor = Color(0xFF00D4AA),
//        totalSpent = 12345.67,
//        transactionCount = 18
//    )
//    SpendGroupCard(group = sampleGroup, onClick = {})
//}


//@Composable
//fun CollectionsScreen(
//    navController: NavController? = null
//) {
//    val context = LocalContext.current
//    val db = remember { TransactionDatabase.getInstance(context) }
//    val repo = remember { TransactionRepository(db.transactionDao(), db.collectionDao()) }
//    val scope = rememberCoroutineScope()
//
//    var collections by remember { mutableStateOf<List<CollectionEntity>>(emptyList()) }
//    var spendGroups by remember { mutableStateOf<List<SpendGroup>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var showAddGroupDialog by remember { mutableStateOf(false) }
//    var newGroupName by remember { mutableStateOf("") }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//    var collectionToDelete by remember { mutableStateOf<CollectionEntity?>(null) }
//
//    // Load collections from database
//    LaunchedEffect(Unit) {
//        try {
//            collections = repo.getAllCollections()
//            val palette = listOf(
//                Color(0xFF00D4AA),
//                Color(0xFF6366F1),
//                Color(0xFFEF4444),
//                Color(0xFFF59E0B),
//                Color(0xFF10B981)
//            )
//            // Build SpendGroup list with counts
//            val groups = collections.mapIndexed { index, c ->
//                val count = repo.getTransactionCountForCollection(c.id)
//                SpendGroup(
//                    id = c.id,
//                    name = c.name,
//                    coverColor = palette[index % palette.size],
//                    totalSpent = 0.0,
//                    transactionCount = count
//                )
//            }
//            spendGroups = groups
//            isLoading = false
//        } catch (e: Exception) {
//            isLoading = false
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF0A0A0A))
//    ) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//            // Header
//            Text(
//                text = "Collections",
//                style = MaterialTheme.typography.headlineLarge,
//                color = Color.White,
//                fontWeight = FontWeight.Bold
//            )
//
//            // List of Collections
//        LazyColumn(
//            modifier = Modifier.fillMaxWidth().weight(1f),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//                if (isLoading) {
//                    item {
//                        Box(
//                            modifier = Modifier.fillMaxWidth(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator(color = Color(0xFF00D4AA))
//                        }
//                    }
//                } else if (collections.isEmpty()) {
//                item {
//                        Card(
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
//                            shape = RoundedCornerShape(16.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(24.dp),
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Icon(
//                                    Icons.Default.Group,
//                                    contentDescription = null,
//                                    tint = Color.Gray,
//                                    modifier = Modifier.size(48.dp)
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                                Text(
//                                    "No collections found",
//                                    color = Color.White,
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.SemiBold
//                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(
//                                    "Add a new collection to get started!",
//                                    color = Color.Gray,
//                                    fontSize = 14.sp
//                                )
//                            }
//                        }
//                }
//                } else {
//                    items(spendGroups) { group ->
//                        SpendGroupCard(
//                            group = group,
//                            onClick = {
//                                navController?.navigate("groupDetail/${group.id}")
//                            },
//                            onDelete = {
//                                val collection = collections.find { it.id == group.id }
//                                if (collection != null) {
//                                    collectionToDelete = collection
//                                    showDeleteDialog = true
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // Floating Action Button
//        FloatingActionButton(
//            onClick = { showAddGroupDialog = true },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp),
//            containerColor = Color(0xFF00D4AA)
//        ) {
//            Icon(
//                Icons.Default.Add,
//                contentDescription = "Add Collection",
//                tint = Color.Black
//            )
//        }
//    }
//    // Add Collection Dialog
//    if (showAddGroupDialog) {
//        AddCollectionDialog(
//            onDismiss = {
//                showAddGroupDialog = false
//                newGroupName = ""
//            },
//            onConfirm = { name ->
//                scope.launch {
//                    try {
//                        val collectionId = repo.createCollection(name)
//                        collections = repo.getAllCollections()
//                        showAddGroupDialog = false
//                        newGroupName = ""
//                    } catch (e: Exception) {
//                        // Handle error
//                    }
//                }
//            }
//        )
//    }
//
//    // Delete Collection Dialog
//    if (showDeleteDialog && collectionToDelete != null) {
//        DeleteCollectionDialog(
//            collection = collectionToDelete!!,
//            onDismiss = {
//                showDeleteDialog = false
//                collectionToDelete = null
//            },
//            onConfirm = { collection ->
//                scope.launch {
//                    try {
//                        // Delete all mappings first (cascade should handle this, but being explicit)
////                        repo.removeAllMappingsForCollection(collection.id)
//                        // Delete the collection
//                        repo.deleteCollection(collection.id)
//                        // Refresh the lists
//                        collections = repo.getAllCollections()
//                        val palette = listOf(
//                            Color(0xFF00D4AA),
//                            Color(0xFF6366F1),
//                            Color(0xFFEF4444),
//                            Color(0xFFF59E0B),
//                            Color(0xFF10B981)
//                        )
//                        val groups = collections.mapIndexed { index, c ->
//                            val count = repo.getTransactionCountForCollection(c.id)
//                            SpendGroup(
//                                id = c.id,
//                                name = c.name,
//                                coverColor = palette[index % palette.size],
//                                totalSpent = 0.0,
//                                transactionCount = count
//                            )
//                        }
//                        spendGroups = groups
//                        showDeleteDialog = false
//                        collectionToDelete = null
//                    } catch (e: Exception) {
//                        // Handle error
//                    }
//                }
//            }
//        )
//    }
//
//}
