
package com.controle.mensal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PamonhaRepository(context: Context) {

    private val prefs = context.getSharedPreferences(
        "pamonha_database",
        Context.MODE_PRIVATE
    )

    fun saveFlavors(items: List<PamonhaFlavor>) {
        val array = JSONArray()

        items.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("priceCents", it.priceCents)
                put("stock", it.stock)
            })
        }

        prefs.edit().putString("flavors", array.toString()).apply()
    }

    fun loadFlavors(): List<PamonhaFlavor> {
        val json = prefs.getString("flavors", null)
            ?: return listOf(
                PamonhaFlavor(1L, "Tradicional", 600, 0)
            )

        val array = JSONArray(json)
        return (0 until array.length()).map {
            val item = array.getJSONObject(it)
            PamonhaFlavor(
                item.getLong("id"),
                item.getString("name"),
                item.getInt("priceCents"),
                item.getInt("stock")
            )
        }
    }

    fun saveSales(items: List<SaleRecord>) {
        val array = JSONArray()

        items.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("flavorId", it.flavorId)
                put("flavorName", it.flavorName)
                put("quantity", it.quantity)
                put("unitPriceCents", it.unitPriceCents)
                put("timestamp", it.timestamp)
            })
        }

        prefs.edit().putString("sales", array.toString()).apply()
    }

    fun loadSales(): List<SaleRecord> {
        val array = JSONArray(
            prefs.getString("sales", "[]")
        )

        return (0 until array.length()).map {
            val item = array.getJSONObject(it)
            SaleRecord(
                item.getLong("id"),
                item.getLong("flavorId"),
                item.getString("flavorName"),
                item.getInt("quantity"),
                item.getInt("unitPriceCents"),
                item.getLong("timestamp")
            )
        }
    }

    fun saveExpenses(items: List<ExpenseRecord>) {
        val array = JSONArray()

        items.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("description", it.description)
                put("category", it.category)
                put("amountCents", it.amountCents)
                put("timestamp", it.timestamp)
            })
        }

        prefs.edit().putString("expenses", array.toString()).apply()
    }

    fun loadExpenses(): List<ExpenseRecord> {
        val array = JSONArray(
            prefs.getString("expenses", "[]")
        )

        return (0 until array.length()).map {
            val item = array.getJSONObject(it)
            ExpenseRecord(
                item.getLong("id"),
                item.getString("description"),
                item.getString("category"),
                item.getInt("amountCents"),
                item.getLong("timestamp")
            )
        }
    }
}
