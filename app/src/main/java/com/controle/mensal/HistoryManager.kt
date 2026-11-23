
package com.controle.mensal

import java.util.Calendar

data class DailyHistory(
    val date: Long,
    val sales: List<SaleRecord>,
    val expenses: List<ExpenseRecord>,
    val summary: FinancialSummary
)

object HistoryManager {

    fun getDailyHistory(
        sales: List<SaleRecord>,
        expenses: List<ExpenseRecord>
    ): List<DailyHistory> {

        val dates = (sales.map { it.timestamp } +
                expenses.map { it.timestamp })
            .map { timestamp ->
                Calendar.getInstance().apply {
                    timeInMillis = timestamp
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            .distinct()
            .sortedDescending()

        return dates.map { date ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
            }

            val nextDay = Calendar.getInstance().apply {
                timeInMillis = date
                add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis

            val dailySales = sales.filter {
                it.timestamp in date until nextDay
            }

            val dailyExpenses = expenses.filter {
                it.timestamp in date until nextDay
            }

            DailyHistory(
                date = date,
                sales = dailySales,
                expenses = dailyExpenses,
                summary = FinanceCalculator.calculate(
                    dailySales,
                    dailyExpenses
                )
            )
        }
    }
}
