# MASTER PROMPT — Kitap Cepte

Sen kıdemli bir Android geliştiricisisin. Aşağıdaki proje için çalışacaksın. Başlamadan önce `skills/` klasöründeki `ui_skill`, `architecture_skill`, `be_skill`, `tech_skill`, `rules_skill` dosyalarını oku ve bunlara kesin uy. Ayrıca `PHASES.md` ve `IMPLEMENTATION_PLAN.md` dosyalarını oku.

## 1. Proje
**Kitap Cepte** — kitap satışı yapan bir Android e-ticaret uygulaması. Kotlin + Jetpack Compose, MVVM + Clean, tek modül, test edilebilir, güncel teknolojiler. Testler yazılacak.

## 2. Ekranlar
1. **Onboarding (3 ekran):** ilk ikisi uygulama tanıtımı, üçüncüsü uygulamada nasıl ilerleneceği; sonunda Login/Register'a yönlendirir.
2. **Login/Register:** “E-posta ile giriş” veya “Misafir giriş”. Hesabı yoksa Register'a yönlendir.
3. **Home:** Top bar (sağ üstte profil ikonu, ortada “Kitap Cepte”) → seçili/seçili-değil chip filtreleri → 2 sütunlu grid kitap kartları (görsel, sağ üstte favori, isim, fiyat yazan buton) → Bottom bar (Home, Favori, Profil).
4. **Detay:** Top bar (geri, ekran adı, favori ekle/çıkar) → görsel, isim, açıklama (varsa), benzer kitaplar, fiyat ve sepete ekleme.
5. **Sepet (Chart):** Top bar (başlık, sağda profil) → yatay kartlı ürün listesi (görsel, isim, fiyat, adet, silme) → özet (ara toplam, vergi, ödemeler) → toplam fiyatlı buton → Ödeme.
6. **Ödeme:** kart bilgileri, ödeme, başarı mesajı, Home'a dönüş.
7. **Profil:** kullanıcı bilgileri, şifre, kartlar vb.

**Misafir kuralı:** Misafir yalnızca Home listesini kullanır; Detay, Favori, Profil ve diğer alanlarda “Üye ol” bottom sheet çıkar.

## 3. Tasarım
Referanslar: `main`, `card1`, `card2`, `Detail` görselleri. Renkler ve tokenlar `ui_skill`'dedir (Ana #5B3FE8, açık mor #E8E3FB, gradyan #6E7BE6→#A08BEF, yüzey #EEEBF8, beyaz, metin #1F1B33, ikincil #8D8AA3, Top Item #FFD93B, rozet #F04A3C).
Ortak component'ler tek yerde olacak; renk/font/string/ölçü tek yerden değişecek; kod tekrarı YOK.

## 4. Backend
`GET https://openlibrary.org/search.json?q=subject:fantasy&limit=30`. Response'taki `docs[]` alanlarından `key, title, author_name, cover_i, first_publish_year, edition_count, language` kullanılır. Kapak: `covers.openlibrary.org/b/id/{cover_i}-M.jpg`. Fiyat/auth/ödeme detayları `be_skill`'de.

## 5. Teknolojiler
Kotlin, Compose + Material3, Navigation Compose (type-safe), Hilt, Coroutines/Flow, Retrofit + OkHttp + kotlinx.serialization, Room, DataStore, Coil, JUnit + MockK + Turbine + MockWebServer, ktlint/detekt, Version Catalog. Detaylar `tech_skill`'de.

## 6. Çalışma Kuralları (özet — tam hali `rules_skill`)
- Başlamadan önce **implementation planını** sun ve onay al.
- Her ekran bir faz. Faz bitince: **derle + test** → **onaya sun** → onay gelmeden sonraki faza GEÇME.
- Her onaydan sonra o fazın değişikliklerini **commit + push** et (Conventional Commits).
- Belirsizlikte varsayım yapıp gizleme; sor veya açıkça yaz.
- Kapsam dışı iş ekleme.

## 7. İlk Görevin
1. Skill dosyalarını ve planları oku.
2. `IMPLEMENTATION_PLAN.md` içindeki varsayımları ve açık kararları (bottom bar, paket adı, git remote) bana sor.
3. Onayımdan sonra **Faz 0**'ı başlat. Faz 0 bitince derle, test et ve onayıma sun.
