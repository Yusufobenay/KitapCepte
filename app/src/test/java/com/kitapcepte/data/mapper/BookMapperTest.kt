package com.kitapcepte.data.mapper

import com.google.common.truth.Truth.assertThat
import com.kitapcepte.data.remote.dto.BookDocDto
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.PriceProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class BookMapperTest {

    private val priceProvider: PriceProvider = mockk()
    private lateinit var mapper: BookMapper

    @Before
    fun setUp() {
        every { priceProvider.getPrice(any()) } returns 99.90
        every { priceProvider.getOriginalPrice(any()) } returns 149.90
        mapper = BookMapper(priceProvider)
    }

    @Test
    fun `mapToDomain correctly maps valid BookDocDto`() {
        val dto = BookDocDto(
            key = "/works/OL12345W",
            title = "The Hobbit",
            authorName = listOf("J.R.R. Tolkien"),
            coverId = 987654L,
            firstPublishYear = 1937,
            editionCount = 50
        )

        val book = mapper.mapToDomain(
            doc = dto,
            category = BookCategory.FANTASY,
            isTopItem = true,
            isFavorite = true
        )

        assertThat(book).isNotNull()
        assertThat(book?.id).isEqualTo("OL12345W")
        assertThat(book?.title).isEqualTo("The Hobbit")
        assertThat(book?.author).isEqualTo("J.R.R. Tolkien")
        assertThat(book?.coverUrl).isEqualTo("https://covers.openlibrary.org/b/id/987654-M.jpg")
        assertThat(book?.price).isEqualTo(99.90)
        assertThat(book?.originalPrice).isEqualTo(149.90)
        assertThat(book?.isTopItem).isTrue()
        assertThat(book?.isFavorite).isTrue()
        assertThat(book?.editionCount).isEqualTo(50)
        assertThat(book?.category).isEqualTo(BookCategory.FANTASY)
    }

    @Test
    fun `mapToDomain returns null when key or title is missing`() {
        val noKey = BookDocDto(key = null, title = "Title")
        val noTitle = BookDocDto(key = "/works/OL1W", title = null)
        val blankTitle = BookDocDto(key = "/works/OL1W", title = "   ")

        assertThat(mapper.mapToDomain(noKey, BookCategory.FANTASY)).isNull()
        assertThat(mapper.mapToDomain(noTitle, BookCategory.FANTASY)).isNull()
        assertThat(mapper.mapToDomain(blankTitle, BookCategory.FANTASY)).isNull()
    }

    @Test
    fun `mapListToDomain marks top 3 books by editionCount as topItem`() {
        val docs = listOf(
            BookDocDto(key = "/works/OL1W", title = "Book 1", editionCount = 10),
            BookDocDto(key = "/works/OL2W", title = "Book 2", editionCount = 50),
            BookDocDto(key = "/works/OL3W", title = "Book 3", editionCount = 30),
            BookDocDto(key = "/works/OL4W", title = "Book 4", editionCount = 5),
            BookDocDto(key = "/works/OL5W", title = "Book 5", editionCount = 100)
        )

        val result = mapper.mapListToDomain(
            docs = docs,
            category = BookCategory.SCI_FI,
            favoriteIds = setOf("OL2W")
        )

        assertThat(result).hasSize(5)
        // Top 3 should be OL5W (100), OL2W (50), OL3W (30)
        val topIds = result.filter { it.isTopItem }.map { it.id }
        assertThat(topIds).containsExactly("OL5W", "OL2W", "OL3W")

        // Favorite check
        val favorite = result.first { it.id == "OL2W" }
        assertThat(favorite.isFavorite).isTrue()
        val nonFavorite = result.first { it.id == "OL1W" }
        assertThat(nonFavorite.isFavorite).isFalse()
    }
}
