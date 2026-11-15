package com.tarimmeteoroloji.ai.data

import com.tarimmeteoroloji.ai.model.BkuCategory
import com.tarimmeteoroloji.ai.model.BkuEntry
import com.tarimmeteoroloji.ai.model.CityPreset
import com.tarimmeteoroloji.ai.model.CropDiseaseDetail
import com.tarimmeteoroloji.ai.model.CropInfo
import com.tarimmeteoroloji.ai.model.CropPhenologyStep
import com.tarimmeteoroloji.ai.model.DiseaseProfile
import com.tarimmeteoroloji.ai.model.GuideSection
import com.tarimmeteoroloji.ai.model.GuideTopicButton
import com.tarimmeteoroloji.ai.model.GuideTopicId
import com.tarimmeteoroloji.ai.model.SymptomPattern

object CropCatalog {

    val crops: List<CropInfo> = listOf(
        CropInfo(
            id = "Bağ",
            emoji = "🍇",
            name = "Bağ (Üzüm)",
            colorHex = 0xFF6A1B9A,
            subtitle = "Sofralık, kurutmalık ve şaraplık asma yetiştiriciliği rehberi",
            climateAndSoil = "Yıllık 200–400 saat kış soğuklaması ister; gelişme döneminde bol güneş ve 1.600–2.000 gün-derece toplam sıcaklık gerektirir. İdeal toprak pH’ı 6.0–7.5 aralığındadır; kireçli topraklarda kloroza dayanıklı anaç (ör. 1103P, 140Ru) seçilmelidir.",
            frostAndTempThresholds = "Kış dinlenmesinde -15°C’ye dayanır; ancak ilkbaharda gözler kabarıp yeşil uç çıktığında -1.5°C / -2°C don sürgünleri yakar. 35°C üzeri sıcakta salkımlarda güneş yanıklığı görülür.",
            waterNeedsAndIrrigation = "Yıllık su ihtiyacı 500–700 mm’dir. Çiçeklenme sırasında sulama yapılmaz (çiçek silker). Tane tutumu ve iri koruk döneminde damlama sulama ile düzenli nem sağlanır; ben düşme sonrası şeker birikimi için su azaltılır.",
            fertilizationProgram = "Kış sonu dekara 2–3 ton yanmış ahır gübresi + taban fosfor/potasyum; sürgünler 25 cm iken ilk azot bölümü; tane tutumu sonrası potasyum sülfat ve magnezyum/bor yaprak takviyesi önerilir. Aşırı azot külleme ve kurşuni küfü artırır.",
            harvestAndStorage = "Sofralık çeşitlerde Briks %16–18, çekirdeğin kahverengileşmesi ve sapın odunlaşması esas alınır. Sabah serinliğinde hasat edilip 0°C ve %90 nemde ön soğutma ile depolanır.",
            criticalStage = "Sürgün gelişimi, çiçeklenme ve ben düşme dönemleri kritik",
            diseases = "Külleme (Uncinula necator), Mildiyö (Plasmopara viticola), Kurşuni Küf, Salkım Güvesi",
            symptoms = "Yaprak üstünde unsu beyaz tabaka (külleme), yaprak altında yağ lekesi ve beyaz miselyum (mildiyö), tanelerde çürüme.",
            management = "• Asma tacını havalandıracak yeşil budama ve yaprak alma yapın.\n• Yağış sonrası yaprak ıslaklığı uzarsa mildiyö riskini yakından izleyin.\n• Kükürt uygulamalarında 30°C üzeri sıcaklıklarda yakıcılık riskine dikkat edin.",
            activeIngredientGroups = "Kükürt (külleme, <30°C), Bakırlı / Kontak & Sistemik Mildiyö fungisitleri (FRAC rotasyonlu), Salkım güvesi için feromon takibi + Bt veya seçici larvisitler.",
            monitoringThreshold = "10-10-24 kuralı (≥10 cm sürgün, ≥10 mm yağış, ≥10°C) sonrası mildiyö kontrolü; feromon tuzağında kelebek pikinden 7–10 gün sonra larva kontrolü.",
            irrigationAndNutrition = "Çiçeklenme döneminde aşırı sulama silkmeye yol açar. Ben düşmeden sonra potasyum desteği tane şekeri ve kabuk direncini artırır; aşırı azot küllemeyi tetikler.",
            phenologySteps = listOf(
                CropPhenologyStep("Kış Budaması & Göz Uyanması", "Şubat – Mart", "Ölü odun temizliği, bordo bulamacı ile kış dezenfeksiyonu"),
                CropPhenologyStep("Sürgün 10–25 cm & Çiçek Öncesi", "Nisan – Mayıs", "Mildiyö (10-10-24 kuralı) ve ilk külleme kükürt uygulaması"),
                CropPhenologyStep("Çiçeklenme & Tane Tutumu", "Mayıs Sonu – Haziran", "Arı/tozlaşma için bakır verilmez; salkım güvesi 1. döl ve koruk ilaçlaması"),
                CropPhenologyStep("Ben Düşme & Olgunlaşma", "Temmuz – Ağustos", "Yaprak alma, kurşuni küf önlemi ve son ilaçlama-hasat aralığı (PHI) takibi")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Bağ Küllemesi (Uncinula necator)",
                    agent = "Fungal Etmen — Kuru ve ılık havalarda bile gelişir",
                    visualSymptomKind = SymptomPattern.POWDERY_WHITE,
                    visualBadge = "Beyaz Unsu Kaplama",
                    symptoms = "Yaprak üst yüzeyinde, sürgünlerde ve yeşil tanelerde beyaz-gri pudra gibi unsu tabaka; ilerleyince taneler çatlar ve çekirdek görünür.",
                    favorableWeather = "20–28°C sıcaklık, gölgeli/sık asma tacı; yağış şart değildir, yüksek nem yeterlidir.",
                    sprayAction = "Sürgünler 20–25 cm iken Mikronize Kükürt (<30°C) ile başlayın; çiçeklenme öncesi/sonrası FRAC Grup 3 / Grup 11 / Grup 13 sistemik fungisitlerle rotasyon yapın."
                ),
                CropDiseaseDetail(
                    name = "Bağ Mildiyösü (Plasmopara viticola)",
                    agent = "Oomycete — Yağış ve yaprak ıslaklığına bağlı salgın yapar",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Sarı Yağ Lekesi & Alt Küf",
                    symptoms = "Yaprak üstünde sarımsı şeffaf 'yağ lekesi', tam altında nemli havalarda beyaz pamuksu miselyum örtüsü; salkımlarda kuruma ve meşinleşme.",
                    favorableWeather = "10-10-24 kuralı: Sürgün ≥10 cm, ≥10 mm yağış ve sıcaklık ≥10°C olduğunda 5–7 gün içinde belirti çıkar.",
                    sprayAction = "Yağış öncesi Bakırlı veya Çok Bölgeli Kontak (FRAC M) koruyucu; salgın riski yüksekse yağış sonrası ilk pencere içinde Sistemik/Translaminar mildiyö fungisiti uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Kurşuni Küf & Salkım Güvesi",
                    agent = "Botrytis cinerea + Lobesia botrana (Böcek yaraları küfü tetikler)",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Tanelerde Çürüme & Güve",
                    symptoms = "Olgunlaşan salkımlarda larva deliği, örümcek ağımsı iplikçik ve tanelerde kahverengi-gri kadifemsi çürüklük.",
                    favorableWeather = "Ben düşme sonrası yağış ve %80+ nemde salkım içi sıkışıklık küfü hızlandırır.",
                    sprayAction = "Salkım güvesi için feromon tuzağı takibiyle yumurta/larva çıkışında Bacillus thuringiensis (Bt) veya seçici larvisit; ben düşmede havalandırma + anti-botrytis fungisit."
                )
            ),
            tempRiskMin = 18.0,
            tempRiskMax = 28.0,
            humidityThreshold = 75
        ),
        CropInfo(
            id = "Zeytin",
            emoji = "🫒",
            name = "Zeytin",
            colorHex = 0xFF33691E,
            subtitle = "Sofralık ve yağlık zeytin bahçesi bakım ve koruma rehberi",
            climateAndSoil = "Akdeniz ve Güneydoğu Anadolu’nun sıcak iklimine uyumludur; yıllık ortalama 15–20°C ve 250–600 saat kış soğuklaması ister. Taşlı, süzek, hafif kireçli pH 6.5–8.0 topraklarda verimli yetişir; taban suyu tutan ağır killi topraklardan hoşlanmaz.",
            frostAndTempThresholds = "-7°C altında genç sürgünlerde don çatlağı başlar; -12°C altında gövde zarar görür. Don ve dolu yaraları Zeytin Dal Kanseri bakterisinin ana giriş kapısıdır. Çiçeklenmede 32°C üstü ve kuru lodos rüzgârı döllenmeyi düşürür.",
            waterNeedsAndIrrigation = "Kurağa dayanıklı olsa da çekirdek sertleşmesi (Haziran sonu) ve yağ birikimi/meyve irileşmesi (Ağustos–Eylül) dönemlerinde sulama verimi ve yağ kalitesini %30–40 artırır. Yıllık toplam 450–650 mm su yeterlidir.",
            fertilizationProgram = "Kışın tabana fosfor ve potasyum; uyanma öncesi (Şubat–Mart) azotun 2/3’ü, meyve tutumunda 1/3’ü verilir. Çiçek öncesi yapraktan Bor + Çinko uygulaması tane tutumunu belirgin artırır.",
            harvestAndStorage = "Yeşil sofralık için meyve rengi saman sarısına döndüğünde, yağlık için meyve etinin %60–70’i karardığında elle veya hasat makinesiyle (dal kırmadan) toplanır; 24 saat içinde sıkıma gitmelidir.",
            criticalStage = "Gözlerin uyanması, çiçeklenme ve meyve tutumu / kararma dönemi",
            diseases = "Halkalı Leke (Spilocaea oleagina), Zeytin Dal Kanseri, Zeytin Sineği, Pamuklu Bit",
            symptoms = "Yaprak üst yüzeyinde iç içe geçmiş koyu halkalı lekeler ve erken yaprak dökümü; sürgünlerde siğil benzeri urlar.",
            management = "• Taç içi hava sirkülasyonu için düzenli budama yapın; budama aletlerini dezenfekte edin.\n• Dolu veya don zararı sonrası bakırlı preparat zamanlamasını değerlendirin.\n• Hasat sonrası bordo bulamacı uygulamasını nemli sonbahar dönemine göre planlayın.",
            activeIngredientGroups = "Bakırlı preparatlar / Bordo bulamacı (halkalı leke ve dal kanseri), Zeytin sineği için cezbedici + ruhsatlı yem dal / kaplama preparatlar.",
            monitoringThreshold = "Yaz sonu – sonbahar serin ve nemli günlerde tuzaklarda sinek artışı ve meyvede %1–2 vuruk eşiği; ilkbahar yağışlarından önce halkalı leke koruyucusu.",
            irrigationAndNutrition = "Çekirdek sertleşmesi ve yağ birikimi döneminde su stresi dane iriliğini ve yağ oranını düşürür. Bor ve çinko eksikliği çiçek tutumunu azaltır.",
            phenologySteps = listOf(
                CropPhenologyStep("Budama & Kış Sonu", "Şubat – Mart", "Taç içi aralama budaması, hemen ardından %1.5–2 Bordo Bulamacı"),
                CropPhenologyStep("Sürgün & Çiçek Salkımı", "Nisan – Mayıs", "Pamuklu bit kontrolü, Bor + Çinko yaprak gübresi, ilkbahar halkalı leke bakırı"),
                CropPhenologyStep("Çekirdek Sertleşmesi & Yaz", "Haziran – Ağustos", "Zeytin sineği tuzak takibi ve düzenli damlama sulama"),
                CropPhenologyStep("Meyve Kararması & Hasat", "Eylül – Kasım", "Zeytin sineği koruması, hasat sonrası dolu/yaraya karşı sonbahar bakırı")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Zeytin Halkalı Leke (Spilocaea oleagina)",
                    agent = "Fungal Etmen — Kış ve ilkbahar yaprak dökümüne yol açar",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "İç İçe Halkalı Leke",
                    symptoms = "Yaprak üst yüzeyinde dairesel, ortası koyu yeşil/siyah çevresi sarı halka ile çevrili 'tavus kuşu gözü' lekeleri ve şiddetli yaprak dökümü.",
                    favorableWeather = "10–20°C serin hava ve %80+ nem / yağış; sık taçlı ve çukur bahçelerde daha şiddetlidir.",
                    sprayAction = "Sonbahar yağışları başlamadan (%1–1.5) ve ilkbaharda yeni sürgünler çıkmadan (%1) Bordo Bulamacı veya Bakır Hidroksit uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Zeytin Dal Kanseri (Pseudomonas savastanoi)",
                    agent = "Bakteriyel Etmen — Don, dolu ve budama yaralarından girer",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Sürgünde Siğil / Ur",
                    symptoms = "Genç dal ve sürgünlerde süngerimsi, çatlaklı şişkinlikler (urlar); zamanla dal kurur ve verim düşer.",
                    favorableWeather = "Sonbahar-kış yağışları, don çatlakları, dolu ve sırıkla yapılan yaralayıcı hasat.",
                    sprayAction = "Urları sağlıklı dokuya kadar temizleyip yakın; aletleri %10 çamaşır suyuyla dezenfekte edin ve budama/dolu sonrası 48 saat içinde Bakırlı preparat atın."
                ),
                CropDiseaseDetail(
                    name = "Zeytin Sineği & Pamuklu Bit",
                    agent = "Bactrocera oleae & Euphyllura olivina",
                    visualSymptomKind = SymptomPattern.POWDERY_WHITE,
                    visualBadge = "Pamukçuk & Meyve Vuruğu",
                    symptoms = "Baharda çiçek salkımlarında beyaz pamuksu salgı (pamuklu bit); yaz sonu meyvede üçgen vuruk izi, kurtlanma ve yağ asitliğinde artış.",
                    favorableWeather = "Zeytin sineği 20–30°C ve yüksek yaz sonu neminde ürer; 35°C üstü kuru sıcakta azalır.",
                    sprayAction = "Tuzaklarda sinek eşiği ve %1–2 vuruk görüldüğünde ruhsatlı yem-dal ilaçlaması (cezbedici + seçici insektisit) yapın; hasat aralığına uyun."
                )
            ),
            tempRiskMin = 12.0,
            tempRiskMax = 24.0,
            humidityThreshold = 80
        ),
        CropInfo(
            id = "Badem",
            emoji = "🌰",
            name = "Badem",
            colorHex = 0xFF6D4C41,
            subtitle = "Erken çiçeklenen sert kabuklu meyve yetiştiriciliği ve don/hastalık yönetimi",
            climateAndSoil = "En erken çiçek açan meyve türüdür; 200–450 saat soğuklama ister. Derin, geçirgen, kumlu-tınlı ve pH 6.5–7.8 topraklarda mükemmel gelişir. Su tutan ağır topraklarda kök çürüklüğü hızla görülür.",
            frostAndTempThresholds = "İlkbahar geç donları en büyük risktir: Pembe tomurcuk -3°C’ye, tam çiçeklenme -2°C’ye, yeni tutmuş küçük çağla ise yalnızca -0.5°C / -1°C’ye dayanabilir.",
            waterNeedsAndIrrigation = "Kurağa dayanıklı görünse de ticari verim için yıllık 600–800 mm su gerekir. Özellikle çağla irileşmesi ve iç dolumu döneminde (Nisan–Temmuz) düzenli damlama sulama boş meyve oranını azaltır.",
            fertilizationProgram = "Çiçek öncesi azot + fosfor; meyve büyüme döneminde potasyum ve azotun kalan kısmı; hasat sonrası (Eylül) yapraklar dökülmeden verilen azot-bor-çinko ertesi yılın çiçek gözlerini besler.",
            harvestAndStorage = "Yeşil dış kabuk (meşin kabuk) ağaçta %70–80 oranında çatlayıp açıldığında hasat edilir; kabuğu soyulup gölgede %5–6 neme kadar kurutularak serin ve kuru depoda saklanır.",
            criticalStage = "Pembe tomurcuk, tam çiçeklenme ve çağla dönemi",
            diseases = "Monilya (Çiçek Yanıklığı), Yaprak Delen (Çil), Dal Kanseri, Badem İç Kurdu",
            symptoms = "Çiçek ve genç sürgünlerde ani kahverengileşme ve zamk akıntısı; yapraklarda kırmızımsı lekeler ve delinmeler.",
            management = "• Çiçeklenme döneminde yağış ve yüksek nem varsa Monilya riski yüksektir.\n• Mumyalaşmış meyveleri ve kuru dalları kış budamasında bahçeden uzaklaştırın.\n• İlkbahar geç donlarına karşı meteorolojik uyarıları gece saatlerinde takip edin.",
            activeIngredientGroups = "Kış sonu bakırlı uygulama (yaprak delen), çiçek başlangıcı/tam çiçekte arıya zararsız ruhsatlı Monilya fungisitleri, çağla çıkışında iç kurdu takibi.",
            monitoringThreshold = "Çiçeklenme döneminde 10–20°C sıcaklık + yağış/çiğ varsa Monilya için koruyucu pencere kritik; sonbaharda ağaçta kalan bademleri mutlaka toplayın.",
            irrigationAndNutrition = "Kök boğazında su birikmesine karşı hassastır (ağır toprakta Phytophthora riski). Hasat sonrası azot-fosfor rezerv gübrelemesi gelecek yıl çiçek gözlerini güçlendirir.",
            phenologySteps = listOf(
                CropPhenologyStep("Kış Dinlenmesi & Budama", "Ocak – Şubat", "Mumya bademlerin toplanması ve gözler kabarmadan %2 Bordo Bulamacı"),
                CropPhenologyStep("Pembe Tomurcuk & Çiçeklenme", "Şubat Sonu – Mart", "Monilya (çiçek yanıklığı) koruması ve ilkbahar geç donu takibi"),
                CropPhenologyStep("Çağla & İç Dolumu", "Nisan – Haziran", "Yaprak delen kontrolü, Badem iç kurdu takibi ve düzenli damlama sulama"),
                CropPhenologyStep("Kabuk Çatlaması & Hasat", "Ağustos – Eylül", "Hasat, dış kabuk soyma, kurutma ve hasat sonrası rezerv besleme")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Monilya / Çiçek Yanıklığı (Monilinia laxa)",
                    agent = "Fungal Etmen — Çiçekten sürgüne geçerek dalı kurutur",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Çiçek Kuruması & Zamk",
                    symptoms = "Çiçeklerin birden yanmış gibi kahverengileşip dal üzerinde kuruması, sürgünlerde zamk akıntısı ve mumyalaşmış meyveler.",
                    favorableWeather = "Çiçeklenme sırasında 10–20°C sıcaklık ile birlikte yağış, sis veya yoğun çiğ.",
                    sprayAction = "1. uygulama %5–10 çiçek açımında (pembe tomurcuk sonu), 2. uygulama %90–100 tam çiçeklenmede arılara zararsız ruhsatlı Monilya fungisiti ile yapılır."
                ),
                CropDiseaseDetail(
                    name = "Yaprak Delen / Çil Hastalığı",
                    agent = "Wilsonomyces carpophilus — Yaprak ve meyvede çil yapar",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Kırmızı Leke & Delinme",
                    symptoms = "Yapraklarda kenarı kırmızı-mor ortası kuruyup dökülen saçma deliği görünümü; meyvede kabuk üstünde siğilimsi lekeler ve zamk.",
                    favorableWeather = "İlkbahar yağışları ve serin-nemli günler (%75+ nem).",
                    sprayAction = "Sonbaharda yaprak dökümünde ve ilkbaharda gözler uyanmadan Bordo Bulamacı; çiçek taç yaprakları dökülünce gerekirse koruyucu fungisit."
                ),
                CropDiseaseDetail(
                    name = "Badem İç Kurdu & Kırmızı Örümcek",
                    agent = "Eurytoma amygdali & Tetranychus spp.",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Yaprak Bronzlaşma / Kurt",
                    symptoms = "İç kurdunun yumurtladığı çağlalar kararır ve ağaçta asılı kalır; kırmızı örümcek yaz sıcağında yaprakları sarı-bronz renge çevirir.",
                    favorableWeather = "İç kurdu erginleri Nisan–Mayıs sıcak günlerinde çıkar; örümcek 28°C+ kuru sıcakta çoğalır.",
                    sprayAction = "Ağaçta kalan eski bademleri kışın mutlaka toplayıp imha edin; çağla döneminde ergin çıkışı tespit edilirse seçici insektisit, yazın gerekirse akarisit uygulayın."
                )
            ),
            tempRiskMin = 10.0,
            tempRiskMax = 22.0,
            humidityThreshold = 78
        ),
        CropInfo(
            id = "Buğday",
            emoji = "🌾",
            name = "Buğday",
            colorHex = 0xFF2E7D32,
            subtitle = "Ekmeklik ve makarnalık buğdayda verim, pas ve süne yönetimi",
            climateAndSoil = "Serin iklim tahılıdır; çimlenme 4–20°C, kardeşlenme 6–12°C serinlik sever. Tınlı, killi-tınlı pH 6.0–7.5 topraklar idealdir. Sapa kalkma ve başaklanma döneminde nem ve 18–25°C sıcaklık tane sayısını belirler.",
            frostAndTempThresholds = "Kar örtüsü altında -20°C’ye dayanır; ancak başaklanma sonrası -2°C don başaklarda kısırlık (beyaz başak) yapar. Süt olum döneminde 33°C üzeri sıcak ve kuru rüzgâr (sam yeli) daneyi buruşturur.",
            waterNeedsAndIrrigation = "Yıllık 450–600 mm yağış/sulama ister. En kritik sulama dönemleri: 1) Sapa kalkma, 2) Başaklanma öncesi, 3) Süt olum dönemi. Bu dönemlerdeki su stresi verimi %35’e kadar düşürür.",
            fertilizationProgram = "Ekimde toprak analizine göre taban gübresi (DAP/20-20-0); kardeşlenme başlangıcında (Şubat–Mart) üre veya amonyum sülfat, sapa kalkmada (Nisan) ikinci azot dozu verilir. Çinko eksikliğinde kardeşlenme zayıflar.",
            harvestAndStorage = "Dane nemi %12–13’e düştüğünde (sert olum / tam olum, tırnakla kırılma kıvamı) biçerdöverle hasat edilir; %12 altı nemde serin ve haşeresiz siloda depolanır.",
            criticalStage = "Kardeşlenme, sapa kalkma ve başaklanma dönemleri",
            diseases = "Sarı/Kahverengi Pas, Septorya Yaprak Lekesi, Kök ve Kökboğazı Çürüklüğü, Süne",
            symptoms = "Yapraklarda sarı-turuncu pas püstülleri, nekrotik lekeler, kök boğazında kararma ve erken beyaz başak oluşumu.",
            management = "• Sertifikalı ve ilaçlı tohum kullanın; aşırı azotlu gübrelemeden kaçının.\n• Nisan-Mayıs yağışlarıyla birlikte yaprak hastalıkları ve süne sürveylerini sıklaştırın.\n• Ekim nöbeti (münavebe) uygulayarak toprak kökenli fungal yükü azaltın.",
            activeIngredientGroups = "Tohum ilaçlaması (rastık/sürme), kardeşlenme sonu seçici herbisit, bayrak yaprak döneminde Triazol/Strobilurin pas-septorya fungisitleri.",
            monitoringThreshold = "Bayrak yaprağın korunması verimin %40’ından fazlasını belirler. Yaprakta ilk pas püstülleri görüldüğünde ve süne nimf eşiği aşıldığında müdahale edin.",
            irrigationAndNutrition = "Taban gübresi (DAP/20-20) ekimde, azotlu üst gübreleme kardeşlenme ve sapa kalkma başlangıcında bölünerek verilmelidir. Süt olum döneminde su stresi hektolitre ağırlığını düşürür.",
            phenologySteps = listOf(
                CropPhenologyStep("Ekim & Çıkış", "Ekim – Kasım", "İlaçlı sertifikalı tohum + taban fosfor-çinko gübresi"),
                CropPhenologyStep("Kardeşlenme", "Şubat – Mart", "Üst azotun 1. yarısı ve yabancı ot (herbisit) uygulaması"),
                CropPhenologyStep("Sapa Kalkma & Bayrak Yaprak", "Nisan – Mayıs", "Pas ve septorya sürveyi; gerekirse bayrak yaprak fungisiti"),
                CropPhenologyStep("Başaklanma, Süt Olum & Hasat", "Mayıs Sonu – Haziran", "Süne nimf kontrolü ve dane %13 nemde biçerdöver hasadı")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Sarı Pas & Kahverengi Pas",
                    agent = "Puccinia striiformis / Puccinia triticina (Rüzgârla hızla yayılır)",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Turuncu-Sarı Çizgi Püstül",
                    symptoms = "Yaprak boyunca dikiş makinesi izi gibi sarı-turuncu tozlu pas şeritleri (sarı pas) veya dağınık kahverengi kabarcıklar.",
                    favorableWeather = "Sarı pas 10–18°C serin ve çiğli/yağışlı bahar günlerinde; kahverengi pas 18–25°C’de salgın yapar.",
                    sprayAction = "Bayrak yaprakta ilk püstüller görüldüğünde Triazol (FRAC 3) veya Triazol + Strobilurin (FRAC 3+11) sistemik fungisiti uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Septorya Yaprak Lekesi & Kökboğazı Çürüklüğü",
                    agent = "Zymoseptoria tritici / Fusarium spp.",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Nekrotik Leke / Siyah Piknid",
                    symptoms = "Alt yapraklardan yukarı tırmanan kahverengi kurumalar ve leke içinde toplu iğne ucu kadar siyah noktalar; kök boğazında kararma.",
                    favorableWeather = "Sık ekim, aşırı azot ve Nisan-Mayıs sağanak yağışları (yağmur sıçramasıyla üst yaprağa taşınır).",
                    sprayAction = "Ekim nöbeti esastır; bayrak yaprak lekelenmeden koruyucu-sistemik Septorya ruhsatlı fungisit uygulaması yapılır."
                ),
                CropDiseaseDetail(
                    name = "Süne & Ekin Kambur Böceği",
                    agent = "Eurygaster integriceps (Emgi sonucu un kalitesini bozar)",
                    visualSymptomKind = SymptomPattern.HEALTHY,
                    visualBadge = "Başak Emgisi / Akbaşak",
                    symptoms = "Kışlaktan inen erginler kardeşlerde kuruma (kurtboğazı), nimfler ise süt/sarı olumda daneyi emerek büzüşük ve ekmeklik değeri kayıp dane oluşturur.",
                    favorableWeather = "İlkbahar sıcaklığının 18–20°C üzerine çıkmasıyla dağlık kışlaklardan ovaya göç başlar.",
                    sprayAction = "İl/İlçe Tarım teknik ekiplerinin 2.–3. dönem nimf sürvey eşiği (m²’de yoğunluk) duyurusuna göre ruhsatlı insektisit ile müdahale edilir."
                )
            ),
            tempRiskMin = 14.0,
            tempRiskMax = 25.0,
            humidityThreshold = 80
        ),
        CropInfo(
            id = "Arpa",
            emoji = "🌿",
            name = "Arpa",
            colorHex = 0xFF558B2F,
            subtitle = "Yemlik ve maltlık arpada yaprak hastalıkları ve yatma önleme rehberi",
            climateAndSoil = "Buğdaya göre kuraklığa ve hafif tuzlu/kireçli topraklara daha dayanıklıdır; olgunlaşması 15–20 gün daha erkendir. Aşırı verimli ve su tutan tarlalarda sap yatması görülebilir.",
            frostAndTempThresholds = "Kışlık çeşitler -12°C / -15°C’ye dayanır. Ancak su biriken taban arazilerde kış donu ve kök çürüklüğü riski yüksektir.",
            waterNeedsAndIrrigation = "Yıllık 350–500 mm su yeterlidir. Sapa kalkma ve başaklanma öncesi bir kez destek sulaması kurak yıllarda verimi ciddi artırır; geç sulama yatmaya yol açar.",
            fertilizationProgram = "Buğdaydan %20–25 daha az azot verilmelidir; özellikle maltlık arpada aşırı azot danede protein oranını yükselterek malt kalitesini bozar ve yatmayı tetikler.",
            harvestAndStorage = "Başaklar aşağı eğilip kılçıklar kırılganlaştığında ve dane nemi %12–13 seviyesine indiğinde biçilir.",
            criticalStage = "Sapa kalkma ve bayrak yaprak dönemi",
            diseases = "Arpa Yaprak Lekesi (Rhynchosporium), Ağ Benek, Rastık, Külleme",
            symptoms = "Yapraklarda ortası açık gri, kenarı koyu kahverengi oval lekeler ve yaprak kurumaları.",
            management = "• Anız artıklarını toprağa gömün veya temizleyin.\n• Serin ve yağışlı bahar günlerinde yaprak lekelerini alt yapraklardan itibaren kontrol edin.",
            activeIngredientGroups = "Sertifikalı tohum koruyucusu (açık/kapalı rastık), yaprak lekesi ve ağ benek için ruhsatlı sistemik fungisitler, kardeşlenmede yabancı ot herbisiti.",
            monitoringThreshold = "Serin (12–20°C) ve yağışlı Nisan-Mayıs dönemlerinde alt yapraklardan üst yapraklara leke ilerleyişi %10’u aşmadan kontrol edilir.",
            irrigationAndNutrition = "Buğdaya göre yatmaya daha duyarlıdır; aşırı azotlu üst gübreleme yatma ve külleme riskini artırır. Potasyum sap sağlamlığını destekler.",
            phenologySteps = listOf(
                CropPhenologyStep("Ekim & Tohum Koruması", "Ekim – Kasım", "Rastık için ilaçlı sertifikalı tohum ve taban gübresi"),
                CropPhenologyStep("Kardeşlenme & Ot Mücadelesi", "Şubat – Mart", "Dengeli üst azot ve seçici yabancı ot herbisiti"),
                CropPhenologyStep("Sapa Kalkma & Bayrak Yaprak", "Nisan", "Rhynchosporium yaprak lekesi ve ağ benek kontrolü"),
                CropPhenologyStep("Erken Hasat", "Mayıs Sonu – Haziran Başı", "Dane %13 nemde yatmadan hızlı hasat")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Arpa Yaprak Lekesi (Rhynchosporium commune)",
                    agent = "Fungal Etmen — Serin ve yağmurlu baharda hızla ilerler",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Gri Merkezli Kahve Leke",
                    symptoms = "Yapraklarda su emmiş gibi başlayıp ortası açık gri-beyaz, kenarları koyu kahverengi hale dönüşen oval-baklava biçimli lekeler.",
                    favorableWeather = "10–18°C serin hava ve sık yağışlı günlerde anız artıklı tarlalarda salgın yapar.",
                    sprayAction = "Sapa kalkma – bayrak yaprak çıkışında hastalık üst yapraklara tırmanırken Triazol/Strobilurin/SDHI grubu ruhsatlı arpa fungisiti uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Arpa Ağ Benek & Külleme",
                    agent = "Pyrenophora teres & Blumeria graminis",
                    visualSymptomKind = SymptomPattern.POWDERY_WHITE,
                    visualBadge = "Ağsı Çizgi / Beyaz Küf",
                    symptoms = "Ağ benekte yaprakta enine-boyuna ağ gibi koyu çizgiler; küllemede alt yapraklarda beyaz-gri pamuksu yastıkçıklar.",
                    favorableWeather = "Sık ekim, gölgeleme, aşırı azot ve %75+ nem.",
                    sprayAction = "Dayanıklı çeşit + anız temizliği temeldir; eşik aşılırsa bayrak yaprak döneminde ruhsatlı sistemik fungisit kullanın."
                )
            ),
            tempRiskMin = 12.0,
            tempRiskMax = 22.0,
            humidityThreshold = 80
        ),
        CropInfo(
            id = "Nohut",
            emoji = "🫘",
            name = "Nohut",
            colorHex = 0xFF8D6E63,
            subtitle = "Antraknoz (Yanıklık) hassas dönem takibi ve baklagil yetiştiriciliği",
            climateAndSoil = "Kurağa dayanıklı bir yemeklik baklagildir; derin, havalanabilir tınlı topraklarda pH 6.0–8.0 arasında iyi yetişir. Su biriken taban arazilerde Fusarium kök çürüklüğü görülür.",
            frostAndTempThresholds = "Kışlık ekim çeşitleri -8°C’ye dayanabilir; ancak ilkbahar çiçeklenme döneminde yüksek sıcaklık (>32°C) ve kuru rüzgâr çiçek döker. En büyük düşmanı serin-yağışlı bahar günleridir.",
            waterNeedsAndIrrigation = "Çoğunlukla kuru tarımda (350–450 mm yağışla) yetiştirilir. Çiçeklenme ve bakla dolumunda bir adet hafif destek sulaması verimi artırır; ancak aşırı sulama antraknozu patlatır.",
            fertilizationProgram = "Kök yumrucuklarıyla (Rhizobium) havadaki azotu bağlar; ekimde dekara yalnızca 2–3 kg saf azot (başlangıç) ve 5–6 kg saf fosfor (P₂O₅) yeterlidir. Fazla azot hastalığı artırır.",
            harvestAndStorage = "Yaprak ve kapsüller sararıp kuruduğunda, daneler sertleşip bakla içinde sallandığında (%12–13 nem) hasat edilir; depoda baklagil tohum böceğine (bruchus) karşı serin ve kapalı muhafaza şarttır.",
            criticalStage = "Çıkış sonrası ve çiçeklenme / bakla bağlama dönemi",
            diseases = "Nohut Antraknozu (Ascochyta rabiei), Fusarium Solgunluğu",
            symptoms = "Gövde, yaprak ve kapsüllerde iç içe halkalı koyu kahverengi çökük lekeler, gövde kırılması ve ani solma.",
            management = "• Yağışlı ve serin (15-22°C) bahar havalarında antraknoz çok hızlı yayılır.\n• Hastalıksız sertifikalı tohum ekin, sık ekimden kaçının.",
            activeIngredientGroups = "Tohum ilaçlaması + çıkış sonrası yağışlı dönem öncesi koruyucu (kontak/sistemik) antraknoz fungisitleri, çıkış öncesi/sonrası dar ve geniş yapraklı herbisitler.",
            monitoringThreshold = "15–22°C aralığında art arda 2 gün yağış veya %78+ nem görüldüğünde ilk lekeler görülür görülmez (gövde kırılmadan) koruyucu uygulama yapılmalıdır.",
            irrigationAndNutrition = "Rhizobium bakterileriyle azot bağlar; aşırı azot nodülasyonu bastırır ve yeşil aksamı büyütüp antraknozu artırır. Çiçeklenme ve bakla dolumunda ağır su birikiminden kaçının.",
            phenologySteps = listOf(
                CropPhenologyStep("Ekim & Çıkış Öncesi", "Şubat – Mart", "Antraknoza dayanıklı sertifikalı tohum ilaçlaması ve çıkış öncesi herbisit"),
                CropPhenologyStep("Vejetatif Büyüme", "Nisan", "Yağışlı günlerde antraknoz gözetimi ve gerekirse koruyucu fungisit"),
                CropPhenologyStep("Çiçeklenme & Bakla Tutumu", "Mayıs", "Serin yağış sonrası ikinci antraknoz koruması, yeşilkurt takibi"),
                CropPhenologyStep("Olgunlaşma & Hasat", "Haziran – Temmuz", "Kapsüller kuruduğunda dökülmeden biçim ve bruchus önlemi")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Nohut Antraknozu / Yanıklık (Ascochyta rabiei)",
                    agent = "Fungal Etmen — Tarlayı 1 hafta içinde yakabilir",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Halkalı Gövde/Kapsül Lekesi",
                    symptoms = "Gövdede yanık gibi uzunlamasına koyu yaralar (gövde kırılır ve üstü kurur), yaprak ve kapsüllerde iç içe siyah noktalı dairesel lekeler.",
                    favorableWeather = "15–22°C serin hava ve 2+ gün süren bahar yağışları (%78+ nem).",
                    sprayAction = "Tohum ilaçlaması zorunludur. Baharda yağış uyarısı alındığında veya ilk lekeler alt yaprakta görüldüğünde 10–14 gün arayla farklı FRAC gruplarıyla antraknoz fungisiti uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Fusarium Solgunluğu & Kök Çürüklüğü",
                    agent = "Fusarium oxysporum f. sp. ciceris (Toprak kökenli)",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Ani Sararma & Solma",
                    symptoms = "Bitkide tek taraflı veya tüm gövdede hızla sararma, yaprakların sarkması; kök boğazı boylamasına kesildiğinde iletim demetlerinde kahverengileşme.",
                    favorableWeather = "Sıcaklık artışı ve üst üste nohut ekilen ağır/su tutan topraklar.",
                    sprayAction = "Kimyasal tedavisi yoktur! En az 3–4 yıllık tahıl münavebesi, dayanıklı çeşit ve temiz sertifikalı tohum tek çözümdür."
                )
            ),
            tempRiskMin = 15.0,
            tempRiskMax = 24.0,
            humidityThreshold = 78
        ),
        CropInfo(
            id = "Nar",
            emoji = "🍎",
            name = "Nar",
            colorHex = 0xFFAD1457,
            subtitle = "Meyve çatlaması, güneş yanığı ve Alternaria iç çürüklüğü önleme rehberi",
            climateAndSoil = "Sıcak ve yarı kurak iklimlerde (yazları sıcak, kışları ılıman) yüksek kalite verir. Derin, drenajı iyi tınlı-alüvyal toprakları sever; hafif tuzluluğa dayanıklıdır.",
            frostAndTempThresholds = "-10°C / -12°C altında gövde don zararı oluşur (genç fidanlar -6°C’de zarar görür). 35°C üzeri yoğun güneşte kabukta kahverengi güneş yanıklığı lekesi çıkar.",
            waterNeedsAndIrrigation = "En kritik kural DÜZENLİ SULAMADIR. Kurak dönem sonrası aniden verilen bol su meyve içinin şişip kabuğun çatlamasına yol açar; çatlaklar da çürükçül böcek ve küfleri çeker.",
            fertilizationProgram = "Baharda azot-fosfor-potasyum dengeli verilir; meyve irileşirken potasyum, kalsiyum ve bor desteği kabuğu esnek tutarak çatlamayı azaltır.",
            harvestAndStorage = "Kabuk tam rengini alıp parmakla vurulduğunda metalik ses verdiğinde ve asit/şeker oranı olgunlaştığında makasla kesilerek toplanır; 5–7°C ve %90 nemde 3–5 ay saklanabilir.",
            criticalStage = "Çiçeklenme, meyve büyümesi ve hasat öncesi dönem",
            diseases = "Alternaria Meyve Çürüklüğü, Güneş Yanıklığı, Yaprak Biti, Harnup Güvesi",
            symptoms = "Dış kabuk sağlam görünürken meyve içinde siyah/kahverengi çürüme, kabuk çatlaması.",
            management = "• Düzensiz sulamadan kaçının; ani su değişimleri meyve çatlamasına ve çürüklüğe yol açar.\n• Çatlamış ve yere dökülmüş meyveleri bahçeden toplayıp imha edin.",
            activeIngredientGroups = "Çiçeklenme başlangıcında Alternaria iç çürüklüğüne karşı ruhsatlı fungisitler, yaprak biti ve harnup güvesi için seçici insektisitler / kaolin güneş koruması.",
            monitoringThreshold = "Harnup güvesi için çatlamış meyvelerin temizlenmesi ve yumurta/larva çıkışı takibi; yaz sıcaklarında (>34°C) kaolin kil uygulamasıyla güneş yanığı önleme.",
            irrigationAndNutrition = "Meyve büyüme döneminden hasada kadar düzenli damlama sulama şarttır. Kalsiyum ve bor desteği kabuk esnekliğini artırarak çatlamayı azaltır.",
            phenologySteps = listOf(
                CropPhenologyStep("Sürgün & Dip Sürgünü Temizliği", "Mart – Nisan", "Dip sürgünlerinin kesilmesi ve yaprak biti gözetimi"),
                CropPhenologyStep("Çiçeklenme", "Mayıs – Haziran", "Alternaria iç çürüklüğü için çiçek dönemi koruyucu önlem"),
                CropPhenologyStep("Meyve Büyümesi & Yaz", "Temmuz – Ağustos", "Düzenli damlama sulama, kalsiyum/bor takviyesi ve kaolin güneş koruması"),
                CropPhenologyStep("Olgunlaşma & Hasat", "Eylül – Ekim", "Çatlak meyve imhası, harnup güvesi kontrolü ve makasla hasat")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Alternaria İç Çürüklüğü (Kara Kalp)",
                    agent = "Alternaria alternata — Çiçek taç kısmından meyveye girer",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "İç Tanelerde Siyah Çürüme",
                    symptoms = "Dış kabuk sağlıklı veya hafif soluk görünürken meyve kesildiğinde danelerin siyah-kahverengi küflü ve ekşi kokulu olması.",
                    favorableWeather = "Çiçeklenme döneminde yağış/yüksek nem sporların açık çiçekten meyve içine girmesini kolaylaştırır.",
                    sprayAction = "Çiçeklenme başlangıcı ve taç yaprak dökümünde (arı uçuşu dışında) ruhsatlı koruyucu fungisit uygulaması ve damlama sulamayla nem dengesi sağlanır."
                ),
                CropDiseaseDetail(
                    name = "Harnup Güvesi & Yaprak Biti",
                    agent = "Ectomyelois ceratoniae & Aphis punicae",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Yapışkan Salgı / Taç Kurdu",
                    symptoms = "Yaprak bitinde sürgün uçlarında kıvrılma ve tatlımsı ballık (karaballık); harnup güvesinde meyve tacı altında kurt pisliği ve çürüme.",
                    favorableWeather = "22–32°C sıcak yaz aylarında yaprak biti ve güve nesilleri hızlanır.",
                    sprayAction = "Çatlak meyveleri bahçeden uzaklaştırın; yaprak bitinde faydalı uğur böceklerini koruyan seçici afisitler, güvede feromon takibi ile müdahale uygulayın."
                )
            ),
            tempRiskMin = 20.0,
            tempRiskMax = 32.0,
            humidityThreshold = 72
        ),
        CropInfo(
            id = "Elma",
            emoji = "🍏",
            name = "Elma",
            colorHex = 0xFFC62828,
            subtitle = "Elma karalekesi, iç kurdu ve acı benek önleme bahçe rehberi",
            climateAndSoil = "Yüksek kış soğuklama ihtiyacı (700–1.400 saat <7°C) olan serin/yarı serin iklim meyvesidir. Derin, organik maddesi zengin, pH 6.0–7.0 toprakları sever.",
            frostAndTempThresholds = "Kışın -25°C’ye dayanır; pembe rozet aşamasında -3°C, tam çiçekte -2°C, küçük meyvede -1°C zarar verir. Güneşli yaz günlerinde >33°C kabukta güneş yanığı yapar.",
            waterNeedsAndIrrigation = "Yıllık 700–900 mm su ister. Meyve büyümesi boyunca düzenli nem sağlanmazsa meyvede Kalsiyum taşınımı bozulur ve 'Acı Benek' (bitter pit) oluşur.",
            fertilizationProgram = "Erken ilkbaharda azot ve fosfor; meyve tutumu sonrası potasyum; ceviz büyüklüğünden hasada kadar 3–4 kez yapraktan Kalsiyum Klorür / Kalsiyum Nitrat uygulaması depolama ömrünü uzatır.",
            harvestAndStorage = "Nişasta-iyot testi, meyve sapının kolay ayrılması ve çekirdeklerin tamamen kahverengileşmesiyle hasat edilir; 0–1°C ve %90–95 nemde aylarca depolanır.",
            criticalStage = "Fare kulağı, pembe rozet ve meyve tutumu dönemi",
            diseases = "Elma Karalekesi, Külleme, Ateş Yanıklığı, Elma İç Kurdu",
            symptoms = "Yaprak ve meyvede zeytin yeşili-siyah kadifemsi lekeler; sürgün uçlarında çoban değneği kıvrılması.",
            management = "• Yere dökülen yaprakları sonbaharda toplayın veya parçalayın.\n• İlkbahar yağışlarından önce koruyucu, yağış sonrası uygun zamanlamayla mücadele planlayın.",
            activeIngredientGroups = "Fare kulağı-pembe rozet döneminde bakırlı/koruyucu karaleke fungisitleri, külleme için kükürt/sistemik gruplar, iç kurdu için feromon + larvisit/granülozis virüsü.",
            monitoringThreshold = "Mills tablosu (sıcaklık × yaprak ıslaklık süresi) karaleke enfeksiyon riskini belirler; iç kurdu tuzaklarında haftalık kritik yakalanma sonrası mücadele başlatılır.",
            irrigationAndNutrition = "Kalsiyum eksikliği meyvede acı benek (bitter pit) bozukluğuna yol açar; meyve tutumu sonrası yapraktan kalsiyum takviyesi ve düzenli sulama önemlidir.",
            phenologySteps = listOf(
                CropPhenologyStep("Kış Sonu & Fare Kulağı", "Mart", "Dökülen yaprakların imhası ve bakırlı karaleke başlangıcı"),
                CropPhenologyStep("Pembe Rozet & Çiçeklenme", "Nisan", "Karaleke & külleme koruması; çiçekte ateş yanıklığına karşı arı dostu önlem"),
                CropPhenologyStep("Ceviz Büyüklüğü & Yaz", "Mayıs – Temmuz", "Elma iç kurdu 1. ve 2. döl mücadelesi + yapraktan Kalsiyum"),
                CropPhenologyStep("Hasat & Depolama", "Ağustos – Ekim", "Son ilaçlama bekleme süresi (PHI) uyumu ve soğuk hava deposu")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Elma Karalekesi (Venturia inaequalis)",
                    agent = "Fungal Etmen — En önemli elma hastalığıdır",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Kadifemsi Siyah Leke",
                    symptoms = "Yaprak ve meyve üzerinde zeytin yeşili ile siyah arası kadifemsi dairesel lekeler; meyvede şekil bozukluğu ve çatlama.",
                    favorableWeather = "12–22°C sıcaklık ve yağış sonrası 9+ saat süren yaprak ıslaklığı (Mills tablosu).",
                    sprayAction = "Fare kulağı ve pembe rozet dönemlerinde kontak koruyucular (FRAC M4/M9); yağış sonrası kritik enfeksiyon olursa 48–72 saat içinde tedavi edici sistemik (FRAC 3/9) fungisit uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Elma Küllemesi & Ateş Yanıklığı",
                    agent = "Podosphaera leucotricha / Erwinia amylovora",
                    visualSymptomKind = SymptomPattern.POWDERY_WHITE,
                    visualBadge = "Beyaz Sürgün / Çoban Değneği",
                    symptoms = "Küllemede beyaz unsu yaprak ve tomurcuklar; Ateş yanıklığında genç sürgün uçlarının kararıp 'çoban değneği' gibi kıvrılması.",
                    favorableWeather = "Külleme 18–25°C kuru-ılıkta; Ateş yanıklığı çiçeklenmede 18–27°C + yağışta sıçrar.",
                    sprayAction = "Küllemeli beyaz sürgünleri kışın budayın ve çiçek öncesi/sonrası kükürt/sistemik uygulayın; ateş yanıklığı olan dalları 30 cm alttan kesip yakın."
                ),
                CropDiseaseDetail(
                    name = "Elma İç Kurdu (Cydia pomonella)",
                    agent = "Lepidoptera Larvası — Doğrudan meyveyi kurtlandırır",
                    visualSymptomKind = SymptomPattern.HEALTHY,
                    visualBadge = "Meyvede Kurt Deliği",
                    symptoms = "Meyve yüzeyinde kahverengi talaş/pislik birikintili giriş deliği ve çekirdek evine kadar uzanan galeri.",
                    favorableWeather = "Akşam sıcaklığının ≥15°C olduğu Mayıs–Temmuz günlerinde yumurtlama hızlanır.",
                    sprayAction = "Feromon tuzaklarıyla kelebek çıkışı ve derece-gün hesabına göre yumurta açılımında ruhsatlı larvisit veya granülozis virüsü (CpGV) uygulayın."
                )
            ),
            tempRiskMin = 14.0,
            tempRiskMax = 24.0,
            humidityThreshold = 80
        ),
        CropInfo(
            id = "Sebze",
            emoji = "🍅",
            name = "Sebze (Domates/Biber)",
            colorHex = 0xFF00838F,
            subtitle = "Açık tarla ve sera domates/biberde mildiyö, yanıklık ve Tuta yönetimi",
            climateAndSoil = "Ilık-sıcak mevsim sebzeleridir; optimum gelişme 20–27°C arasıdır. Organik maddesi yüksek, süzek tınlı pH 6.0–6.8 topraklarda mükemmel verim verir.",
            frostAndTempThresholds = "0°C’de donarak ölürler; 10°C altında büyüme durur ve çiçek tozu canlılığı düşer. 35°C üzeri sıcakta çiçek dökümü (döllenme bozukluğu) başlar.",
            waterNeedsAndIrrigation = "Damlama sulama ile sık ve dengeli sulama şarttır. Düzensiz sulama ve Kalsiyum eksikliği meyve ucunda siyah çökük alan ('Çiçek Burnu Çürüklüğü') oluşturur. Akşam üstten sulama yaprak hastalıklarını tetikler.",
            fertilizationProgram = "Dikimden çiçeklenmeye kadar fosfor-azot ağırlıklı; meyve tutumuyla beraber Potasyum + Kalsiyum + Magnezyum dengeli fertirigasyon programı uygulanır.",
            harvestAndStorage = "Domateste alacalı-kırmızı renklenmede, biberde çeşit iriliği ve parlak kabukta hasat edilir; hasat öncesi bekleme sürelerine (PHI) kısa hasat aralıkları nedeniyle azami dikkat edilmelidir.",
            criticalStage = "Fide dikimi, çiçeklenme ve meyve olgunlaşma dönemi",
            diseases = "Mildiyö, Erken Yaprak Yanıklığı, Külleme, Kurşuni Küf, Beyazsinek / Tuta",
            symptoms = "Alt yapraklarda hedef tahtası görünümlü koyu lekeler, gövdede kahverengi yaralar ve meyve çürüklüğü.",
            management = "• Damlama sulama kullanın; yaprakları akşam saatlerinde ıslatmaktan kaçının.\n• Sera ve açık alanda sıra arası hava akışını sağlayın.",
            activeIngredientGroups = "Kontak ve translaminar mildiyö/Alternaria fungisitleri, kurşuni küfe karşı farklı FRAC grupları, Tuta ve beyazsinek için sarı/feromon tuzaklar + Bt/seçici preparatlar.",
            monitoringThreshold = "Serada %80 üzeri bağıl nemde kurşuni küf ve mildiyö hızla başlar; alt yaprak budaması + sabah havalandırması ile yaprak ıslaklığı 4 saatin altında tutulmalıdır.",
            irrigationAndNutrition = "Düzensiz sulama ve kalsiyum taşınım bozukluğu domates ve biberde çiçek burnu çürüklüğüne neden olur. Damlama ile dengeli K-Ca programı uygulanmalıdır.",
            phenologySteps = listOf(
                CropPhenologyStep("Fide Dikimi & Köklenme", "Nisan – Mayıs", "Temiz fide, damlama sulama kurulumu ve sarı/mavi yapışkan tuzaklar"),
                CropPhenologyStep("Çiçeklenme & Meyve Tutumu", "Haziran", "Kalsiyum desteği, alt yaprak temizliği ve Tuta/beyazsinek takibi"),
                CropPhenologyStep("Meyve Büyümesi & Hasat", "Temmuz – Eylül", "Kısa PHI (1–3 gün) biyolojik/seçici ürünler ve sabah sulaması"),
                CropPhenologyStep("Sezon Sonu Temizlik", "Ekim", "Hastalıklı bitki artıklarının tarla/seradan uzaklaştırılması")
            ),
            diseaseDetails = listOf(
                CropDiseaseDetail(
                    name = "Erken Yaprak Yanıklığı & Mildiyö",
                    agent = "Alternaria solani & Phytophthora infestans",
                    visualSymptomKind = SymptomPattern.NECROTIC_SPOT,
                    visualBadge = "Hedef Tahtası / Islak Yanıklık",
                    symptoms = "Alt yapraklarda iç içe halkalı 'hedef tahtası' koyu lekeler (Alternaria) veya hızlı ilerleyen ıslak kahverengi yanıklık ve meyve çürümesi (Mildiyö).",
                    favorableWeather = "16–25°C sıcaklık ve %78+ nem / gece çiği / yağış.",
                    sprayAction = "Yağış ve yoğun çiğ öncesinde Kontak (FRAC M3/M5/Bakır) koruyucu; belirti varsa translaminar/sistemik mildiyö-Alternaria fungisiti ile kısa PHI kurallarına uyarak ilaçlama yapın."
                ),
                CropDiseaseDetail(
                    name = "Sebze Küllemesi & Kurşuni Küf",
                    agent = "Leveillula taurica & Botrytis cinerea",
                    visualSymptomKind = SymptomPattern.POWDERY_WHITE,
                    visualBadge = "Yaprak Altı Unsu Küf",
                    symptoms = "Biber/domates yapraklarında üstte sarı lekeler, altta beyazımsı toz (külleme) veya gövde/meyvede gri kadifemsi küf çürüklüğü.",
                    favorableWeather = "Sera içi yüksek nem (%80+) ve yetersiz havalandırma.",
                    sprayAction = "Sabah sera havalandırmasını açın, alt yaprakları budayın; etki grubu rotasyonlu ruhsatlı anti-külleme / anti-botrytis preparatı uygulayın."
                ),
                CropDiseaseDetail(
                    name = "Domates Güvesi (Tuta) & Beyazsinek",
                    agent = "Tuta absoluta & Bemisia tabaci",
                    visualSymptomKind = SymptomPattern.YELLOW_RUST,
                    visualBadge = "Yaprak Galerisi / Virüs Riski",
                    symptoms = "Yaprak içinde şeffaf galeriler ve meyvede kurt deliği (Tuta); yaprak altında beyaz küçük sinekler, ballık ve virüs sarılığı.",
                    favorableWeather = "22–32°C sıcak ve kurak dönemlerde popülasyon patlar.",
                    sprayAction = "Feromon tuzak + su tuzağı ile erginleri yakalayın; larva çıkışında akşamüstü Bacillus thuringiensis (Bt) veya kısa hasat aralıklı seçici insektisit kullanın."
                )
            ),
            tempRiskMin = 16.0,
            tempRiskMax = 27.0,
            humidityThreshold = 78
        )
    )

    val presetCities: List<CityPreset> = listOf(
        CityPreset("Adıyaman Merkez", 37.7648, 38.2786),
        CityPreset("Kahta (Adıyaman)", 37.7819, 38.6228),
        CityPreset("Besni (Adıyaman)", 37.6928, 37.8611),
        CityPreset("Gölbaşı (Adıyaman)", 37.7836, 37.6375),
        CityPreset("Şanlıurfa", 37.1591, 38.7969),
        CityPreset("Gaziantep", 37.0662, 37.3833),
        CityPreset("Malatya", 38.3552, 38.3095),
        CityPreset("Kahramanmaraş", 37.5858, 36.9371),
        CityPreset("Diyarbakır", 37.9144, 40.2306),
        CityPreset("Adana", 37.0000, 35.3213),
        CityPreset("Konya", 37.8746, 32.4932),
        CityPreset("İzmir", 38.4237, 27.1428),
        CityPreset("Bursa", 40.1885, 29.0610)
    )

    val embeddedGuideButtons: List<GuideTopicButton> = listOf(
        GuideTopicButton(
            id = GuideTopicId.METEOROLOGY,
            title = "☁️ Tarımsal Meteoroloji & İlaçlama Kuralları",
            subtitle = "Rüzgâr, inversiyon, don korunma ve ET₀ sulama hesaplama"
        ),
        GuideTopicButton(
            id = GuideTopicId.BKU,
            title = "🧪 Etken Madde & Güvenli İlaçlama Rehberi",
            subtitle = "Gömülü etken madde grupları, direnç rotasyonu ve arı/hasat güvenliği"
        ),
        GuideTopicButton(
            id = GuideTopicId.IPM,
            title = "🌿 Entegre Mücadele (IPM) & Bitki Besleme",
            subtitle = "Zararlı eşikleri, budama hijyeni ve toprak-gübre dengesi"
        )
    )

    val meteorologyGuideSections: List<GuideSection> = listOf(
        GuideSection(
            heading = "🎯 İdeal Püskürtme (İlaçlama) Hava Penceresi",
            items = listOf(
                "Sıcaklık: 10°C – 25°C arası en verimli aralıktır. 30°C üzerinde buharlaşma artar ve yaprak yanıklığı (fitotoksisite) riski doğar.",
                "Rüzgâr Hızı: 3 – 12 km/s hafif esinti idealdir. 15 km/s üzerinde sürüklenme (drift) başlar; 0–2 km/s tam durgunlukta ise terselme (inversiyon) riski kontrol edilmelidir.",
                "Bağıl Nem: %45 – %80 arası damlacıkların yaprak yüzeyine buharlaşmadan tutunmasını sağlar. %35 altında ince damlalar havada kurur.",
                "Yağışsız Süre: Uygulama sonrası ilacın bitki dokusuna tutunması için en az 4 – 6 saat yağışsız dönem gerekir."
            )
        ),
        GuideSection(
            heading = "🌫️ Termal İnversiyon (Isı Terselmesi) Uyarısı",
            items = listOf(
                "Güneş batımından hemen sonra veya rüzgârsız sabaha karşı yer yüzeyi soğurken sıcak hava tabakası üstte kalır.",
                "İnversiyon sırasında püskürtülen ince damlalar yere çökmez, havada asılı kalıp komşu parsellere sürüklenebilir.",
                "Yatay duman veya sis tabakası görülüyorsa püskürtmeyi erteleyin; güneş doğup hafif karışım rüzgârı (3–5 km/s) başladığında çalışın."
            )
        ),
        GuideSection(
            heading = "❄️ Zirai Don Türleri ve Bahçe/Tarla Koruma",
            items = listOf(
                "Radyasyon Donu: Açık, bulutsuz ve rüzgârsız gecelerde ısı kaybıyla oluşur; çukur arazilerde soğuk hava birikir.",
                "Adveksiyon Donu: Soğuk hava kütlesinin rüzgârla taşınmasıyla oluşur ve çok daha şiddetlidir.",
                "Alınabilecek Tedbirler: Çiçeklenme öncesi toprak yüzeyini sıkı ve nemli tutmak, yabancı otları temizlemek, don öncesi hafif sulama yapmak ve meyve bahçelerinde üstten yağmurlama / ısıtma planlamak."
            )
        ),
        GuideSection(
            heading = "💧 ET₀ (Bitki Su Tüketimi) ve Sulama Bütçesi",
            items = listOf(
                "ET₀ (FAO Penman-Monteith Referans Evapotranspirasyonu), bir günde topraktan buharlaşma + bitkiden terleme ile kaybedilen referans su miktarını (mm) gösterir.",
                "Pratik Kural: 1 mm su = dekara (1.000 m²) 1 ton (1.000 litre) su demektir.",
                "7 Günlük Net Su Açığı = (Toplam ET₀ × Bitki Katsayısı Kc) − Etkili Yağış. Açık 12–15 mm üzerine çıktığında sulama planlanır."
            )
        )
    )

    val ipmGuideSections: List<GuideSection> = listOf(
        GuideSection(
            heading = "🛡️ Entegre Mücadele (IPM) 4 Temel Basamağı",
            items = listOf(
                "1. Kültürel Önlemler: Sertifikalı ve temiz tohum/fidan kullanımı, ekim nöbeti (münavebe), dengeli azot gübrelemesi, taç içi havalandırma budaması ve hasta bitki artıklarının imhası.",
                "2. Biyoteknik & Mekanik Mücadele: Feromon tuzakları (Salkım güvesi, Elma iç kurdu, Akdeniz meyve sineği), sarı/mavi yapışkan tuzaklar ve gövde tuzak bantları.",
                "3. Biyolojik Mücadele: Doğal düşmanların (uğur böceği, avcı akarlar, parazitoit arıcıklar) korunması; gereksiz geniş spektrumlu insektisit atılmaması.",
                "4. Kimyasal Mücadele (Son Çare): Ekonomik Zarar Eşiği aşıldığında, hedef zararlıya/hastalığa özgü seçici preparatın doğru fenolojik dönemde ve etiketteki dozda uygulanması."
            )
        ),
        GuideSection(
            heading = "🔍 Sahada Kritik Ekonomik Zarar Eşikleri",
            items = listOf(
                "Buğday/Arpa – Süne: Metrekarede kışlamış ergin sayısı kritik eşiğe ulaştığında veya 2.–3. dönem nimf yoğunluğunda yerel sürvey duyurularına göre hareket edilir.",
                "Zeytin – Zeytin Sineği: Feromon + besin tuzaklarında haftalık yakalanma artışı ve meyvede vuruk oranı %1–2 üzerine çıktığında yem dal ilaçlaması veya kaplama değerlendirilir.",
                "Bağ – Mildiyö (10-10-24 Kuralı): Sürgün boyu ≥10 cm, son 24–48 saatte ≥10 mm yağış ve sıcaklık ≥10°C gerçekleştiğinde primer enfeksiyon takibi başlatılır.",
                "Nohut – Antraknoz: Serin (15–22°C) ve ardışık yağışlı günlerde alt yapraklarda ilk halkalı lekeler görüldüğünde koruyucu uygulama hayati önem taşır."
            )
        ),
        GuideSection(
            heading = "🌱 Toprak Verimliliği & Dengeli Bitki Besleme",
            items = listOf(
                "Aşırı Azot (N) Riski: Gereğinden fazla azotlu gübre bitki dokularını sulu ve ince hücre duvarlı yapar; bu durum külleme, pas, mildiyö ve yaprak bitlerini doğrudan artırır.",
                "Potasyum (K) & Kalsiyum (Ca): Hücre duvarını güçlendirerek kuraklık, don ve mantari hastalıklara karşı doğal direnci yükseltir; nar ve domateste meyve çatlamasını azaltır.",
                "Toprak pH Dengesi: Yüksek pH’lı (kireçli) topraklarda demir, çinko ve fosfor bağlanır; damlama ile asidik/humik destek ve yaprak uygulamaları gerekebilir."
            )
        )
    )

    val bkuEntries: List<BkuEntry> = listOf(
        BkuEntry(
            id = "bakir",
            category = BkuCategory.FUNGICIDE,
            activeGroup = "Bakır Tuzları (Bordo Bulamacı, Bakır Oksiklorür, Bakır Hidroksit)",
            targets = "Zeytin halkalı leke, zeytin dal kanseri, bağ mildiyösü, badem yaprak delen (çil), ateş yanıklığı (koruyucu bakterisit/fungisit)",
            crops = "Zeytin, Bağ, Badem, Elma, Nar, Sebze",
            weatherRule = "Yağış öncesi veya budama/dolu yaraları sonrası koruyucu olarak uygulanır. Donlu ve aşırı sıcak (>30°C) havalarda atılmamalıdır.",
            caution = "Çiçeklenme döneminde çiçek yakma riski nedeniyle genellikle uygulanmaz; alkali olduğu için asidik ilaçlarla karıştırılmaz."
        ),
        BkuEntry(
            id = "kukurt",
            category = BkuCategory.FUNGICIDE,
            activeGroup = "İnorganik Kükürt (WP / WG Mikronize Kükürt)",
            targets = "Külleme (Bağ, Elma, Sebze, Tahıl) ve kırmızı örümcek baskılama",
            crops = "Bağ, Elma, Sebze, Buğday, Arpa",
            weatherRule = "En iyi etki 18°C – 27°C arasındadır. 30°C üzerindeki sıcaklıklarda yaprak ve tanelerde yanıklık (fitotoksisite) yapar.",
            caution = "Yazlık yağlar ve kalsiyum polisülfür ile karıştırılmamalı; yağ uygulamasından en az 15–20 gün arayla kullanılmalıdır."
        ),
        BkuEntry(
            id = "triazol-strobilurin",
            category = BkuCategory.FUNGICIDE,
            activeGroup = "Sistemik / Yarı Sistemik Fungisitler (FRAC Grup 3 Triazoller & Grup 11 Strobilurinler)",
            targets = "Buğday/arpa pas hastalıkları, septorya, arpa yaprak lekesi, elma karalekesi, bağ küllemesi, nohut antraknozu",
            crops = "Buğday, Arpa, Nohut, Bağ, Elma, Badem",
            weatherRule = "Bitki dokusuna hızla nüfuz etmesi için uygulama sırasında bitki aktif büyümede olmalı ve en az 2–4 saat yağış almamalıdır.",
            caution = "Aynı FRAC grubundan üst üste 2’den fazla uygulama direnç geliştirir; mutlaka farklı etki mekanizmalı gruplarla rotasyon yapın."
        ),
        BkuEntry(
            id = "contact-fungicide",
            category = BkuCategory.FUNGICIDE,
            activeGroup = "Çok Bölgeli Kontak Fungisitler (FRAC Grup M3 / M4 / M5)",
            targets = "Mildiyö, erken yaprak yanıklığı, elma karalekesi, nohut antraknozu (koruyucu)",
            crops = "Bağ, Sebze, Nohut, Elma",
            weatherRule = "Hastalık belirtisi çıkmadan, yağış ve yüksek nem (%75+) öncesinde yaprak yüzeyini kaplayacak şekilde uygulanır.",
            caution = "Yeni sürgünler çıktıkça ve yoğun yağış yıkaması sonrası koruma süresi azalır; etiket son ilaçlama-hasat aralığına (PHI) uyun."
        ),
        BkuEntry(
            id = "bt-bio",
            category = BkuCategory.BIOLOGICAL,
            activeGroup = "Bacillus thuringiensis (Bt) & Spinosad / Azadirachtin",
            targets = "Salkım güvesi larvaları, elma iç kurdu, domates güvesi (Tuta absoluta), yaprak yiyen tırtıllar",
            crops = "Bağ, Elma, Sebze, Nar",
            weatherRule = "UV güneş ışığı bakteriyi hızla parçaladığı için akşamüstü serinliğinde uygulanmalıdır.",
            caution = "Yalnızca genç larva (tırtıl) döneminde etkilidir; yumurta veya pupa dönemine etki etmez, zamanlama feromon tuzağıyla belirlenir."
        ),
        BkuEntry(
            id = "selective-insecticide",
            category = BkuCategory.INSECTICIDE,
            activeGroup = "Seçici Zararlı İlaçları (IRAC Rotasyon Grupları & Feromon/Büyüme Düzenleyicileri)",
            targets = "Yaprak biti, beyazsinek, zeytin sineği, pamuklu bit, süne, badem iç kurdu",
            crops = "Zeytin, Badem, Buğday, Nar, Sebze",
            weatherRule = "Rüzgâr <12 km/s ve sıcaklık <28°C iken uygulanmalıdır. Arıların aktif uçuş saatlerinde kesinlikle uygulanmaz.",
            caution = "Çiçeklenme döneminde arılara zehirli preparatlardan kaçının; akşam geç saatlerde ve arıya zararsız yöntemleri değerlendirin."
        ),
        BkuEntry(
            id = "herbicide",
            category = BkuCategory.HERBICIDE,
            activeGroup = "Tahıl & Baklagil Yabancı Ot İlaçları (Dar ve Geniş Yapraklı Seçici Herbisitler)",
            targets = "Buğday ve arpada yabani hardal, pıtrak, yabani yulaf; nohutta çıkış öncesi/sonrası yabancı otlar",
            crops = "Buğday, Arpa, Nohut",
            weatherRule = "Gece donu beklenmeyen ılık günlerde (10°C – 22°C), rüzgâr <10 km/s iken uygulanmalıdır. Don stresi altındaki üründe sararma yapar.",
            caution = "Düşük basınçlı yelpaze hüzmeli meme kullanın; bağ, meyve bahçesi ve sebzelere drift (sürüklenme) olmamasına azami dikkat edin."
        )
    )

    val diseaseProfiles: List<DiseaseProfile> = listOf(
        DiseaseProfile(
            id = "bag_kulleme",
            cropIds = listOf("Bağ", "Elma", "Sebze", "Arpa"),
            pattern = SymptomPattern.POWDERY_WHITE,
            diseaseName = "Külleme Hastalığı (Powdery Mildew)",
            agentType = "Mantar (Fungal) — Uncinula necator / Podosphaera / Leveillula",
            visualSigns = "Yaprak üst yüzeyi, genç sürgünler ve tanelerde un serpilmiş gibi beyaz-gri pudramsı tabaka.",
            sprayRecommendation = "Koruyucu ve durdurucu olarak Mikronize Kükürt (WP/WG) veya sistemik FRAC Grup 3 / Grup 11 / Grup 13 külleme fungisitleri ile dönüşümlü uygulama yapın.",
            activeIngredients = "Mikronize Kükürt, Triazoller (FRAC 3), Strobilurinler (FRAC 11) — ürün ruhsat etiketine uygun dozda.",
            timingAndWeatherNote = "30°C üzerindeki sıcaklıklarda kükürt yaprak ve tanelerde yakıcılık yapar; sabah erken veya akşamüstü uygulayın.",
            culturalMeasures = "Asma/bitki tacını havalandıracak yeşil budama ve yaprak alma yapın, aşırı azotlu gübrelemeden kaçının.",
            preferredTempMin = 18.0,
            preferredTempMax = 30.0,
            minHumidity = 55
        ),
        DiseaseProfile(
            id = "mildiyo_kursuni",
            cropIds = listOf("Bağ", "Sebze"),
            pattern = SymptomPattern.POWDERY_WHITE,
            diseaseName = "Mildiyö veya Kurşuni Küf (Downy Mildew / Botrytis)",
            agentType = "Oomycete / Fungal — Plasmopara / Phytophthora / Botrytis cinerea",
            visualSigns = "Yaprak alt yüzeyinde beyazımsı-gri küf örtüsü, üstte yağ lekesi veya nemli dokularda gri kadifemsi çürüklük.",
            sprayRecommendation = "Yağış/nem öncesi Bakırlı veya Çok Bölgeli Kontak (FRAC M) koruyucu; hastalık başlangıcında translaminar/sistemik mildiyö (FRAC 4/40) veya anti-botrytis (FRAC 9/17) fungisitleri uygulayın.",
            activeIngredients = "Bakır Hidroksit/Oksiklorür, Metalaxyl-M/Dimethomorph/Mandipropamid grupları, Kurşuni küf için Cyprodinil/Fenhexamid grupları.",
            timingAndWeatherNote = "Yaprak ıslaklığı uzamadan ve yağıştan önce koruyucu kaplama yapılmalıdır; aynı etki grubunu art arda 2 defadan fazla kullanmayın.",
            culturalMeasures = "Akşam sulamasını durdurun, damlama sulamaya geçin, sera içi havalandırmayı artırın ve çürüyen organları temizleyin.",
            preferredTempMin = 14.0,
            preferredTempMax = 26.0,
            minHumidity = 75
        ),
        DiseaseProfile(
            id = "tahil_pas",
            cropIds = listOf("Buğday", "Arpa"),
            pattern = SymptomPattern.YELLOW_RUST,
            diseaseName = "Sarı / Kahverengi Pas veya Yaprak Klorozu",
            agentType = "Fungal (Puccinia striiformis / P. triticina)",
            visualSigns = "Yaprak ayasında damarlar boyunca çizgi halinde sarı-turuncu tozlu pas püstülleri ve çevrelerinde sararma.",
            sprayRecommendation = "Bayrak yaprak korunması için ilk pas püstülleri görüldüğünde Sistemik Triazol (FRAC 3) veya Triazol + Strobilurin (FRAC 3+11) kombinasyonlu fungisit uygulayın.",
            activeIngredients = "Tebuconazole / Propiconazole / Azoxystrobin / Epoxiconazole ruhsatlı kombinasyonları.",
            timingAndWeatherNote = "En az 3–4 saat yağışsız ve rüzgârı <15 km/s olan ılık saatlerde püskürtün. Pas değilse demir/azot noksanlığı da kontrol edilmelidir.",
            culturalMeasures = "Aşırı azotlu üst gübrelemeden kaçının; pas hassasiyeti yüksek tarlalarda sonraki sezon dayanıklı çeşit ve münavebe seçin.",
            preferredTempMin = 12.0,
            preferredTempMax = 25.0,
            minHumidity = 70
        ),
        DiseaseProfile(
            id = "besin_kloroz_mildiyo_yag",
            cropIds = listOf("Bağ", "Zeytin", "Badem", "Nar", "Elma", "Sebze", "Nohut"),
            pattern = SymptomPattern.YELLOW_RUST,
            diseaseName = "Yağ Lekesi (Mildiyö Başlangıcı) / Yaprak Sararması (Kloroz)",
            agentType = "Erken Fungal Enfeksiyon veya Demir/Azot/Magnezyum Noksanlığı",
            visualSigns = "Yaprakta sarımsı-açık yeşil lekelenmeler, damar arası sararma veya erken pas/akar emgi izleri.",
            sprayRecommendation = "Yaprak altı kontrolünde beyaz miselyum varsa Kontak/Bakırlı veya Sistemik Mildiyö fungisiti uygulayın; mantari spor yoksa şelatlı Demir (Fe-EDDHA) / Magnezyum yaprak gübresi verin.",
            activeIngredients = "Mildiyö şüphesinde Bakırlı/FRAC M; besin noksanlığında Şelatlı Fe + Çinko + Magnezyum yaprak beslemesi; kırmızı örümcek varsa seçici akarisit.",
            timingAndWeatherNote = "Önce büyüteçle yaprak altını kontrol edin: ağ/akar yoksa veya beyaz spor varsa etken madde seçimi ona göre yapılmalıdır.",
            culturalMeasures = "Yüksek pH’lı kireçli topraklarda damlama sulama suyunu dengeleyin; kök bölgesinde aşırı su birikimini önleyin.",
            preferredTempMin = 15.0,
            preferredTempMax = 29.0,
            minHumidity = 60
        ),
        DiseaseProfile(
            id = "elma_karaleke_halkali",
            cropIds = listOf("Elma", "Zeytin", "Badem", "Bağ"),
            pattern = SymptomPattern.NECROTIC_SPOT,
            diseaseName = "Karaleke / Halkalı Leke / Yaprak Delen (Çil)",
            agentType = "Fungal — Venturia inaequalis / Spilocaea oleagina / Wilsonomyces carpophilus",
            visualSigns = "Yaprak ve meyvede koyu kahverengi-siyah kadifemsi veya iç içe halkalı nekrotik lekeler, ilerleyince delinme ve yaprak dökümü.",
            sprayRecommendation = "Koruyucu olarak Bakırlı Preparatlar (Bordo Bulamacı / Bakır Hidroksit — çiçek dönemi hariç) veya ruhsatlı Sistemik / Kontak Karaleke-Çil fungisitleri (FRAC 3 / M4 / 9) uygulayın.",
            activeIngredients = "Bakır Tuzları, Captan/Dithianon (FRAC M4/M9), Difenoconazole/Cyprodinil (FRAC 3/9).",
            timingAndWeatherNote = "Yağış öncesi koruyucu kaplama en yüksek başarıyı sağlar; donlu ve >30°C sıcak günlerde bakır uygulamayın.",
            culturalMeasures = "Taç içi havalanma budaması yapın, yere dökülen hastalıklı yaprak ve mumya meyveleri sonbaharda toplayıp uzaklaştırın.",
            preferredTempMin = 10.0,
            preferredTempMax = 24.0,
            minHumidity = 75
        ),
        DiseaseProfile(
            id = "antraknoz_alternaria_septorya",
            cropIds = listOf("Nohut", "Sebze", "Nar", "Buğday", "Arpa"),
            pattern = SymptomPattern.NECROTIC_SPOT,
            diseaseName = "Antraknoz / Erken Yaprak Yanıklığı / Septorya Lekesi",
            agentType = "Fungal — Ascochyta rabiei / Alternaria solani / Zymoseptoria tritici",
            visualSigns = "Alt yapraklarda ve gövdede hedef tahtası veya iç içe halkalı koyu kahverengi-siyah çökük nekrotik lekeler.",
            sprayRecommendation = "İlk lekeler görülür görülmez geniş alanlara yayılmadan Kontak (FRAC M3/M5) veya Sistemik (FRAC 3/7/11) leke fungisitleri ile koruyucu-tedavi edici uygulama yapın.",
            activeIngredients = "Azoxystrobin, Difenoconazole, Boscalid/Pyraclostrobin veya ruhsatlı bakırlı/kontak fungisitler (ürün etiketine göre).",
            timingAndWeatherNote = "Yağışlı ve %78+ nemli günlerde yayılım çok hızlıdır; yağış arası 4+ saatlik kuru pencere yakalandığında ilaçlama yapılmalıdır.",
            culturalMeasures = "Sık ekim/dikimden kaçının, yaprakları akşam ıslatmayın, anız ve hastalıklı bitki artıklarını tarladan temizleyin.",
            preferredTempMin = 13.0,
            preferredTempMax = 26.0,
            minHumidity = 72
        ),
        DiseaseProfile(
            id = "saglikli_koruma",
            cropIds = listOf("Bağ", "Zeytin", "Badem", "Buğday", "Arpa", "Nohut", "Nar", "Elma", "Sebze"),
            pattern = SymptomPattern.HEALTHY,
            diseaseName = "Belirgin Hastalık Saptanamadı (Sağlıklı Görünüm)",
            agentType = "Sağlıklı Yeşil Doku — Koruyucu Takip Önerilir",
            visualSigns = "Görüntülenen yaprak dokusunda baskın nekroz, pas püstülü veya unsu beyaz küf tabakası tespit edilmedi.",
            sprayRecommendation = "Şu aşamada kimyasal ilaçlama gerekmez. Gereksiz ilaçlama maliyeti artırır ve faydalı böceklere zarar verir; yalnızca meteorolojik risk yüksekse koruyucu gözlem yapın.",
            activeIngredients = "Kimyasal müdahale gereksizdir; stres dönemlerinde gerekirse deniz yosunu/aminoasit veya dengeli yaprak beslemesi değerlendirilebilir.",
            timingAndWeatherNote = "7 günlük hava tahmininde yüksek nem (%78+) veya yoğun yağış öngörülüyorsa yaprak altlarını yakından tekrar inceleyin.",
            culturalMeasures = "Dengeli sulama, budama hijyeni ve haftalık tarla/bahçe gözlemini sürdürün. Şüpheli bir leke varsa yaprağa daha yakından ve gün ışığında tekrar çekim yapın.",
            preferredTempMin = 0.0,
            preferredTempMax = 45.0,
            minHumidity = 0
        )
    )
}
