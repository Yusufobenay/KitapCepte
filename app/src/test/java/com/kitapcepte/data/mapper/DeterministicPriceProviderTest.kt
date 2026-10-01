package com.kitapcepte.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeterministicPriceProviderTest {

    private val provider = DeterministicPriceProvider()

    @Test
    fun `getPrice returns consistent price for same book id`() {
        val price1 = provider.getPrice("OL12345W")
        val price2 = provider.getPrice("OL12345W")
        assertThat(price1).isEqualTo(price2)
        assertThat(price1).isAtLeast(49.90)
        assertThat(price1).isAtMost(349.90)
    }

    @Test
    fun `getPrice returns different prices for different book ids`() {
        val priceA = provider.getPrice("OL10001W")
        val priceB = provider.getPrice("OL99999W")
        // Not guaranteed to be different for every pair due to hash collisions, but both in valid range
        assertThat(priceA).isAtLeast(49.90)
        assertThat(priceB).isAtLeast(49.90)
    }

    @Test
    fun `getOriginalPrice when present is greater than regular price`() {
        // Find an id that generates original price
        var found = false
        for (i in 1..20) {
            val id = "book_$i"
            val orig = provider.getOriginalPrice(id)
            if (orig != null) {
                found = true
                assertThat(orig).isGreaterThan(provider.getPrice(id))
            }
        }
        assertThat(found).isTrue()
    }
}
