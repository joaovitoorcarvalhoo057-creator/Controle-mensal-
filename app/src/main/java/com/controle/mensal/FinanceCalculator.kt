
package com.controle.mensal

data class FinancialSummary(
    val totalUnits: Int,
    val revenueCents: Long,
    val expensesCents: Long,
    val profitCents: Long
)

object FinanceCalculator {

    fun calculate(
        sales: List<SaleRecord>,
        expenses: List<ExpenseRecord>,
        startTime: Long = 0L,
        endTime: Long = Long.MAX_VALUE
    ): FinancialSummary {

        val filteredSales = sales.filter {
            it.timestamp in startTime..endTime
        }

        val filteredExpenses = expenses.filter {
            it.timestamp in startTime..endTime
        }

        val units = filteredSales.sumOf {
            it.quantity
        }

        val revenue = filteredSales.sumOf {
            it.quantity.toLong() * it.unitPriceCents
        }

        val costs = filteredExpenses.sumOf {
            it.amountCents.toLong()
        }

        return FinancialSummary(
            totalUnits = units,
            revenueCents = revenue,
            expensesCents = costs,
            profitCents = revenue - costs
        )
    }

    fun formatMoney(cents: Long): String {
        return java.text.NumberFormat
            .getCurrencyInstance(java.util.Locale("pt", "BR"))
            .format(cents / 100.0)
    }
}
