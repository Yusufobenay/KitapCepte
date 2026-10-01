---
name: ui_skill
description: Kitap Cepte'nin tasarım sistemi — renkler, tipografi, ortak componentler, ekran düzenleri ve referans görsellere (main, card1, card2, detail) uyum kuralları. Herhangi bir Compose UI yazarken kullan.
---

# UI Skill — Kitap Cepte

Referans: `main.png` (Cart / Detail / Home), `card1.png` (ürün kartı), `card2.png` (sepet kartı), `Detail.png`.
Stil: yumuşak mor/indigo, cam (glass) hissi veren açık yüzeyler, yuvarlak köşeler, pill butonlar.

## Altın Kural: Tek Yerden Yönetim
Hiçbir ekranda hardcoded renk, font, boyut, string, köşe yarıçapı OLMAZ. Hepsi `core/designsystem` altında:
```
designsystem/
├── theme/  Color.kt, Type.kt, Shape.kt, Spacing.kt, Theme.kt (KitapCepteTheme)
└── component/  ortak componentler (aşağıda)
```
- Renkler: `AppColors` (ColorScheme + ek semantic renkler için `CompositionLocal`).
- Tipografi: tek `FontFamily` + `AppTypography`; font değişimi tek satır. Referans fontu geometrik sans (örn. DM Sans / Plus Jakarta Sans).
- String'ler: `strings.xml` (tek dil başlangıçta TR, EN hazır olabilir).
- Spacing/Shape: `Spacing.xs/sm/md/lg/xl`, `AppShapes` (pill, card=20dp, sheet=28dp).

## Renk Tokenları
| Token | Hex | Kullanım |
|---|---|---|
| primary | #5B3FE8 | ana butonlar, seçili chip/sekme, toplam fiyat butonu |
| primaryContainer | #E8E3FB | ikincil buton, “Sepette” zemini, seçili kart vurgusu |
| backgroundGradientStart | #6E7BE6 | arka plan gradyanı sol üst |
| backgroundGradientEnd | #A08BEF | arka plan gradyanı sağ alt |
| surface | #EEEBF8 | ekran/kart zemini |
| surfaceWhite | #FFFFFF | kartlar, butonlar |
| onSurface | #1F1B33 | başlık/metin |
| onSurfaceVariant | #8D8AA3 | açıklama, eski fiyat |
| badgeTopItem | #FFD93B | “Top Item” rozeti |
| badgeCart | #F04A3C | sepet bildirim rozeti |
Gradyan: `Brush.linearGradient` (topStart → bottomEnd), tüm ana ekranların zemini.

## Ortak Componentler (kod tekrarı yasak)
Her biri `core/designsystem/component` içinde, stateless, preview'lu:
- `AppTopBar` (slot'lu: leading / title / trailing) — Home: sağda profil ikonu, ortada “Kitap Cepte”; Detail: sol geri, orta başlık, sağ favori; Cart: orta başlık, sağ profil.
- `AppBottomBar` + `BottomBarItem` (seçili item pill: ikon + label, mor zemin; diğerleri sadece ikon; sepet rozeti).
- `AppChip` / `ChipRow` (selected/unselected; seçili = primary zemin beyaz yazı).
- `BookCard` (card1): kapak görseli, sağ üstte `FavoriteButton`, altında kitap adı (2 satır, ellipsis), altında `PriceButton` (buton metni = fiyat). “Top Item” rozeti opsiyonel. Sepette ise `PriceButton` “Sepette” durumuna geçer (primaryContainer).
- `CartItemCard` (card2): görsel, ad, alt metin, fiyat pill'i, adet seçici, silme ikonu; silmede satır içi “Sil? Hayır/Evet” onayı.
- `PrimaryButton`, `SecondaryButton`, `PriceButton` (ikonlu pill), `AppTextField`, `PromoField`.
- `FavoriteButton` (beyaz yuvarlak zemin, dolu/boş kalp).
- `AppBadge` (TopItem, cart count).
- `SummaryRow` / `OrderSummaryCard` (ara toplam, vergi, ödeme/komisyon, toplam).
- `MemberRequiredBottomSheet` (misafir için “Üye ol” sheet'i) — tek component, her yerden çağrılır.
- `LoadingView`, `ErrorView`, `EmptyView`, `AppSnackbar`.
- `BookImage` (Coil wrapper: placeholder/error/crossfade).

## Ekran Düzenleri
- **Onboarding (3):** tam ekran gradyan, illüstrasyon alanı, başlık+açıklama, sayfa göstergesi, “İleri/Atla”; son sayfa “Nasıl ilerleriz” + “Başla” → Auth.
- **Auth:** iki seçenek: “E-posta ile giriş” ve “Misafir olarak devam et”. Hesap yoksa Register'a yönlendirme linki/yönlendirme. Register: ad, e-posta, şifre (+tekrar).
- **Home:** TopBar → ChipRow → 2 sütunlu `LazyVerticalGrid` (BookCard) → BottomBar. Grid boşluğu sabit token.
- **Detail:** TopBar → geniş kapak kartı (Top Item rozeti opsiyonel) → başlık, açıklama (varsa), “Benzer kitaplar” yatay liste, altta sabit fiyat + “Sepete ekle” pill butonu.
- **Favoriler:** Home ile aynı grid + BookCard (kod tekrarı yok).
- **Cart:** TopBar → `CartItemCard` dikey liste → promosyon alanı (opsiyonel) → `OrderSummaryCard` → toplam fiyatlı primary buton (→ Ödeme).
- **Ödeme:** kart önizleme, kart sahibi, numara, SKT, CVV, “Öde”; başarıda başarı ekranı/dialog → Home.
- **Profil:** kullanıcı bilgileri, şifre değiştir, kayıtlı kartlar, çıkış.

## Kurallar
- Her composable `@Preview` (light) içerir; modifier parametresi ilk opsiyonel parametre.
- Dokunma alanı ≥ 48dp, `contentDescription` zorunlu (ikonlar).
- Animasyon sade: chip geçişi, buton durum değişimi, sheet.
- Yeni görsel ihtiyaçta önce mevcut component genişletilir; kopyalanmaz.
