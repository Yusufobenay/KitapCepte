package com.kitapcepte.domain.model

data class CartItem(
    val id: Long = 0,
    val bookId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val price: Double,
    val quantity: Int = 1
) {
    val totalPrice: Double get() = price * quantity
}
