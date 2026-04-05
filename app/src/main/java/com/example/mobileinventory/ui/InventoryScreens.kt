package com.example.mobileinventory.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mobileinventory.model.CartItem
import com.example.mobileinventory.model.InventoryItem
import com.example.mobileinventory.model.SaleLineSummary
import com.example.mobileinventory.ui.theme.*
import com.example.mobileinventory.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ============== SHARED: Search + Filter Bar ==============

@Composable
fun SearchFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search products...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = GreenPrimary.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = GreenOutline,
                cursorColor = GreenPrimary
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box {
            FilledTonalButton(
                onClick = { filterExpanded = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (selectedCategory == "All") "All" else selectedCategory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                cat,
                                fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                                color = if (cat == selectedCategory) GreenPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = { onCategorySelect(cat); filterExpanded = false },
                        leadingIcon = if (cat == selectedCategory) {
                            {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GreenPrimary)
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

// ================= ADMIN SCREENS =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val items by viewModel.filteredList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GreenPrimary, GreenPrimaryDark)
                            )
                        )
                        .padding(28.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Inventory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Admin Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null, tint = GreenPrimary) },
                    label = {
                        Text(
                            "Add New Product",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { showAddDialog = true; scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = GreenPrimary) },
                    label = {
                        Text(
                            "Create User Account",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("add_account"); scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GreenPrimary) },
                    label = {
                        Text(
                            "Transaction History",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("admin_sales_today"); scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                Divider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = GreenOutline.copy(alpha = 0.4f)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = GreenError) },
                    label = {
                        Text(
                            "Logout",
                            fontWeight = FontWeight.Medium,
                            color = GreenError
                        )
                    },
                    selected = false,
                    onClick = { viewModel.logout(); navController.navigate("login") { popUpTo(0) } },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Admin Panel",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GreenPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                SearchFilterBar(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.searchQuery.value = it },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectedCategory.value = it }
                )

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = GreenOutline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No products found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap the menu to add inventory",
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenOutline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            AdminItemRow(
                                item = item,
                                onAddStock = { viewModel.updateStock(item, 1) },
                                onMinusStock = { viewModel.updateStock(item, -1) },
                                onSetStockQuantity = { qty -> viewModel.setStockQuantity(item, qty) },
                                onEdit = { itemToEdit = item },
                                onDelete = { viewModel.deleteItem(item.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, price, costPerUnit, qty, cat ->
                viewModel.addItem(name, price, costPerUnit, qty, cat)
                showAddDialog = false
            }
        )
    }

    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onUpdate = { name, price, cost, cat ->
                viewModel.editItem(itemToEdit!!.id, name, price, cost, cat)
                itemToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSalesTodayScreen(navController: NavController, viewModel: MainViewModel) {
    val saleLines by viewModel.todaySaleLines.collectAsState()
    val todayTotalProfit by viewModel.todayTotalProfit.collectAsState()
    val todayTotalRevenue by viewModel.todayTotalRevenue.collectAsState()
    val selectedDate by viewModel.selectedSalesDate.collectAsState()
    val context = LocalContext.current

    val today = remember { LocalDate.now(ZoneId.systemDefault()) }
    val isToday = selectedDate == today

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM d, yyyy") }
    val dateLabel = if (isToday) "Today — ${selectedDate.format(dateFormatter)}" else selectedDate.format(dateFormatter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transaction History",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportSalesToCsv(context) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ===== Date Picker Row =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        viewModel.setSelectedSalesDate(selectedDate.minusDays(1))
                    }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day",
                            tint = GreenPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable {
                                val dpd = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        viewModel.setSelectedSalesDate(LocalDate.of(year, month + 1, dayOfMonth))
                                    },
                                    selectedDate.year,
                                    selectedDate.monthValue - 1,
                                    selectedDate.dayOfMonth
                                )
                                dpd.show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Pick Date",
                            modifier = Modifier.size(18.dp),
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            dateLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.setSelectedSalesDate(selectedDate.plusDays(1))
                        },
                        enabled = selectedDate < today
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Day",
                            tint = if (selectedDate < today) GreenPrimary else GreenOutline.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ===== Summary Card =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Summary",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Revenue",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                "₱${"%.2f".format(todayTotalRevenue)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Profit (Kinita)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                            Text(
                                "₱${"%.2f".format(todayTotalProfit)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (todayTotalProfit >= 0) GreenSecondary else GreenError
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalQtySold = saleLines.sumOf { it.quantity }
                    Text(
                        "Items Sold: $totalQtySold",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Export Button =====
            Button(
                onClick = { viewModel.exportSalesToCsv(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenSecondary,
                    contentColor = Color.White
                ),
                enabled = saleLines.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Export to Excel (CSV)",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== Sales List =====
            if (saleLines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = GreenOutline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (isToday) "No sales today yet." else "No sales on this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Transactions will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenOutline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(saleLines, key = { it.inventoryItemId.ifBlank { it.name } }) { line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            line.category.ifBlank { "—" },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    // Qty sold badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GreenSecondary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "${line.quantity} sold",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = GreenSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    line.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Cost: ₱${"%.2f".format(line.unitCost)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Price: ₱${"%.2f".format(line.unitPrice)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = GreenPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = GreenOutlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Revenue: ₱${"%.2f".format(line.totalRevenue)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (line.totalProfit >= 0) GreenSecondary else GreenError
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "Profit: ₱${"%.2f".format(line.totalProfit)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (line.totalProfit >= 0) GreenSecondary else GreenError
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun AdminItemRow(
    item: InventoryItem,
    onAddStock: () -> Unit,
    onMinusStock: () -> Unit,
    onSetStockQuantity: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var stockText by remember { mutableStateOf(item.stockQuantity.toString()) }
    LaunchedEffect(item.stockQuantity) {
        stockText = item.stockQuantity.toString()
    }
    val profitPerUnit = item.price - item.cost
    val totalProfit = profitPerUnit * item.stockQuantity
    val isLowStock = item.stockQuantity <= 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        item.category.ifBlank { "—" },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Stock indicator  
                if (isLowStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenErrorContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "LOW STOCK",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = GreenError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Cost: ₱${"%.2f".format(item.cost)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Price: ₱${"%.2f".format(item.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = GreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (profitPerUnit > 0) GreenSecondary else GreenError
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "₱${"%.2f".format(profitPerUnit)}/unit • Total: ₱${"%.2f".format(totalProfit)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (profitPerUnit > 0) GreenSecondary else GreenError
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = GreenOutlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: stock controls + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stock controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = onMinusStock,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                    }
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            stockText = if (digitsOnly.isBlank()) "" else digitsOnly
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val desired = stockText.toIntOrNull() ?: 0
                                onSetStockQuantity(desired)
                            }
                        ),
                        modifier = Modifier
                            .width(78.dp)
                            .padding(horizontal = 14.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = GreenOutline,
                            cursorColor = GreenPrimary
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) GreenError else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    FilledTonalIconButton(
                        onClick = onAddStock,
                        modifier = Modifier.size(34.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = GreenError.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Product?") },
            text = {
                Text(
                    "Sigurado ka ba na gusto mong i-delete ang \"${item.name}\"? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = GreenError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// ============== DIALOGS ==============

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") } // selling price per unit
    var costPrice by remember { mutableStateOf("") } // cost per unit
    var category by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }

    val context = LocalContext.current

    val parsedPrice = price.toDoubleOrNull()
    val parsedCostPrice = costPrice.toDoubleOrNull()

    val canCompute = parsedPrice != null && parsedCostPrice != null
    val computedProfitPerUnit = if (canCompute) parsedPrice!! - parsedCostPrice!! else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add New Product", fontWeight = FontWeight.Bold)
                Text(
                    "Fill in the product details below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Drinks)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = costPrice,
                    onValueChange = { costPrice = it },
                    label = { Text("Cost Price each (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Selling Price each (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )

                OutlinedTextField(
                    value = initialStock,
                    onValueChange = { initialStock = it },
                    label = { Text("Initial Stock Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )

                if (canCompute) {
                    Divider(color = GreenOutlineVariant.copy(alpha = 0.4f))
                    Text(
                        "Computed:",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Profit per piece: ₱${"%.2f".format(computedProfitPerUnit ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if ((computedProfitPerUnit ?: 0.0) >= 0) GreenSecondary else GreenError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val c = costPrice.toDoubleOrNull() ?: 0.0
                    val cat = category.ifBlank { "Uncategorized" }
                    val stock = initialStock.toIntOrNull() ?: 0

                    if (name.isBlank()) return@Button
                    if (costPrice.isBlank()) {
                        Toast.makeText(context, "Enter valid Cost Price", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onAdd(name, p, c, stock, cat)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) { Text("Save", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun EditItemDialog(item: InventoryItem, onDismiss: () -> Unit, onUpdate: (String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var cost by remember { mutableStateOf(item.cost.toString()) }
    var category by remember { mutableStateOf(item.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Edit Product", fontWeight = FontWeight.Bold)
                Text(
                    "Update the product details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost Price each (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Selling Price each (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                
                val parsedPrice = price.toDoubleOrNull()
                val parsedCost = cost.toDoubleOrNull()
                val canCompute = parsedPrice != null && parsedCost != null
                if (canCompute) {
                    val computedProfit = parsedPrice!! - parsedCost!!
                    Divider(color = GreenOutlineVariant.copy(alpha = 0.4f))
                    Text(
                        "Computed:",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Profit per piece: ₱${"%.2f".format(computedProfit)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (computedProfit >= 0) GreenSecondary else GreenError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val c = cost.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onUpdate(name, p, c, category.ifBlank { "Uncategorized" })
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) { Text("Update", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// ================= CASHIER POS SCREENS =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierPosScreen(navController: NavController, viewModel: MainViewModel) {
    val items by viewModel.filteredList.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val subTotal by viewModel.subTotal.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                // Drawer header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GreenSecondary, GreenPrimary)
                            )
                        )
                        .padding(28.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Inventory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Cashier Terminal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        if (adminEmail.isNotBlank()) {
                            Text(
                                "Linked Admin: $adminEmail",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.65f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Divider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = GreenOutline.copy(alpha = 0.4f)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = GreenError) },
                    label = {
                        Text(
                            "Logout",
                            fontWeight = FontWeight.Medium,
                            color = GreenError
                        )
                    },
                    selected = false,
                    onClick = { viewModel.logout(); navController.navigate("login") { popUpTo(0) } },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Cashier POS",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GreenSecondary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.weight(1f)) {
                    SearchFilterBar(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectedCategory.value = it }
                    )

                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = GreenOutline
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No products available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                CashierCatalogRow(item = item, onClick = { viewModel.addToCart(item) })
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }

                if (cart.isNotEmpty()) {
                    Surface(
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier.fillMaxHeight(0.45f).fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Current Order",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${cart.sumOf { it.quantity }} items",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Divider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = GreenOutlineVariant.copy(alpha = 0.5f)
                            )

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(cart, key = { it.inventoryItem.id }) { cartItem ->
                                    CartItemRow(
                                        cartItem = cartItem,
                                        onAdd = { viewModel.addToCart(cartItem.inventoryItem) },
                                        onMinus = { viewModel.reduceFromCart(cartItem.inventoryItem) }
                                    )
                                }
                            }

                            Divider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = GreenOutlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "TOTAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "₱${"%.2f".format(subTotal)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                                Button(
                                    onClick = { showPaymentDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GreenPrimary,
                                        contentColor = Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Text(
                                        "CHECKOUT",
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        PaymentDialog(
            subTotal = subTotal,
            onConfirm = {
                viewModel.checkout { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) showPaymentDialog = false
                }
            },
            onDismiss = { showPaymentDialog = false }
        )
    }
}

@Composable
fun PaymentDialog(subTotal: Double, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var tenderedStr by remember { mutableStateOf("") }
    val tendered = tenderedStr.toDoubleOrNull() ?: 0.0
    val change = tendered - subTotal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Payment", fontWeight = FontWeight.Bold)
                Text(
                    "Complete the transaction",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // Total amount card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "₱${"%.2f".format(subTotal)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = tenderedStr,
                    onValueChange = { tenderedStr = it },
                    label = { Text("Tendered Cash (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        focusedLabelColor = GreenPrimary,
                        cursorColor = GreenPrimary
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (tenderedStr.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (change >= 0)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                GreenErrorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (change >= 0) "Sukli (Change)" else "Insufficient!",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (change >= 0)
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                else
                                    GreenError
                            )
                            Text(
                                if (change >= 0) "₱${"%.2f".format(change)}" else "Short by ₱${"%.2f".format(-change)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (change >= 0) GreenSecondary else GreenError
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = change >= 0 && tenderedStr.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm Payment", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Cashier sees selling price ONLY (no cost/puhunan)
@Composable
fun CashierCatalogRow(item: InventoryItem, onClick: () -> Unit) {
    val isOutOfStock = item.stockQuantity <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isOutOfStock, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isOutOfStock)
                MaterialTheme.colorScheme.surface
            else
                GreenErrorContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!isOutOfStock) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!isOutOfStock)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₱${"%.2f".format(item.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (!isOutOfStock) GreenPrimary else GreenError
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (!isOutOfStock)
                                GreenSecondaryContainer.copy(alpha = 0.9f)
                            else
                                GreenErrorContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (!isOutOfStock) "${item.stockQuantity} In Stock" else "OUT OF STOCK",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isOutOfStock)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onAdd: () -> Unit,
    onMinus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cartItem.inventoryItem.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "₱${"%.2f".format(cartItem.inventoryItem.price)} × ${cartItem.quantity} = ₱${"%.2f".format(cartItem.inventoryItem.price * cartItem.quantity)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = onMinus,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
            }
            Text(
                "${cartItem.quantity}",
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            FilledTonalIconButton(
                onClick = onAdd,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
            }
        }
    }
}
