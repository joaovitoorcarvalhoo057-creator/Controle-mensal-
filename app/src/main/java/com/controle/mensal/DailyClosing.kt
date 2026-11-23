
package com.controle.mensal

data class DailyClosing(
    val id: Long,
    val date: Long,
    val closingTime: Long,
    val soldQuantity: Int,
    val revenueCents: Long,
    val expensesCents: Long,
    val profitCents: Long,
    val remainingStock: Int
)

object DailyClosingCalculator {

    fun create(
        sales: List<SaleRecord>,
        expenses: List<ExpenseRecord>,
        remainingStock: Int
    ): DailyClosing {

        val summary = FinanceCalculator.calculate(
            sales,
            expenses
        )

        val now = System.currentTimeMillis()

        return DailyClosing(
            id = now,
            date = now,
            closingTime = now,
            soldQuantity = summary.totalUnits,
            revenueCents = summary.revenueCents,
            expensesCents = summary.expensesCents,
            profitCents = summary.profitCents,
            remainingStock = remainingStock
        )
    }
}
