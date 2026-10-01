---
name: tech_skill
description: Kitap Cepte projesinde kullanılacak teknoloji yığını, sürüm politikası ve bağımlılık kuralları. Yeni bağımlılık eklerken, Gradle yapılandırırken veya bir kütüphane seçerken kullan.
---

# Tech Skill — Kitap Cepte

## Dil ve Platform
- Kotlin (güncel stabil), tek modüllü Android projesi (`:app`). Multi-module YOK.
- minSdk 26, targetSdk/compileSdk güncel stabil. Java 17 toolchain.
- Gradle Kotlin DSL + **Version Catalog** (`gradle/libs.versions.toml`). Sürümler sadece orada tanımlanır.
- KSP kullan (kapt KULLANMA).

## UI
- **Jetpack Compose** + **Material 3** (Compose BOM ile sürüm yönetimi).
- **Navigation Compose** — type-safe route'lar (`@Serializable` route nesneleri).
- Coil 3 (Compose) — kapak görselleri.
- Edge-to-edge, `WindowInsets` doğru kullanımı.
- Ekran durumu: `collectAsStateWithLifecycle()`.

## Mimari Kütüphaneleri
- **Hilt** (DI) — `@HiltViewModel`, `@Module @InstallIn`.
- Kotlin Coroutines + Flow (`StateFlow`; tek seferlik event'ler için `Channel`).
- ViewModel + `SavedStateHandle`.

## Ağ
- Retrofit + OkHttp (logging interceptor sadece debug).
- **kotlinx.serialization** (Gson/Moshi KULLANMA), `ignoreUnknownKeys = true`.

## Yerel Veri
- **Room** — favoriler, sepet, kullanıcılar, kayıtlı kartlar (maskeli).
- **DataStore (Preferences)** — oturum, onboarding tamamlandı bayrağı, misafir modu.
- Şifre düz saklanmaz: salted hash (PBKDF2). Kart numarasının tamamı ASLA saklanmaz (son 4 hane + marka).

## Test (Test yazılacak)
- JUnit, **MockK**, **Turbine**, `kotlinx-coroutines-test`, Truth.
- Compose UI testleri: `ui-test-junit4` (kritik akışlar).
- Room: in-memory DB. Ağ: `MockWebServer`.

## Kalite
- ktlint/spotless + detekt Gradle task olarak.
- Deprecated API kullanma; emin değilsen güncel dokümantasyona bak.

## Yasaklar
- XML layout, Fragment, LiveData, RxJava, Gson, kapt, manuel singleton.
- `build.gradle.kts` içinde hardcoded sürüm numarası.
