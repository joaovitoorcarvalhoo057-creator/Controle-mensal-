package com.controle.mensal

class SalesManager(
    private val repository: PamonhaRepository
) {

    private val sales = repository.loadSales().toMutableList()
    private val flavors = repository.loadFlavors().toMutableList()

    fun getFlavors(): List<PamonhaFlavor> = flavors.toList()

    fun getSales(): List<SaleRecord> = sales.toList()

    fun addFlavor(
        name: String,
        priceCents: Int,
        stock: Int
    ): Boolean {
        if (name.isBlank() || priceCents <= 0 || stock < 0) {
            return false
        }

        val flavor = PamonhaFlavor(
            id = (flavors.maxOfOrNull { it.id } ?: 0L) + 1L,
            name = name.trim(),
            priceCents = priceCents,
            stock = stock
        )

        flavors.add(flavor)
        repository.saveFlavors(flavors)
        return true
    }

    fun updateFlavor(
        id: Long,
        name: String,
        priceCents: Int,
        stock: Int
    ): Boolean {
        if (name.isBlank() || priceCents <= 0 || stock < 0) {
            return false
        }

        val index = flavors.indexOfFirst { it.id == id }
        if (index == -1) return false

        flavors[index] = flavors[index].copy(
            name = name.trim(),
            priceCents = priceCents,
            stock = stock
        )

        repository.saveFlavors(flavors)
        return true
    }

    fun deleteFlavor(id: Long): Boolean {
        if (sales.any { it.flavorId == id }) return false

        val removed = flavors.removeAll { it.id == id }

        if (removed) repository.saveFlavors(flavors)

        return removed
    }

    fun addSale(flavorId: Long, quantity: Int = 1): Boolean {
        if (quantity <= 0) return false

        val index = flavors.indexOfFirst { it.id == flavorId }
        if (index == -1) return false

        val flavor = flavors[index]
        if (flavor.stock < quantity) return false

        val sale = SaleRecord(
            id = System.currentTimeMillis(),
            flavorId = flavor.id,
            flavorName = flavor.name,
            quantity = quantity,
            unitPriceCents = flavor.priceCents,
            timestamp = System.currentTimeMillis()
        )

        flavors[index] = flavor.copy(
            stock = flavor.stock - quantity
        )

        sales.add(sale)

        repository.saveFlavors(flavors)
        repository.saveSales(sales)

        return true
    }

    fun undoLastSale(): Boolean {
        val sale = sales.lastOrNull() ?: return false
        val index = flavors.indexOfFirst { it.id == sale.flavorId }

        if (index == -1) return false

        flavors[index] = flavors[index].copy(
            stock = flavors[index].stock + sale.quantity
        )

        sales.remove(sale)

        repository.saveFlavors(flavors)
        repository.saveSales(sales)

        return true
    }

    fun updateStock(flavorId: Long, quantity: Int): Boolean {
        if (quantity < 0) return false

        val index = flavors.indexOfFirst { it.id == flavorId }
        if (index == -1) return false

        flavors[index] = flavors[index].copy(stock = quantity)
        repository.saveFlavors(flavors)

        return true
    }
}
