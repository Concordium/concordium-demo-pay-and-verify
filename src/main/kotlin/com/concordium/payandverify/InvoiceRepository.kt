package com.concordium.payandverify

class InvoiceRepository {

    private val invoicesById = mutableMapOf<String, Invoice>()

    fun addInvoice(invoice: Invoice) {
        check(!invoicesById.containsKey(invoice.id)) {
            "This invoice is already added"
        }
        invoicesById[invoice.id] = invoice
    }

    fun getInvoiceById(id: String): Invoice? =
        invoicesById[id]
}
