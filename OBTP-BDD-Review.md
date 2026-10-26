# OBTP Smoke BDD Senaryoları — Review

**Genel değerlendirme:** Set iyi bir temel ve büyük kısmı BDD'ye uygun. Tek Given / tek When / tek Then kuralına ve `Verify` önekine neredeyse her yerde uyulmuş. Ama otomasyona almadan önce düzeltilmesi gereken üç tür sorun var:

1. Bazı adımlarda sayfa/modal eki eksik.
2. Bazı Given adımları birden fazla koşulu tek cümleye sıkıştırıyor.
3. Bazı senaryolarda adım atlanmış ya da veri belirsiz bırakılmış.

28 senaryodan ~12'si olduğu gibi kullanılabilir, kalanlar düzeltme istiyor.

---

## 1. Kurala aykırı genel sorunlar

**a) Sayfa/Modal eki eksik ya da yanlış yerde**

| Senaryo | Adım | Sorun |
|---|---|---|
| ASSIGNMENT-002 | `...on the confirmation dialog` | `on Confirmation Modal` olmalı |
| ASSIGNMENT-005, 007, 008, 009 | Given | Cümle sayfa adıyla bitmiyor |
| ASSIGNMENT-001, 004 | Given | Sayfa adı cümlenin ortasında kalmış |
| TECHNICIAN-LIST-API-001/002 | Given | `on ... Service` eki yok |

**b) Given içinde "and" ile birleştirilmiş birden fazla koşul**
"User is on Flights Page **and** a technician is..." gibi cümleler Given + And olarak bölünmeli. Kurallar And kullanımına zaten izin veriyor. En kötü örnek RESERVATION-001/002/003: tek adımda iki farklı sayfa var (Flights ve App Parameters).

**c) Navigasyon olmadan sayfa değişiyor**
- **ASSIGNMENT-004:** Kullanıcı Assign Technician Modal'dayken Then adımı `on Technicians Page` doğruluyor.
- **ASSIGNMENT-006:** Aynı sorun.
- **ROUTE-003:** Then/And adımları 3 farklı sayfada (Technicians, Routes, Flights) ama arada geçiş adımı yok.

Ya `And I navigate to X Page` adımı eklenmeli ya da doğrulama aynı ekranda yapılmalı.

**d) Şahıs tutarsızlığı:** Given'larda "User is...", When'lerde "I ..." kullanılmış. Tek birini seçin (tercihen "I").

**e) Etiketler**
- Set adı "Smoke" ama feature tag'i `@Regression`. `@Smoke` eklenmeli.
- `@KAIZEN-5030` (ASSIGNMENT-005 ve 008) ve `@KAIZEN-5207` (ASSIGNMENT-007 ve 009) iki senaryoda kullanılmış. Jira tarafında case bölünecek mi, yoksa iki senaryo aynı case'e mi bağlanacak, netleştirilmeli. Raporlamada tek case iki sonuç alır.

---

## 2. Senaryo bazında içerik sorunları

**FLIGHT-LISTING**
- **001–006 aynı kalıbın varyasyonları.** Gövde tipi, teknisyen, kapsam, toggle ve beklenen sonuç sütunlarıyla tek bir **Scenario Outline + Examples** olarak yazılması çok daha bakımı kolay olur.
- **007:** "technician **or** company coverage" belirsiz. Test hangi koşulla koşacak? İki ayrı satır/senaryo olmalı.
- **Terim tutarsızlığı:** "MOE toggle", "Turkish Technic MOE toggle" ve "AC Type toggle" aynı şeyi mi ifade ediyor? Tek terim kullanılmalı.
- **Given cümlesi:** "flight arrives ... on Stations Page" anlamsal olarak yanlış, uçuş Stations Page'de değil. Şöyle bölün: `Given Station X has an authorized technician ... on Stations Page` / `And A narrow body flight arrives at Station X on Flights Page`.

**ASSIGNMENT**
- **004:** Çok karmaşık. Hem pozitif hem negatif kontrol var, iki arama var, "B1 **or** A1+B2" koşullu doğrulama var. İki senaryoya bölün: eligible listeleniyor / non-matching listelenmiyor. Kategori kontrolünü de Examples'a taşıyın.
- **005:** Modal'ı açma adımı (`I click Assign Technician link`) eksik. "Rest period" süresi tanımsız, somut veri lazım (örn. "önceki uçuştan 4 saat sonra").
- **008:** `I click Assign button` adımı eksik. Atama yapılmadan "assigned successfully" doğrulanamaz.
- **001, 002, 003, 006, 007, 009:** Mantık olarak iyi. Negatif kontroller vacuous değil, çünkü Given içinde ilgili teknisyenin varlığı kurulmuş. Bu doğru bir yaklaşım.

**ROUTE**
- **003:** Smoke için fazla uzun. When/And adımlarının her biri 2–3 aksiyon içeriyor ("keep only the first leg **and** assign..."). Data table kullanın ya da CR1/CR2/CR3'ü ayrı senaryolara bölün. "Create Route" ile "Create Custom Route" adlandırması da tutarsız.
- **006:** "I update the route with Edit Route" çok belirsiz. Ne güncelleniyor (leg ekleme/çıkarma)? Then adımı da "updated leg list"i neye göre doğrulayacağını bilmiyor.
- **001/002:** "a flight card is available for a custom route" belirsiz bir veri koşulu. Hangi kriterle seçilecek?

**RESERVATION**
- **001:** "30 seconds after the assignment" sabit bekleme gibi okunuyor, flaky olur. `Verify ... within 30 seconds` (polling) olarak yazın. Ayrıca ilk Then zaten RESERVED doğruladığı için ikinci And kısmen tekrar.
- **003:** "each leg of the route" deniyor ama When tek uçuşa atama yapıyor. Çok bacaklı rota Given'da tanımlanmalı.
- **004:** "more than one technician" ve "each" ifadeleri adım içinde döngü anlamına geliyor. "two technicians" şeklinde somutlaştırın.

**API**
- Status code kontrolü yok (`Verify the response status code is 200`).
- **002:** "eligible for the VF flight" kriteri tanımsız. "rest period'daki teknisyen dönmüyor" kontrolü için de Given'da öyle bir teknisyenin var olduğu kurulmalı, yoksa test her zaman geçer.

---

## 3. Örnek düzeltme (ASSIGNMENT-008)

```gherkin
@GeneratedByQalityApp @KAIZEN-5030
Scenario: ASSIGNMENT-008: Assignment succeeds once the technician rest period has elapsed
  Given I am on Flights Page
  And A technician assigned to a previous flight has completed the required rest period before a later flight on Flights Page
  When I click Assign Technician link on the later flight card on Flights Page
  And I select that technician on Assign Technician Modal
  And I click Assign button on Assign Technician Modal
  Then Verify no rest period warning message is displayed on Assign Technician Modal
  And Verify the later flight card shows the technician name and register id on Flights Page
```

---

## 4. "Otomatize edilemez" listesi gözden geçirilmeli

Gerekçelerin bir kısmı otomasyon için engel değil, sadece UI testi için engel:
- **4350, 4516 (yalnızca DB doğrulaması var):** Framework'e DB step'i eklenirse otomatize edilebilir (`Verify ... on INTEGRATION_LOGS Table`).
- **4200, 4953 (mail):** Test mail kutusu (Graph API, Mailhog vb.) ile doğrulanabilir.
- **4885, 5362 (SMS):** SMS gateway logu veya mock üzerinden içerik ve teslim kontrol edilebilir.
- **Rapor case'leri (4407, 4501, 4604, 4669):** Notta önerilen sabit test verisi ve fixture yaklaşımı doğru.
- **Telex ve Netline case'leri:** Gerçekten harici sisteme bağlı, liste dışında kalmaları makul.
