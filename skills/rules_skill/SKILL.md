---
name: rules_skill
description: Kitap Cepte geliştirme süreç kuralları — faz akışı, derleme/onay/commit/push protokolü, kod kalitesi ve yasaklar. Her görevin başında ve her faz bitişinde uygula.
---

# Rules Skill — Kitap Cepte

## Faz Akışı (ZORUNLU)
Her ekran = bir faz. Sırayla ilerle, faz atlama.
1. Faza başlamadan: ilgili skill'leri oku (ui, architecture, be, tech) ve faz kapsamını 5 satırda özetle.
2. Fazı uygula (kod + **testler**).
3. **Projeyi derle** (`./gradlew assembleDebug`) ve testleri çalıştır (`./gradlew testDebugUnitTest`, lint/ktlint/detekt).
4. Hatasız ise **kullanıcıya onaya sun**: yapılanlar, ekran görüntüsü/açıklama, test sonucu, bilinen eksikler.
5. **Onay gelmeden** sonraki faza GEÇME.
6. Onay sonrası: o fazın değişikliklerini **commit + push** et.
7. Sonra bir sonraki faza geç.

## Commit Kuralları
- Conventional Commits: `feat(home): ...`, `fix(cart): ...`, `test(detail): ...`, `chore: ...`.
- Bir faz = en az bir anlamlı commit; faz adı mesajda olsun: `feat(phase-3/home): add book grid and category chips`.
- Derlenmeyen / testi kırık kod commit'lenmez. Onaysız push YOK.
- `main` korunur; faz başına `feature/phase-N-isim` branch'i, onay sonrası merge/push (kullanıcı farklı söylemedikçe).

## Kod Kuralları
- Kod tekrarı yok: ortak UI → `designsystem/component`, ortak mantık → domain/common.
- Renk/font/string/ölçü hardcode edilmez (ui_skill).
- Katman kuralları ihlal edilmez (architecture_skill); DTO UI'a sızmaz.
- Public API'lerde KDoc sadece anlaşılması zor yerlerde; gereksiz yorum yok.
- `!!` kullanma, `GlobalScope` kullanma, UI içinde iş mantığı yazma.
- Deprecated/eski API kullanma. Emin olmadığın API'yi tahmin etme; dokümana bak.
- Hassas veri loglanmaz (şifre, kart).

## Test Kuralları
- Her fazda: ViewModel + use case + mapper/repository testleri (Turbine, MockK, coroutines-test).
- Kritik akış için en az bir Compose UI testi (Home filtre, misafir guard, sepet, ödeme).
- Faz “bitti” sayılmak için: build ✔, unit testler ✔, lint ✔.

## İletişim
- Varsayım yaparsan açıkça yaz ve onaya sun; belirsiz iş kuralında dur ve sor.
- Faz sonu raporu kısa ve madde madde: Yapılanlar / Test / Riskler / Sıradaki faz.
- Kapsam dışı iş ekleme (scope creep yok).

## Misafir Kuralı (her fazda kontrol et)
Misafir yalnızca Home listesi ve chip filtrelemesini kullanır. Detay, Favori, Profil, Sepet, Ödeme ve favoriye/sepete ekleme eylemleri → `MemberRequiredBottomSheet`.
