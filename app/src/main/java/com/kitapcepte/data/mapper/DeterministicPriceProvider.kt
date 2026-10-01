package com.kitapcepte.data.mapper

import com.kitapcepte.domain.model.PriceProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DeterministicPriceProvider @Inject constructor() : PriceProvider {

    override fun getPrice(bookId: String): Double {
        val hash = abs(bookId.hashCode())
        // 49.90 TL to 349.90 TL with 10 TL steps
        val step = hash % 31
        return 49.90 + (step * 10.0)
    }

    override fun getOriginalPrice(bookId: String): Double? {
        val hash = abs(bookId.hashCode())
        // Generate an original strikethrough price for roughly 1 in 3 books
        return if (hash % 3 == 0) {
            val basePrice = getPrice(bookId)
            val markup = ((hash % 4) + 2) * 15.0 // 30.0, 45.0, 60.0, 75.0 TL markup
            basePrice + markup
        } else {
            null
        }
    }
}
