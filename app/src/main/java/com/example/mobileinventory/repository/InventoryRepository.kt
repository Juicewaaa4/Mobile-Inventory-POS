package com.example.mobileinventory.repository

import com.example.mobileinventory.model.InventoryItem
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class InventoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private var storeId: String = ""

    private fun itemsCollection(): CollectionReference {
        return db.collection("stores").document(storeId).collection("inventory")
    }

    fun setStoreId(id: String) {
        storeId = id
    }

    fun getItems(): Flow<List<InventoryItem>> = callbackFlow {
        if (storeId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = itemsCollection().addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { it.toObject(InventoryItem::class.java) }
                trySend(items)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun addItem(item: InventoryItem) {
        val map = hashMapOf(
            "name" to item.name,
            "price" to item.price,
            "cost" to item.cost,
            "stockQuantity" to item.stockQuantity,
            "category" to item.category
        )
        if (item.id.isEmpty()) {
            itemsCollection().add(map)
        } else {
            itemsCollection().document(item.id).set(map)
        }
    }

    fun deleteItem(id: String) {
        itemsCollection().document(id).delete()
    }

    fun updateStock(id: String, newStock: Int) {
        itemsCollection().document(id).update("stockQuantity", newStock)
    }

    fun updateItemDetails(id: String, name: String, price: Double, cost: Double, category: String) {
        val map = mapOf<String, Any>(
            "name" to name,
            "price" to price,
            "cost" to cost,
            "category" to category
        )
        itemsCollection().document(id).update(map)
    }
}
