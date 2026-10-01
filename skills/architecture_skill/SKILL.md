---
name: architecture_skill
description: Kitap Cepte için MVVM + Clean Architecture katman kuralları, paket yapısı, veri akışı ve test edilebilirlik standartları. Yeni bir feature, ViewModel, repository veya use case yazarken kullan.
---

# Architecture Skill — Kitap Cepte

Tek modül, **paket bazlı katman ayrımı**. MVVM + Clean (Presentation / Domain / Data), tek yönlü veri akışı (UDF).

## Paket Yapısı
```
com.kitapcepte
├── KitapCepteApp.kt, MainActivity.kt
├── core/
│   ├── designsystem/   # tema + ortak componentler (ui_skill)
│   ├── navigation/     # Routes, NavHost, GuestGuard
│   ├── common/         # Resource, UiText, DispatcherProvider, extensions
│   └── di/             # Hilt modülleri
├── data/
│   ├── remote/         # OpenLibraryApi, DTO'lar
│   ├── local/          # Room (db, dao, entity), DataStore
│   ├── mapper/         # DTO/Entity <-> Domain
│   └── repository/     # *RepositoryImpl
├── domain/
│   ├── model/          # Book, CartItem, User, SavedCard ...
│   ├── repository/     # interface'ler
│   └── usecase/        # tek sorumluluklu use case'ler
└── feature/
    └── onboarding/ auth/ home/ detail/ favorites/ cart/ payment/ profile/
        ├── XRoute.kt + XScreen.kt
        ├── XViewModel.kt
        └── XUiState.kt / XUiEvent.kt / XUiEffect.kt
```

## Katman Kuralları
- **Domain** saf Kotlin: Android, Retrofit, Room bağımlılığı YOK.
- **Data** domain interface'lerini uygular; DTO/Entity asla UI'a sızmaz.
- **Presentation** yalnızca use case/repository interface'i bilir.
- Bağımlılık yönü: `feature → domain ← data`.

## ViewModel Standardı
- `StateFlow<XUiState>` (immutable), `onEvent(XUiEvent)` girdi, `Channel<XUiEffect>` tek seferlik işler (navigasyon, snackbar).
- `XRoute` ViewModel'i bağlar; `XScreen` **stateless** ve preview edilebilir (state hoisting zorunlu).
- Coroutine'ler `viewModelScope`; dispatcher `DispatcherProvider` ile enjekte edilir.
- Hata: `Resource<T>` — UI'a ham exception gitmez, `UiText` gider.

## Use Case
- `operator fun invoke(...)`, tek iş. Salt pass-through ise use case üretme, repository kullan.

## Veri Kaynağı
- Kitap listesi uzaktan; favori/sepet Room'da (source of truth: Room).
- Liste ekranı: loading / content / empty / error durumları modellenir.

## Misafir Kuralı
- `SessionRepository.sessionState: Flow<Session>` → `LoggedIn | Guest | LoggedOut`.
- Yetki kontrolü tek yerde: `GuestGuard`. Misafir kısıtlı hedefe gitmek isterse route değişmez; ortak **“Üye Ol” bottom sheet** açılır.

## Test Edilebilirlik
- Her ViewModel, use case, repository, mapper için unit test.
- Fake repository'ler `test` kaynağında; MockK gerektiğinde.
- Zaman/dispatcher/ID üretimi enjekte edilir.
