
package com.controle.mensal

class SalesManager(
    private val repository: PamonhaRepository
) {

    private val sales = repository.loadSales().toMutableList()
    private val flavors = repository.loadFlavors().toMutableList()

    fun getFlavors(): List<PamonhaFlavor> = flavors.toList()

    fun getSales(): List<SaleRecord> = sales.toList()

    fun addSale(flavorId: Long, quantity: Int = 1): Boolean {
        if (quantity <= 0) return false

        val index = flavors.indexOfFirst {
            it.id == flavorId
        }

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

        val index = flavors.indexOfFirst {
            it.id == sale.flavorId
        }

        if (index == -1) return false

        val flavor = flavors[index]

        flavors[index] = flavor.copy(
            stock = flavor.stock + sale.quantity
        )

        sales.remove(sale)

        repository.saveFlavors(flavors)
        repository.saveSales(sales)

        return true
    }

    fun updateStock(flavorId: Long, quantity: Int): Boolean {
        if (quantity < 0) return false

        val index = flavors.indexOfFirst {
            it.id == flavorId
        }

        if (index == -1) return false

        flavors[index] = flavors[index].copy(
            stock = quantity
        )

        repository.saveFlavors(flavors)
        return true
    }
}
