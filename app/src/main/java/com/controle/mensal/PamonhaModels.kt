package com.controle.mensal

data class PamonhaFlavor(
    val id: Long,
    val name: String,
    val priceCents: Int,
    val stock: Int
)

data class SaleRecord(
    val id: Long,
    val flavorId: Long,
    val flavorName: String,
    val quantity: Int,
    val unitPriceCents: Int,
    val timestamp: Long
)

data class ExpenseRecord(
    val id: Long,
    val description: String,
    val category: String,
    val amountCents: Int,
    val timestamp: Long
)
