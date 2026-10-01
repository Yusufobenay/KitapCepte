package com.kitapcepte.data.mapper

import com.kitapcepte.data.local.entity.SavedCardEntity
import com.kitapcepte.domain.model.SavedCard

fun SavedCardEntity.toDomain(): SavedCard = SavedCard(
    id = id,
    userId = userId,
    cardHolder = cardHolder,
    maskedNumber = maskedNumber,
    cardBrand = cardBrand,
    expiryMonth = expiryMonth,
    expiryYear = expiryYear,
    isDefault = isDefault
)

fun SavedCard.toEntity(): SavedCardEntity = SavedCardEntity(
    id = id,
    userId = userId,
    cardHolder = cardHolder,
    maskedNumber = maskedNumber,
    cardBrand = cardBrand,
    expiryMonth = expiryMonth,
    expiryYear = expiryYear,
    isDefault = isDefault
)
