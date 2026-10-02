# kitapcepte
KitapCepte, Jetpack Compose ve Material 3 bileşenleri temel alınarak geliştirilmiş bir Android kitap yönetim ve profil arayüzü uygulamasıdır. Proje; modern Android mimarisi, tek yönlü veri akışı (UDF) ve bileşen tabanlı UI pratiklerini uygulamak amacıyla geliştirilmiştir

## 📌 Genel Özellikler
* **Profil ve Hesap Yönetimi:** Kullanıcı bilgileri, güvenlik ayarları ve kayıtlı ödeme yöntemleri için modüler kart yapıları.
* **Okuma Hedefi Takibi:** Giriş yapmış kullanıcılar için yıllık hedefleri ve tamamlanan kitap oranını gösteren dinamik `LinearProgressIndicator` entegrasyonu.
* **Dinamik Misafir Modu:** Giriş yapmamış kullanıcıları karşılayan, uygulama özelliklerini özetleyen ve kayda teşvik eden özel durum arayüzü (`GuestBenefitsCard`).
* **İşlem Doğrulama:** Çıkış gibi kritik kullanıcı hareketleri için Compose tabanlı `AlertDialog` onay mekanizması.

## 🛠 Teknik Altyapı ve Teknolojiler

* **Dil:** Kotlin
* **UI Toolkit:** Jetpack Compose, Material 3
* **Mimari Yaklaşım:** MVVM & Unidirectional Data Flow (UDF)
* **Geliştirme Ortamı:** Android Studio Ladybug | 2024.2+
* **Versiyon Kontrol:** Git & GitHub

## 🏗 Proje Yapısı

kitapcepte/
├── core/                  # Temel tema, yardımcı sınıflar ve bileşenler
├── feature/
│   └── profile/           # Profil ekranı UI, state ve event tanımları
│       ├── ProfileScreen.kt
│       ├── ProfileUiState.kt
│       └── ProfileUiEvent.kt
└── README.md
