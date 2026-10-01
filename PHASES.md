# Kitap Cepte — Faz Planı (her ekran bir faz)

Her faz sonunda: **derle → test → onaya sun → onay → commit + push → sonraki faz** (rules_skill).

| Faz | Kapsam | Ana çıktılar | Test |
|---|---|---|---|
| **0** | Proje kurulumu & temel altyapı | Gradle + version catalog, Hilt, tema (renk/tipografi/şekil), ortak component'lerin iskeleti, Navigation iskeleti, DispatcherProvider, Resource/UiText, Room+DataStore kurulumu, ktlint/detekt, git repo | Tema/Resource birim testleri, build |
| **1** | Onboarding (3 ekran) | HorizontalPager, sayfa göstergesi, “Atla/İleri/Başla”, onboarding bayrağı (DataStore) → Auth | `OnboardingViewModel` testi, ilk açılış yönlendirme testi |
| **2** | Login / Register | Giriş seçenekleri (E-posta / Misafir), Register akışı, hesap yoksa yönlendirme, Room users + hash, oturum (`Session`), `GuestGuard` + `MemberRequiredBottomSheet` | Validasyon, AuthRepository, SessionRepository, guard testleri |
| **3** | Home | Open Library entegrasyonu (DTO/mapper/repo/use case), kategori chip'leri, 2'li grid, `BookCard`, favori butonu, fiyat/Top Item politikası, loading/empty/error, BottomBar, TopBar (profil ikonu + “Kitap Cepte”) | Mapper, PriceProvider, repo (MockWebServer), HomeViewModel (filtre), UI testi |
| **4** | Detay | Geri/başlık/favori TopBar, kapak, ad, açıklama (works API), benzer kitaplar, fiyat + “Sepete ekle” | DetailViewModel, açıklama parse (string/obje), sepete ekleme |
| **5** | Favoriler | Favori Room akışı, Favoriler ekranı (aynı `BookCard`), Home/Detay ile senkron | FavoritesRepository, toggle testi |
| **6** | Sepet (Chart) | `CartItemCard`, adet artır/azalt, satır içi silme onayı, özet alanı (ara toplam, vergi, ödemeler), toplam butonu, sepet rozeti | Toplam/vergi hesabı, CartViewModel |
| **7** | Ödeme | Kart formu, Luhn/SKT/CVV doğrulama, sahte ödeme, başarı mesajı, sepeti temizle, Home'a dönüş | Validatorlar, PaymentViewModel, akış testi |
| **8** | Profil | Kullanıcı bilgileri, şifre değiştir, kayıtlı kartlar (maskeli), çıkış | ProfileViewModel, şifre değiştirme |
| **9** | Cila & Son Kontrol | Misafir akışı uçtan uca, hata/boş durumlar, erişilebilirlik, performans, tüm regresyon testleri | Uçtan uca UI testleri, lint |

## Bağımlılık sırası notu
- Favori ve sepet butonları Faz 3/4'te görünür; kalıcı işlevleri Faz 5/6'da tamamlanır. Faz 3'te Room altyapısı hazır olduğu için favori toggle Faz 3'te çalışabilir; Faz 5 ekranı ve senkronu tamamlar.
- `MemberRequiredBottomSheet` Faz 2'de yazılır; sonraki tüm fazlarda yeniden kullanılır.
