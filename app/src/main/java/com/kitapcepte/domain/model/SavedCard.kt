package com.kitapcepte.domain.model

data class SavedCard(
    val id: Long = 0,
    val userId: Long,
    val cardHolder: String,
    val maskedNumber: String,
    val cardBrand: String,
    val expiryMonth: String,
    val expiryYear: String,
    val isDefault: Boolean = false
) {
    val formattedExpiry: String
        get() = "$expiryMonth/$expiryYear"
}
