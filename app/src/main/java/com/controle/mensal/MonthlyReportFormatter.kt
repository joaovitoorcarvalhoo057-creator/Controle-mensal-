
package com.controle.mensal

import java.util.Calendar

class MonthlyReportManager(
    private val salesManager: SalesManager,
    private val expenseManager: ExpenseManager
) {

    fun getReport(month: Int, year: Int): MonthlySummary {
        return MonthlyReport.calculate(
            sales = salesManager.getSales(),
            expenses = expenseManager.getExpenses(),
            month = month,
            year = year
        )
    }

    fun getCurrentMonthReport(): MonthlySummary {
        val calendar = Calendar.getInstance()

        return getReport(
            month = calendar.get(Calendar.MONTH),
            year = calendar.get(Calendar.YEAR)
        )
    }
}
