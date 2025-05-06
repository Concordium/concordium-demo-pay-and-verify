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

    fun updateInvoiceStatusById(
        id: String,
        newStatus: Invoice.Status
    ) {
        val invoice = getInvoiceById(id)
            ?: error("Invoice $id not found")

        check(invoice.status is Invoice.Status.Pending) {
            "Invoice $id is no longer pending"
        }

        invoicesById[invoice.id] = invoice.copy(
            status = newStatus,
        )
    }

    fun getPaid(): List<Invoice> =
        invoicesById.values
            .filter { it.status is Invoice.Status.Paid }
}
