package com.kitapcepte.data.mapper

import com.kitapcepte.data.remote.OpenLibraryApi
import com.kitapcepte.data.remote.dto.BookDocDto
import com.kitapcepte.domain.model.Book
import com.kitapcepte.domain.model.BookCategory
import com.kitapcepte.domain.model.PriceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookMapper @Inject constructor(
    private val priceProvider: PriceProvider
) {

    fun mapToDomain(
        doc: BookDocDto,
        category: BookCategory,
        isTopItem: Boolean = false,
        isFavorite: Boolean = false
    ): Book? {
        val rawKey = doc.key ?: return null
        val title = doc.title?.trim()
        if (title.isNullOrBlank()) return null

        val id = rawKey.removePrefix("/works/").trim()
        if (id.isEmpty()) return null

        val author = doc.authorName?.firstOrNull()?.trim() ?: "Bilinmeyen Yazar"
        val coverUrl = doc.coverId?.let { "${OpenLibraryApi.COVER_BASE_URL}$it-M.jpg" }
        val price = priceProvider.getPrice(id)
        val originalPrice = priceProvider.getOriginalPrice(id)

        return Book(
            id = id,
            title = title,
            author = author,
            coverUrl = coverUrl,
            price = price,
            originalPrice = originalPrice,
            isFavorite = isFavorite,
            isTopItem = isTopItem,
            editionCount = doc.editionCount ?: 0,
            firstPublishYear = doc.firstPublishYear,
            category = category
        )
    }

    fun mapListToDomain(
        docs: List<BookDocDto>,
        category: BookCategory,
        favoriteIds: Set<String> = emptySet()
    ): List<Book> {
        val topItemIds = TopItemPolicy.getTopItemIds(docs, topN = 3)
        return docs.mapNotNull { doc ->
            val rawId = doc.key?.removePrefix("/works/")?.trim()
            val isTopItem = rawId != null && rawId in topItemIds
            val isFavorite = rawId != null && rawId in favoriteIds
            mapToDomain(doc, category, isTopItem = isTopItem, isFavorite = isFavorite)
        }
    }
}
