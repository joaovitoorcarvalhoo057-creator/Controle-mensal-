package com.controle.mensal

class ExpenseManager(
    private val repository: PamonhaRepository
) {

    private val expenses =
        repository.loadExpenses().toMutableList()

    fun getExpenses(): List<ExpenseRecord> {
        return expenses.toList()
    }

    fun addExpense(
        description: String,
        category: String,
        amountCents: Int
    ): Boolean {

        if (description.isBlank()) return false
        if (amountCents <= 0) return false

        val expense = ExpenseRecord(
            id = System.currentTimeMillis(),
            description = description.trim(),
            category = category,
            amountCents = amountCents,
            timestamp = System.currentTimeMillis()
        )

        expenses.add(expense)
        repository.saveExpenses(expenses)

        return true
    }

    fun removeExpense(id: Long): Boolean {
        val removed = expenses.removeAll {
            it.id == id
        }

        if (removed) {
            repository.saveExpenses(expenses)
        }

        return removed
    }

    fun getTotalExpenses(): Long {
        return expenses.sumOf {
            it.amountCents.toLong()
        }
    }
}
