package com.kitapcepte.domain.model

interface PriceProvider {
    fun getPrice(bookId: String): Double
    fun getOriginalPrice(bookId: String): Double?
}
