package com.kitapcepte.data.mapper

import com.kitapcepte.data.remote.dto.BookDocDto

object TopItemPolicy {
    fun getTopItemIds(docs: List<BookDocDto>, topN: Int = 3): Set<String> {
        return docs
            .filter { (it.editionCount ?: 0) > 0 && !it.key.isNullOrBlank() }
            .sortedByDescending { it.editionCount ?: 0 }
            .take(topN)
            .mapNotNull { it.key?.removePrefix("/works/") }
            .toSet()
    }
}
