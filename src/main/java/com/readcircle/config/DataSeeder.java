package com.readcircle.config;

import com.readcircle.model.Resource;
import com.readcircle.model.ResourceTranslation;
import com.readcircle.model.ResourceType;
import com.readcircle.repository.ResourceRepository;
import com.readcircle.service.ResourceLoaderService;
import com.readcircle.util.Constants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ResourceRepository resourceRepository;
    private final ResourceLoaderService resourceLoaderService;

    public DataSeeder(ResourceRepository resourceRepository, ResourceLoaderService resourceLoaderService) {
        this.resourceRepository = resourceRepository;
        this.resourceLoaderService = resourceLoaderService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        loadData();
    }

    private void loadData() {
        // 1. KURAN-I KERİM
        createOrUpdateResource(
                "QURAN",
                ResourceType.PAGED,
                604,
                Map.of(
                        "tr", "Kuran-ı Kerim",
                        "en", "The Holy Quran",
                        "fr", "Le Saint Coran",
                        "ku", "Qurana Pîroz",
                        "ar", "القرآن الكريم"
                ),
                Map.of(
                        "tr", "Sayfa",
                        "en", "Page",
                        "fr", "Page",
                        "ku", "Rûpel",
                        "ar", "صفحة"
                ),
                null
        );

        // 2. CEVŞENÜ'L KEBİR
        createOrUpdateResource(
                "CEVSEN",
                ResourceType.LIST_BASED,
                100,
                Map.of(
                        "tr", "Cevşenü'l Kebir",
                        "en", "Jawshan al-Kabir",
                        "fr", "Jawshan al-Kabir",
                        "ku", "Cewşenû'l Kebîr",
                        "ar", "الجوشن الكبير"
                ),
                Map.of(
                        "tr", "Bab",
                        "en", "Section",
                        "fr", "Section",
                        "ku", "Beş",
                        "ar", "باب"
                ),
                (lang) -> {
                    if ("tr".equals(lang)) {
                        return resourceLoaderService.mergeThreeFiles("cevsen.txt", "cevsen_latin.txt", "cevsen_tr.txt");
                    } else {
                        try {
                            return resourceLoaderService.mergeThreeFiles("cevsen.txt", "cevsen_latin.txt", "cevsen_en.txt");
                        } catch (Exception e) {
                            return resourceLoaderService.mergeThreeFiles("cevsen.txt", "cevsen_latin.txt", "cevsen_tr.txt");
                        }
                    }
                }
        );

        // 3. TEVHİDNAME
        createOrUpdateResource(
                "TEVHIDNAME",
                ResourceType.LIST_BASED,
                133,
                Map.of(
                        "tr", "Tevhidnâme",
                        "en", "Tawhidname",
                        "fr", "Tawhidname",
                        "ku", "Tewhîdname",
                        "ar", "توحيدنامة"
                ),
                Map.of(
                        "tr", "Bölüm",
                        "en", "Section",
                        "fr", "Section",
                        "ku", "Beş",
                        "ar", "باب"
                ),
                (lang) -> {
                    if (!"tr".equals(lang)) {
                        try {
                            return resourceLoaderService.mergeThreeFiles("tevhidname.txt", "tevhidname_en.txt", "tevhidname_en.txt");
                        } catch (Exception e) {
                            return resourceLoaderService.mergeThreeFiles("tevhidname.txt", "tevhidname_tr.txt", "tevhidname_tr.txt");
                        }
                    }
                    return resourceLoaderService.mergeThreeFiles("tevhidname.txt", "tevhidname_tr.txt", "tevhidname_tr.txt");
                }
        );

        // 4. FETİH SURESİ
        createOrUpdateResource(
                "FETIH",
                ResourceType.JOINT,
                1,
                Map.of(
                        "tr", "Fetih Suresi",
                        "en", "Surah Al-Fath",
                        "fr", "Sourate Al-Fath",
                        "ku", "Sureya Fetih",
                        "ar", "سورة الفتح"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    try {
                        String filename = "en".equals(lang) ? "fetih_en.txt" : "fetih_tr.txt";
                        return resourceLoaderService.loadTextFile(filename);
                    } catch (Exception e) {
                        return resourceLoaderService.loadTextFile("fetih_tr.txt");
                    }
                }
        );

        // 5. YASİN SURESİ
        createOrUpdateResource(
                "YASIN",
                ResourceType.JOINT,
                1,
                Map.of(
                        "tr", "Yasin Suresi",
                        "en", "Surah Ya-Sin",
                        "fr", "Sourate Ya-Sin",
                        "ku", "Sureya Yasîn",
                        "ar", "سورة يس"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    try {
                        String filename = "en".equals(lang) ? "yasin_en.txt" : "yasin_tr.txt";
                        return resourceLoaderService.loadTextFile(filename);
                    } catch (Exception e) {
                        return resourceLoaderService.loadTextFile("yasin_tr.txt");
                    }
                }
        );

        // 6. ASHAB-I BEDİR
        createOrUpdateResource(
                "BEDIR",
                ResourceType.LIST_BASED,
                320,
                Map.of(
                        "tr", "Ashab-ı Bedir",
                        "en", "Companions of Badr",
                        "fr", "Les Compagnons de Badr",
                        "ku", "Eshabê Bedrê",
                        "ar", "أصحاب بدر"
                ),
                Map.of(
                        "tr", "Kişi",
                        "en", "Person",
                        "fr", "Personne",
                        "ku", "Kes",
                        "ar", "شخص"
                ),
                (lang) -> {
                    if ("tr".equals(lang)) return resourceLoaderService.mergeTwoFiles("bedir.txt", "bedir_latin.txt", "Meal hazırlanıyor...");
                    return resourceLoaderService.mergeTwoFiles("bedir.txt", "bedir_latin.txt", "Translation pending...");
                }
        );

        // 7. ŞÜHEDA-İ UHUD
        createOrUpdateResource(
                "UHUD",
                ResourceType.LIST_BASED,
                70,
                Map.of(
                        "tr", "Şühedâ-i Uhud",
                        "en", "Martyrs of Uhud",
                        "fr", "Martyrs d'Uhud",
                        "ku", "Şehîdên Uhudê",
                        "ar", "شهداء أحد"
                ),
                Map.of(
                        "tr", "Kişi",
                        "en", "Person",
                        "fr", "Personne",
                        "ku", "Kes",
                        "ar", "شخص"
                ),
                (lang) -> {
                    try {
                        String arabic = resourceLoaderService.loadTextFile("uhud.txt");
                        String latin = resourceLoaderService.loadTextFile("uhud_latin.txt");
                        String meaning = "tr".equals(lang) ? "Şüheda-i Uhud İsim Listesi" : "Names of Uhud Martyrs";
                        return arabic.trim() + Constants.FIELD_SEPARATOR + latin.trim() + Constants.FIELD_SEPARATOR + meaning;
                    } catch (Exception e) {
                        return "Hata" + Constants.FIELD_SEPARATOR + "Error" + Constants.FIELD_SEPARATOR + "Error";
                    }
                }
        );

        // 8. BÜYÜK SALAVAT
        createOrUpdateResource(
                "OZELSALAVAT",
                ResourceType.JOINT,
                1,
                Map.of(
                        "tr", "Büyük Salavat (Resimli)",
                        "en", "Grand Salawat (Image)",
                        "fr", "Grand Salawat (Image)",
                        "ku", "Selawata Mezin (Bi Wêne)",
                        "ar", "الصلوات الكبيرة (صورة)"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> resourceLoaderService.loadTextFile("salavat.txt")
        );

        // 9. SALAT-I MÜNCİYE
        createOrUpdateResource(
                "MUNCIYE",
                ResourceType.COUNTABLE,
                1000,
                Map.of(
                        "tr", "Salat-ı Münciye",
                        "en", "Salat al-Munjiyah",
                        "fr", "Salat al-Munjiyah",
                        "ku", "Selata Münciye",
                        "ar", "الصلاة المنجية"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    try {
                        String filename = "en".equals(lang) ? "munciye_en.txt" : "munciye_tr.txt";
                        return resourceLoaderService.loadTextFile(filename);
                    } catch (Exception e) {
                        return resourceLoaderService.loadTextFile("munciye_tr.txt");
                    }
                }
        );

        // 10. SALAT-I TEFRİCİYE
        createOrUpdateResource(
                "TEFRICIYE",
                ResourceType.COUNTABLE,
                4444,
                Map.of(
                        "tr", "Salat-ı Tefriciye",
                        "en", "Salat al-Tafrijiyah",
                        "fr", "Salat al-Tafrijiyah",
                        "ku", "Selata Tefriciye",
                        "ar", "الصلاة التفريجية"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    try {
                        String filename = "en".equals(lang) ? "tefriciye_en.txt" : "tefriciye_tr.txt";
                        return resourceLoaderService.loadTextFile(filename);
                    } catch (Exception e) {
                        return resourceLoaderService.loadTextFile("tefriciye_tr.txt");
                    }
                }
        );

        // 11. YÂ LATÎF
        createOrUpdateResource(
                "YALATIF",
                ResourceType.JOINT,
                129,
                Map.of(
                        "tr", "Yâ Latîf",
                        "en", "Ya Latif",
                        "fr", "Ya Latif",
                        "ku", "Ya Letîf",
                        "ar", "يا لطيف"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    String meaning = "tr".equals(lang)
                            ? "Ey sonsuz lütuf ve ihsan sahibi, en ince işlerin iç yüzünü bilen, kullarına şefkatle muamele eden Allah."
                            : "O Gentle One, Who knows the subtleties of all things and treats His servants with kindness.";
                    return "يَا لَطِيفُ" + Constants.FIELD_SEPARATOR + "Yâ Latîf" + Constants.FIELD_SEPARATOR + meaning;
                }
        );

        // 12. YÂ HAFÎZ
        createOrUpdateResource(
                "YAHAFIZ",
                ResourceType.JOINT,
                998,
                Map.of(
                        "tr", "Yâ Hafîz",
                        "en", "Ya Hafiz",
                        "fr", "Ya Hafiz",
                        "ku", "Ya Hafiz",
                        "ar", "يا حفيظ"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    String meaning = "tr".equals(lang)
                            ? "Ey her şeyi koruyan, muhafaza eden, hiç bir şeyin kaybolmasına müsaade etmeyen ve belalardan saklayan Allah."
                            : "O Preserver, Who protects and preserves all things, allows nothing to be lost, and guards against calamities.";
                    return "يَا حَفِيظُ" + Constants.FIELD_SEPARATOR + "Yâ Hafîz" + Constants.FIELD_SEPARATOR + meaning;
                }
        );

        // 13. YÂ FETTÂH
        createOrUpdateResource(
                "YAFETTAH",
                ResourceType.JOINT,
                489,
                Map.of(
                        "tr", "Yâ Fettâh",
                        "en", "Ya Fattah",
                        "fr", "Ya Fattah",
                        "ku", "Ya Fettah",
                        "ar", "يا فتاح"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    String meaning = "tr".equals(lang)
                            ? "Ey her türlü hayır kapılarını açan, maddi-manevi darlıkları gideren, zorlukları kolaylaştıran Allah."
                            : "O Opener, Who opens all doors of goodness, removes material and spiritual difficulties, and eases hardships.";
                    return "يَا فَتَّاحُ" + Constants.FIELD_SEPARATOR + "Yâ Fettâh" + Constants.FIELD_SEPARATOR + meaning;
                }
        );

        // 14. HASBUNALLAH
        createOrUpdateResource(
                "HASBUNALLAH",
                ResourceType.JOINT,
                450,
                Map.of(
                        "tr", "Hasbunallâh",
                        "en", "Hasbunallah",
                        "fr", "Hasbunallah",
                        "ku", "Hasbunallah",
                        "ar", "حسبنا الله"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    String meaning = "tr".equals(lang)
                            ? "Allah bize yeter, O ne güzel vekildir."
                            : "Allah is sufficient for us, and He is the best Disposer of affairs.";
                    return "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ" + Constants.FIELD_SEPARATOR +
                            "Hasbunallâhu ve ni'mel vekîl" + Constants.FIELD_SEPARATOR + meaning;
                }
        );

        // 15. LÂ HAVLE
        createOrUpdateResource(
                "LAHAVLE",
                ResourceType.JOINT,
                199,
                Map.of(
                        "tr", "Lâ Havle",
                        "en", "La Hawla",
                        "fr", "La Hawla",
                        "ku", "La Hewle",
                        "ar", "لا حول ولا قوة إلا بالله"
                ),
                Map.of(
                        "tr", "Adet",
                        "en", "Count",
                        "fr", "Nombre",
                        "ku", "Hejmar",
                        "ar", "عدد"
                ),
                (lang) -> {
                    String meaning = "tr".equals(lang)
                            ? "Güç ve kuvvet, sadece Yüce ve Büyük olan Allah'ın yardımıyladır."
                            : "There is no power and no strength except with Allah, the Most High, the Most Great.";
                    return "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" + Constants.FIELD_SEPARATOR +
                            "Lâ havle ve lâ kuvvete illâ billâh" + Constants.FIELD_SEPARATOR + meaning;
                }
        );
    }
    private void createOrUpdateResource(
            String codeKey,
            ResourceType type,
            int totalUnits,
            Map<String, String> names,
            Map<String, String> unitNames,
            ContentProvider contentProvider
    ) {
        Resource resource = resourceRepository.findByCodeKey(codeKey);
        if (resource == null) {
            resource = new Resource();
            resource.setCodeKey(codeKey);
        }
        resource.setType(type);
        resource.setTotalUnits(totalUnits);
        resource = resourceRepository.save(resource);

        List<ResourceTranslation> translations = resource.getTranslations();
        if (translations == null) translations = new ArrayList<>();

         String[] languages = {"tr", "en", "ku", "fr", "ar"};

        for (String lang : languages) {
            ResourceTranslation tr = null;

             for (ResourceTranslation t : translations) {
                if (t.getLangCode().equals(lang)) {
                    tr = t;
                    break;
                }
            }

             if (tr == null) {
                tr = new ResourceTranslation();
                tr.setLangCode(lang);
                tr.setResource(resource);
                translations.add(tr);
            }

             tr.setName(names.getOrDefault(lang, names.get("tr")));
            tr.setUnitName(unitNames.getOrDefault(lang, unitNames.get("tr")));

             if (contentProvider != null) {
                try {
                    String content = contentProvider.getContent(lang);
                    if (content == null || content.isEmpty()) {
                         content = contentProvider.getContent("tr");
                    }
                    tr.setDescription(content);
                } catch (Exception e) {
                    tr.setDescription("Content pending...");
                }
            }
        }

        resource.setTranslations(translations);
        resourceRepository.save(resource);
    }

    @FunctionalInterface
    interface ContentProvider {
        String getContent(String lang);
    }
}