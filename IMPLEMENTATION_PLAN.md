# Kitap Cepte — Implementation Plan (Uygulamaya başlamadan önce onayınıza sunulur)

## 1. Hedef
Open Library verisiyle çalışan, Kotlin + Jetpack Compose, MVVM + Clean, test edilebilir bir kitap e-ticaret uygulaması. 10 faz (Faz 0–9), her faz sonunda derleme + test + onay + commit/push.

## 2. Varsayımlar (onayınızı istiyorum)
1. **Fiyat:** Open Library fiyat vermez → kitap `id`'sinden deterministik sahte fiyat üretilir (TL).
2. **Auth ve ödeme:** Gerçek sunucu yok; yerel (Room + DataStore) auth ve sahte ödeme. Arayüzler arkasında, sonradan gerçek backend'e geçirilebilir.
3. **Misafir yetkisi:** Misafir sadece Home listesi + chip filtreleme kullanabilir. Detay, Favori, Profil, Sepet, Ödeme ve favoriye/sepete ekleme → “Üye ol” bottom sheet.
4. **Bottom bar:** Metinde 3 sekme var (Home, Favori, Profil), referans görselde ise Sepet de sekme. **Sepete erişim için 4. sekme (Sepet, rozetli) eklemeyi öneriyorum.** Karar sizde: (a) 4 sekme, (b) 3 sekme + Detay'dan “Sepete git”.
5. **Detay ekranındaki sekmeler/renk/beden:** Referanstaki Reviews/Questions/renk/beden kitaplar için uygulanmaz; yalnızca açıklama, benzer kitaplar, fiyat.
6. **Kategoriler:** Chip listesi sabit bir enum'dur (Fantasy, Romance, Sci-Fi, Mystery, History, Horror, Biography); her chip `subject:{slug}` sorgusuna çevrilir.
7. **Dil:** TR arayüz (strings.xml), EN eklemeye hazır.
8. **Git:** Faz başına `feature/phase-N-*` branch'i; onay sonrası commit + push. Remote repo adresini sizden alacağım.

## 3. Mimari Özet
- Tek modül, paket bazlı katmanlar: `core / data / domain / feature` (architecture_skill).
- UDF: `UiState` + `UiEvent` + `UiEffect`, stateless screen'ler.
- Hilt DI, Room, DataStore, Retrofit + kotlinx.serialization, Coil, Navigation Compose (type-safe).

## 4. Tasarım Sistemi Özeti
- Tüm renk/font/şekil/spacing/string tek yerde (`core/designsystem`).
- Ortak component seti: TopBar, BottomBar, Chip, BookCard, CartItemCard, Price/Primary/Secondary button, Favorite button, Badge, Summary, MemberRequired sheet, Loading/Error/Empty.

## 5. Faz Detayları
Bkz. `PHASES.md`. Her faz için akış:
`skill oku → kapsam özeti → kodla + test yaz → assembleDebug + unit test + lint → onay iste → (onay) commit+push → sonraki faz`.

## 6. Test Stratejisi
- Unit: ViewModel (Turbine), use case, mapper, repository (MockWebServer/in-memory Room), validator'lar.
- UI: kritik akışlar (chip filtreleme, misafir guard, sepet, ödeme).
- Kalite kapısı: build ✔ + test ✔ + ktlint/detekt ✔.

## 7. Riskler
| Risk | Önlem |
|---|---|
| Open Library yavaş/limitli | Cache, timeout+retry, hata/boş durum UI |
| Bazı kitaplarda kapak/ad yok | Mapper'da eleme + placeholder |
| Açıklama alanı değişken formatta | `description` için özel serializer (string/obje) |
| Sahte fiyat gerçek değil | `PriceProvider` soyutlaması, dokümante edildi |
| Kapsam büyümesi | Faz onay kapısı, rules_skill |

## 8. Başlangıç Kontrol Listesi (Faz 0 öncesi)
- [ ] Varsayımlar (madde 2) onaylandı
- [ ] Bottom bar kararı (4 sekme / 3 sekme)
- [ ] Paket adı (öneri: `com.kitapcepte`) ve git remote adresi
- [ ] Android Studio/SDK/JDK 17 hazır
