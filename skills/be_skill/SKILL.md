---
name: be_skill
description: Kitap Cepte'nin backend/veri kaynağı kuralları — Open Library API entegrasyonu, DTO modeli, fiyat üretimi, kapak URL'leri ve yerel (mock) auth/ödeme davranışı. API, DTO, repository ve mapper yazarken kullan.
---

# Backend Skill — Kitap Cepte

## Kaynak
Open Library Search API (GET):
`https://openlibrary.org/search.json?q=subject:{subject}&limit=30`

```kotlin
interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,          // "subject:fantasy"
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0    // ileride sayfalama için
    ): SearchResponseDto

    @GET("works/{id}.json")                  // açıklama (opsiyonel, detay ekranı)
    suspend fun getWork(@Path("id") id: String): WorkDto
}
```
Base URL: `https://openlibrary.org/` (sabit, BuildConfig/DI'dan).

## DTO (kotlinx.serialization, tüm alanlar nullable/default)
```kotlin
@Serializable data class SearchResponseDto(
    val numFound: Int = 0,
    val start: Int = 0,
    val docs: List<BookDocDto> = emptyList()
)
@Serializable data class BookDocDto(
    val key: String? = null,
    val title: String? = null,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("edition_count") val editionCount: Int? = null,
    val language: List<String>? = null
)
```
`ia`, `ia_collection`, `cover_edition_key` vb. alanlar kullanılmaz (`ignoreUnknownKeys`).

## Domain Dönüşümü
- `key` ("/works/OL8400950W") → `id = "OL8400950W"`. `key` veya `title` null ise kayıt atılır.
- Kapak: `https://covers.openlibrary.org/b/id/{coverId}-M.jpg` (liste M, detay L). `coverId` yoksa placeholder.
- Mapper saf fonksiyon; unit test zorunlu.

## Fiyat (API fiyat vermez)
- **Deterministik sahte fiyat:** `id` hash'inden 49.90–349.90 TL, `PriceProvider` interface'i arkasında (ileride gerçek backend ile değişir). Aynı kitap hep aynı fiyatı alır.
- Eski fiyat (üstü çizili): hash'e göre bazı kitaplarda üretilir.
- “Top Item” rozeti: `editionCount` en yüksek ilk N kitap (`TopItemPolicy`).
- Açıklama: `works/{id}.json` → `description` (string veya `{value}` olabilir; ikisi de parse edilir). Yoksa açıklama bölümü gizlenir.
- “Benzer kitaplar”: aynı kategori sonuçlarından mevcut kitap hariç ilk N.

## Chip → Sorgu
Kategoriler tek yerde (`BookCategory` enum): Fantasy, Romance, Science Fiction, Mystery, History, Horror, Biography… Seçim → `subject:{slug}`. Tek seçim; seçili chip'e tekrar basmak varsayılana (fantasy) döner.

## Hata Politikası
- Timeout 15 sn, 1 retry. Hata → `Resource.Error(UiText)`; boş liste → Empty state.
- Aynı kategori için oturum içi in-memory cache.

## Yerel “Backend” (MVP)
Gerçek auth/ödeme sunucusu yok:
- **Auth:** Room `users` + PBKDF2 hash; oturum DataStore'da. `AuthRepository` arkasında, sonradan REST/Firebase ile değiştirilebilir.
- **Ödeme:** `PaymentRepository` sahte işlem (kısa gecikme, Luhn + son kullanma kontrolü). Kart no/CVV kaydedilmez, loglanmaz.
- Sepet, favoriler, kayıtlı kartlar (maskeli) Room'da.
