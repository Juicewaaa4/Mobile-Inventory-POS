package com.example.mobileinventory.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileinventory.model.CartItem
import com.example.mobileinventory.model.InventoryItem
import com.example.mobileinventory.model.SaleLine
import com.example.mobileinventory.model.SaleLineSummary
import com.example.mobileinventory.model.SaleTransaction
import com.example.mobileinventory.repository.AuthRepository
import com.example.mobileinventory.repository.InventoryRepository
import com.example.mobileinventory.repository.SalesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val authRepository = AuthRepository(application)
    val inventoryRepository = InventoryRepository()
    val salesRepository = SalesRepository()

    data class CashierSalesSummary(
        val cashierUid: String,
        val totalRevenue: Double,
        val totalProfit: Double,
        val lines: List<SaleLineSummary>
    )

    private val _inventoryList = MutableStateFlow<List<InventoryItem>>(emptyList())
    val rawInventoryList = _inventoryList.asStateFlow()

    internal val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _storeId = MutableStateFlow("")
    val storeId: StateFlow<String> = _storeId.asStateFlow()

    private val _adminEmail = MutableStateFlow("")
    val adminEmail: StateFlow<String> = _adminEmail.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private var inventoryJob: Job? = null
    private var salesJob: Job? = null

    // --- FILTERS ---
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val categories: StateFlow<List<String>> = rawInventoryList.map { list ->
        val distinctCats = list.map { it.category.trim().lowercase().replaceFirstChar { c -> c.uppercase() } }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        listOf("All") + distinctCats
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf("All"))

    val filteredList = combine(_inventoryList, searchQuery, selectedCategory) { inventory, query, category ->
        inventory.filter { item ->
            val matchesSearch = item.name.contains(query, ignoreCase = true)
            val normalizedItemCat = item.category.trim().lowercase().replaceFirstChar { c -> c.uppercase() }
            val matchesCategory = if (category == "All") true else normalizedItemCat == category
            matchesSearch && matchesCategory
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // --- CART STATE ---
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    val subTotal = _cart.map { list -> list.sumOf { it.inventoryItem.price * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun fetchUserRole(uid: String) {
        viewModelScope.launch {
            try {
                val userRoleData = authRepository.getUserRoleData(uid)
                _userRole.value = userRoleData.role
                _storeId.value = userRoleData.storeId
                _adminEmail.value = userRoleData.adminEmail
                inventoryRepository.setStoreId(userRoleData.storeId)
                // Multi-tenant safety: both admin and cashier must record sales under the correct store.
                salesRepository.setStoreId(userRoleData.storeId)
                startInventoryListener()

                // Admin only: show sales history (cashier should not see).
                if (userRoleData.role == "admin") {
                    startSalesListener()
                } else {
                    salesJob?.cancel()
                    salesJob = null
                    _todaySaleLines.value = emptyList()
                    _todayTransactions.value = emptyList()
                    _todayCashierSales.value = emptyList()
                    _todayTotalProfit.value = 0.0
                    _todayTotalRevenue.value = 0.0
                }
            } catch (e: Exception) {
                logout()
                _loginError.value = "Connection failed: Please check your internet and try again."
            }
        }
    }
    
    fun clearLoginError() {
        _loginError.value = null
    }

    private fun startInventoryListener() {
        inventoryJob?.cancel()
        inventoryJob = viewModelScope.launch {
            inventoryRepository.getItems().collect { list ->
                _inventoryList.value = list
            }
        }
    }

    fun logout() {
        authRepository.auth.signOut()
        _userRole.value = null
        _storeId.value = ""
        _adminEmail.value = ""
        _inventoryList.value = emptyList()
        inventoryJob?.cancel()
        salesJob?.cancel()
        salesJob = null
        _todaySaleLines.value = emptyList()
        _todayTransactions.value = emptyList()
        _todayCashierSales.value = emptyList()
        _todayTotalProfit.value = 0.0
        _todayTotalRevenue.value = 0.0
        _selectedSalesDate.value = LocalDate.now(ZoneId.systemDefault())
        clearCart()
    }

    // --- SALES (Admin only) ---
    private val _selectedSalesDate = MutableStateFlow(LocalDate.now(ZoneId.systemDefault()))
    val selectedSalesDate: StateFlow<LocalDate> = _selectedSalesDate.asStateFlow()

    private val _todaySaleLines = MutableStateFlow<List<SaleLineSummary>>(emptyList())
    val todaySaleLines: StateFlow<List<SaleLineSummary>> = _todaySaleLines.asStateFlow()

    private val _todayTotalProfit = MutableStateFlow(0.0)
    val todayTotalProfit: StateFlow<Double> = _todayTotalProfit.asStateFlow()

    private val _todayTotalRevenue = MutableStateFlow(0.0)
    val todayTotalRevenue: StateFlow<Double> = _todayTotalRevenue.asStateFlow()

    private val _todayTransactions = MutableStateFlow<List<SaleTransaction>>(emptyList())
    val todayTransactions: StateFlow<List<SaleTransaction>> = _todayTransactions.asStateFlow()

    private val _todayCashierSales = MutableStateFlow<List<CashierSalesSummary>>(emptyList())
    val todayCashierSales: StateFlow<List<CashierSalesSummary>> = _todayCashierSales.asStateFlow()

    fun setSelectedSalesDate(date: LocalDate) {
        _selectedSalesDate.value = date
        startSalesListener()
    }

    private fun startSalesListener() {
        salesJob?.cancel()
        salesJob = viewModelScope.launch {
            val date = _selectedSalesDate.value
            salesRepository.getSalesForDate(date).collect { sales ->
                _todayTransactions.value = sales
                val aggregated = linkedMapOf<String, SaleLineSummary>()
                var totalRevenue = 0.0
                var totalProfit = 0.0

                // cashierUid -> (itemKey -> aggregated line)
                val cashierItemAgg = linkedMapOf<String, LinkedHashMap<String, SaleLineSummary>>()
                val cashierRevenue = linkedMapOf<String, Double>()
                val cashierProfit = linkedMapOf<String, Double>()

                sales.forEach { tx ->
                    val cashierUid = tx.cashierUid.ifBlank { "Unknown Cashier" }
                    tx.items.forEach { line ->
                        totalRevenue += line.revenue
                        totalProfit += line.profit

                        cashierRevenue[cashierUid] = (cashierRevenue[cashierUid] ?: 0.0) + line.revenue
                        cashierProfit[cashierUid] = (cashierProfit[cashierUid] ?: 0.0) + line.profit

                        val key = line.inventoryItemId.ifBlank { line.name }
                        val existing = aggregated[key]
                        if (existing == null) {
                            aggregated[key] = SaleLineSummary(
                                inventoryItemId = line.inventoryItemId,
                                name = line.name,
                                category = line.category,
                                unitCost = line.unitCost,
                                unitPrice = line.unitPrice,
                                quantity = line.quantity,
                                totalRevenue = line.revenue,
                                totalProfit = line.profit
                            )
                        } else {
                            aggregated[key] = existing.copy(
                                quantity = existing.quantity + line.quantity,
                                totalRevenue = existing.totalRevenue + line.revenue,
                                totalProfit = existing.totalProfit + line.profit
                            )
                        }

                        val cashierMap = cashierItemAgg.getOrPut(cashierUid) { LinkedHashMap() }
                        val cashierExisting = cashierMap[key]
                        if (cashierExisting == null) {
                            cashierMap[key] = SaleLineSummary(
                                inventoryItemId = line.inventoryItemId,
                                name = line.name,
                                category = line.category,
                                unitCost = line.unitCost,
                                unitPrice = line.unitPrice,
                                quantity = line.quantity,
                                totalRevenue = line.revenue,
                                totalProfit = line.profit
                            )
                        } else {
                            cashierMap[key] = cashierExisting.copy(
                                quantity = cashierExisting.quantity + line.quantity,
                                totalRevenue = cashierExisting.totalRevenue + line.revenue,
                                totalProfit = cashierExisting.totalProfit + line.profit
                            )
                        }
                    }
                }

                _todaySaleLines.value = aggregated.values.toList().sortedBy { it.name }
                _todayTotalProfit.value = totalProfit
                _todayTotalRevenue.value = totalRevenue

                _todayCashierSales.value = cashierItemAgg.entries
                    .map { (cashierUid, itemMap) ->
                        CashierSalesSummary(
                            cashierUid = cashierUid,
                            totalRevenue = cashierRevenue[cashierUid] ?: 0.0,
                            totalProfit = cashierProfit[cashierUid] ?: 0.0,
                            lines = itemMap.values.toList().sortedBy { it.name }
                        )
                    }
                    .sortedBy { it.cashierUid }
            }
        }
    }

    // --- EXPORT TO CSV ---
    fun exportSalesToCsv(context: Context) {
        val date = _selectedSalesDate.value
        val lines = _todaySaleLines.value
        val totalProfit = _todayTotalProfit.value
        val totalRevenue = _todayTotalRevenue.value

        if (lines.isEmpty()) {
            Toast.makeText(context, "No sales data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val fileName = "Sales_$dateStr.csv"

        val sb = StringBuilder()
        sb.appendLine("Sales Report - $dateStr")
        sb.appendLine()
        sb.appendLine("Product Name,Category,Qty Sold,Cost Price each,Selling Price each,Revenue,Profit")

        lines.forEach { line ->
            val name = line.name.replace(",", " ")
            val category = line.category.replace(",", " ").ifBlank { "Uncategorized" }
            sb.appendLine("$name,$category,${line.quantity},${"%.2f".format(line.unitCost)},${"%.2f".format(line.unitPrice)},${"%.2f".format(line.totalRevenue)},${"%.2f".format(line.totalProfit)}")
        }

        sb.appendLine()
        val totalQty = lines.sumOf { it.quantity }
        sb.appendLine("TOTAL,,$totalQty,,,${"%.2f".format(totalRevenue)},${"%.2f".format(totalProfit)}")

        try {
            val cacheDir = File(context.cacheDir, "exports")
            cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            file.writeText(sb.toString())

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sales Report - $dateStr")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Sales Report"))

        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- INVENTORY OPERATIONS ---
    fun addItem(name: String, price: Double, cost: Double, stock: Int, category: String) {
        inventoryRepository.addItem(
            InventoryItem(name = name, price = price, cost = cost, stockQuantity = stock.coerceIn(0, 100), category = category)
        )
    }

    fun updateStock(item: InventoryItem, delta: Int) {
        val newStock = (item.stockQuantity + delta).coerceIn(0, 100)
        inventoryRepository.updateStock(item.id, newStock)
    }

    fun setStockQuantity(item: InventoryItem, quantity: Int) {
        val newStock = quantity.coerceIn(0, 100)
        inventoryRepository.updateStock(item.id, newStock)
    }

    fun editItem(id: String, newName: String, newPrice: Double, newCost: Double, newCategory: String) {
        inventoryRepository.updateItemDetails(id, newName, newPrice, newCost, newCategory)
    }

    fun deleteItem(id: String) {
        inventoryRepository.deleteItem(id)
    }

    // --- CART OPERATIONS ---
    fun addToCart(item: InventoryItem) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.inventoryItem.id == item.id }
        if (index != -1) {
            val existing = currentCart[index]
            if (existing.quantity < item.stockQuantity) {
                currentCart[index] = existing.copy(quantity = existing.quantity + 1)
                _cart.value = currentCart
            }
        } else {
            if (item.stockQuantity > 0) {
                currentCart.add(CartItem(item, 1))
                _cart.value = currentCart
            }
        }
    }

    fun reduceFromCart(item: InventoryItem) {
        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.inventoryItem.id == item.id }
        if (index != -1) {
            val existing = currentCart[index]
            if (existing.quantity > 1) {
                currentCart[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                currentCart.removeAt(index)
            }
            _cart.value = currentCart
        }
    }

    fun setCartQuantity(item: InventoryItem, quantity: Int) {
        val desired = quantity.coerceIn(0, 100)
        val maxAllowed = item.stockQuantity.coerceIn(0, 100)

        val currentCart = _cart.value.toMutableList()
        val index = currentCart.indexOfFirst { it.inventoryItem.id == item.id }

        if (index == -1) {
            if (desired > 0 && maxAllowed > 0) {
                currentCart.add(CartItem(item, desired.coerceAtMost(maxAllowed)))
                _cart.value = currentCart
            }
            return
        }

        if (desired <= 0 || maxAllowed <= 0) {
            currentCart.removeAt(index)
        } else {
            currentCart[index] = currentCart[index].copy(quantity = desired.coerceAtMost(maxAllowed))
        }
        _cart.value = currentCart
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun checkout(onResult: (Boolean, String) -> Unit) {
        val currentOrder = _cart.value
        if (currentOrder.isEmpty()) {
            onResult(false, "Cart is empty")
            return
        }

        // Validate + compute sale lines (important: do not use forEach for early-return).
        val cashierUid = authRepository.auth.currentUser?.uid ?: ""
        val saleItems = mutableListOf<SaleLine>()
        var totalRevenue = 0.0
        var totalProfit = 0.0

        for (cartItem in currentOrder) {
            val liveItem = _inventoryList.value.find { it.id == cartItem.inventoryItem.id }
            if (liveItem == null || liveItem.stockQuantity < cartItem.quantity) {
                onResult(false, "Not enough stock for ${cartItem.inventoryItem.name}")
                return
            }

            val qty = cartItem.quantity
            val revenue = liveItem.price * qty
            val profit = (liveItem.price - liveItem.cost) * qty

            saleItems.add(
                SaleLine(
                    inventoryItemId = liveItem.id,
                    name = liveItem.name,
                    category = liveItem.category,
                    quantity = qty,
                    unitPrice = liveItem.price,
                    unitCost = liveItem.cost,
                    revenue = revenue,
                    profit = profit
                )
            )
            totalRevenue += revenue
            totalProfit += profit
        }

        // Update stock after validation.
        for (cartLine in saleItems) {
            val liveItem = _inventoryList.value.find { it.id == cartLine.inventoryItemId } ?: continue
            inventoryRepository.updateStock(liveItem.id, liveItem.stockQuantity - cartLine.quantity)
        }

        // Record sale for admin dashboard.
        salesRepository.recordSale(
            cashierUid = cashierUid,
            items = saleItems,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit
        )

        clearCart()
        onResult(true, "Checkout Successful!")
    }
}
