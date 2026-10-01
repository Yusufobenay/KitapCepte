package com.kitapcepte.domain.model

enum class BookCategory(val displayName: String, val slug: String) {
    FANTASY("Fantasy", "fantasy"),
    ROMANCE("Romance", "romance"),
    SCI_FI("Sci-Fi", "science_fiction"),
    MYSTERY("Mystery", "mystery"),
    HISTORY("History", "history"),
    HORROR("Horror", "horror"),
    BIOGRAPHY("Biography", "biography");

    val query: String get() = "subject:$slug"

    companion object {
        val DEFAULT = FANTASY

        fun fromSlug(slug: String): BookCategory =
            entries.firstOrNull { it.slug == slug } ?: DEFAULT
    }
}
