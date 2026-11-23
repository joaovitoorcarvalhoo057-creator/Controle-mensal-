
package com.controle.mensal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ClosingRepository(context: Context) {

    private val prefs = context.getSharedPreferences(
        "pamonha_closings",
        Context.MODE_PRIVATE
    )

    fun save(closing: DailyClosing) {
        val items = load().toMutableList()

        items.removeAll { it.id == closing.id }
        items.add(closing)

        val array = JSONArray()

        items.forEach {
            array.put(JSONObject().apply {
                put("id", it.id)
                put("date", it.date)
                put("closingTime", it.closingTime)
                put("soldQuantity", it.soldQuantity)
                put("revenueCents", it.revenueCents)
                put("expensesCents", it.expensesCents)
                put("profitCents", it.profitCents)
                put("remainingStock", it.remainingStock)
            })
        }

        prefs.edit().putString("closings", array.toString()).apply()
    }

    fun load(): List<DailyClosing> {
        val array = JSONArray(
            prefs.getString("closings", "[]")
        )

        return (0 until array.length()).map {
            val item = array.getJSONObject(it)

            DailyClosing(
                id = item.getLong("id"),
                date = item.getLong("date"),
                closingTime = item.getLong("closingTime"),
                soldQuantity = item.getInt("soldQuantity"),
                revenueCents = item.getLong("revenueCents"),
                expensesCents = item.getLong("expensesCents"),
                profitCents = item.getLong("profitCents"),
                remainingStock = item.getInt("remainingStock")
            )
        }.sortedByDescending { it.date }
    }
}
