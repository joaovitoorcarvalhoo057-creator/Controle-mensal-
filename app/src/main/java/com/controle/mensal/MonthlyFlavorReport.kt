package com.controle.mensal

import java.util.Calendar

data class FlavorMonthlySummary(
    val flavorName: String,
    val totalUnits: Int,
    val revenueCents: Long
)

object MonthlyFlavorReport {

    fun calculate(
        sales: List<SaleRecord>,
        month: Int,
        year: Int
    ): List<FlavorMonthlySummary> {

        val monthlySales = sales.filter { sale ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = sale.timestamp
            }

            calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.YEAR) == year
        }

        return monthlySales
            .groupBy { it.flavorName }
            .map { (name, flavorSales) ->

                FlavorMonthlySummary(
                    flavorName = name,
                    totalUnits = flavorSales.sumOf {
                        it.quantity
                    },
                    revenueCents = flavorSales.sumOf {
                        it.quantity.toLong() *
                            it.unitPriceCents.toLong()
                    }
                )
            }
            .sortedByDescending { it.totalUnits }
    }
}
