package com.example.mobileinventory.repository

import com.example.mobileinventory.model.SaleLine
import com.example.mobileinventory.model.SaleTransaction
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.ZoneId
import java.time.LocalDate
import java.time.Instant
import java.util.Date

class SalesRepository {
    private val db = FirebaseFirestore.getInstance()
    private var storeId: String = ""

    fun setStoreId(id: String) {
        storeId = id
    }

    private fun salesCollection(): CollectionReference {
        return db.collection("stores").document(storeId).collection("sales")
    }

    fun getSalesForToday(): Flow<List<SaleTransaction>> {
        return getSalesForDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate())
    }

    fun getSalesForDate(date: LocalDate): Flow<List<SaleTransaction>> = callbackFlow {
        if (storeId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val zone = ZoneId.systemDefault()
        val startInstant = date.atStartOfDay(zone).toInstant()
        val endInstant = date.plusDays(1).atStartOfDay(zone).toInstant()

        val startTs = Timestamp(Date.from(startInstant))
        val endTs = Timestamp(Date.from(endInstant))

        val query: Query = salesCollection()
            .whereGreaterThanOrEqualTo("timestamp", startTs)
            .whereLessThan("timestamp", endTs)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val sales = snapshot.documents.mapNotNull { it.toObject(SaleTransaction::class.java) }
                trySend(sales)
            }
        }

        awaitClose { subscription.remove() }
    }

    fun recordSale(
        cashierUid: String,
        items: List<SaleLine>,
        totalRevenue: Double,
        totalProfit: Double,
        timestamp: Timestamp = Timestamp(Date())
    ) {
        if (storeId.isEmpty()) return

        val itemMaps = items.map { line ->
            mapOf(
                "inventoryItemId" to line.inventoryItemId,
                "name" to line.name,
                "category" to line.category,
                "quantity" to line.quantity,
                "unitPrice" to line.unitPrice,
                "unitCost" to line.unitCost,
                "revenue" to line.revenue,
                "profit" to line.profit
            )
        }

        val saleDoc = hashMapOf(
            "cashierUid" to cashierUid,
            "timestamp" to timestamp,
            "items" to itemMaps,
            "totalRevenue" to totalRevenue,
            "totalProfit" to totalProfit
        )

        salesCollection().add(saleDoc)
    }
}
