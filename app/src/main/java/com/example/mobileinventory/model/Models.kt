package com.example.mobileinventory.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class InventoryItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val stockQuantity: Int = 0,
    val category: String = ""
)

data class UserRole(
    @DocumentId val uid: String = "",
    val email: String = "",
    val role: String = "cashier",
    val storeId: String = "",
    val adminEmail: String = ""
)

data class CartItem(
    val inventoryItem: InventoryItem,
    var quantity: Int = 1
)

// ===== Sales / Transactions =====
// Used to show "Sales for Today" to admins only.
data class SaleLine(
    val inventoryItemId: String = "",
    val name: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val unitCost: Double = 0.0,
    val revenue: Double = 0.0,
    val profit: Double = 0.0
)

data class SaleTransaction(
    @DocumentId val id: String = "",
    val cashierUid: String = "",
    val timestamp: Timestamp? = null,
    val items: List<SaleLine> = emptyList(),
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0
)

data class SaleLineSummary(
    val inventoryItemId: String = "",
    val name: String = "",
    val category: String = "",
    val unitCost: Double = 0.0,
    val unitPrice: Double = 0.0,
    val quantity: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0
)
