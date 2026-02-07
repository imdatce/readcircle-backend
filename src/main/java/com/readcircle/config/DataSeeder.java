package com.readcircle.config;

import com.readcircle.model.Resource;
import com.readcircle.model.ResourceTranslation;
import com.readcircle.model.ResourceType;
import com.readcircle.repository.ResourceRepository;
import com.readcircle.service.ResourceLoaderService;
import com.readcircle.util.Constants; // <-- YENİ IMPORT
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        if (resourceRepository.findByCodeKey("QURAN") == null) {
            Resource quran = new Resource();
            quran.setCodeKey("QURAN");
            quran.setType(ResourceType.PAGED);
            quran.setTotalUnits(604);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Kuran-ı Kerim");
            tr.setUnitName("Sayfa");
            tr.setResource(quran);
            quran.setTranslations(List.of(tr));
            resourceRepository.save(quran);
        }

        // 2. ASHAB-I BEDİR
        if (resourceRepository.findByCodeKey("BEDIR") == null) {
            Resource bedir = new Resource();
            bedir.setCodeKey("BEDIR");
            bedir.setType(ResourceType.LIST_BASED);
            bedir.setTotalUnits(32);
            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Ashab-ı Bedir");
            tr.setUnitName("Grup");

            String mergedContent = resourceLoaderService.mergeTwoFiles("bedir.txt", "bedir_latin.txt", "Meal hazırlanıyor...");

            tr.setDescription(mergedContent);
            tr.setResource(bedir);
            bedir.setTranslations(List.of(tr));
            resourceRepository.save(bedir);
        }

        // 3. SALAT-I TEFRİCİYE
        if (resourceRepository.findByCodeKey("TEFRICIYE") == null) {
            Resource tefriciye = new Resource();
            tefriciye.setCodeKey("TEFRICIYE");
            tefriciye.setType(ResourceType.COUNTABLE);
            tefriciye.setTotalUnits(4444);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Salat-ı Tefriciye");
            tr.setUnitName("Adet");

            String content = resourceLoaderService.loadTextFile("tefriciye.txt");

            tr.setDescription(content);
            tr.setResource(tefriciye);
            tefriciye.setTranslations(List.of(tr));
            resourceRepository.save(tefriciye);
        }

        // 4. CEVŞEN
        if (resourceRepository.findByCodeKey("CEVSEN") == null) {
            Resource cevsen = new Resource();
            cevsen.setCodeKey("CEVSEN");
            cevsen.setType(ResourceType.LIST_BASED);
            cevsen.setTotalUnits(100);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Cevşenü'l Kebir");
            tr.setUnitName("Bab");

            String finalDescription = resourceLoaderService.mergeThreeFiles("cevsen.txt", "cevsen_latin.txt", "cevsen_meaning.txt");

            tr.setDescription(finalDescription);
            tr.setResource(cevsen);
            cevsen.setTranslations(List.of(tr));
            resourceRepository.save(cevsen);
        }

        // 5. SALAT-I MÜNCİYE
        if (resourceRepository.findByCodeKey("MUNCIYE") == null) {
            Resource munciye = new Resource();
            munciye.setCodeKey("MUNCIYE");
            munciye.setType(ResourceType.COUNTABLE);
            munciye.setTotalUnits(1000);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Salat-ı Münciye");
            tr.setUnitName("Adet");

            String content = resourceLoaderService.loadTextFile("munciye.txt");

            tr.setDescription(content);
            tr.setResource(munciye);
            munciye.setTranslations(List.of(tr));
            resourceRepository.save(munciye);
        }

        // 6. ŞÜHEDA-İ UHUD (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("UHUD") == null) {
            Resource uhud = new Resource();
            uhud.setCodeKey("UHUD");
            uhud.setType(ResourceType.JOINT);
            uhud.setTotalUnits(1);

            ResourceTranslation trUhud = new ResourceTranslation();
            trUhud.setLangCode("tr");
            trUhud.setName("Şühedâ-i Uhud");
            trUhud.setUnitName("Tamamı");

            try {
                String arabic = resourceLoaderService.loadTextFile("uhud.txt");
                String latin = resourceLoaderService.loadTextFile("uhud_latin.txt");
                // "|||" YERİNE SABİT KULLANILDI
                String finalDesc = arabic.trim() + Constants.FIELD_SEPARATOR + latin.trim()
                        + Constants.FIELD_SEPARATOR + "Şüheda-i Uhud İsim Listesi";
                trUhud.setDescription(finalDesc);
            } catch (Exception e) {
                // "|||" YERİNE SABİT KULLANILDI
                trUhud.setDescription("Hata" + Constants.FIELD_SEPARATOR + "Hata" + Constants.FIELD_SEPARATOR + "Hata");
            }

            trUhud.setResource(uhud);
            uhud.setTranslations(List.of(trUhud));
            resourceRepository.save(uhud);
        }

        // 7. YÂ LATÎF (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("YALATIF") == null) {
            Resource yaLatif = new Resource();
            yaLatif.setCodeKey("YALATIF");
            yaLatif.setType(ResourceType.JOINT);
            yaLatif.setTotalUnits(129);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Latîf");
            tr.setUnitName("Adet");

            // "|||" YERİNE SABİT KULLANILDI
            String content = "يَا لَطِيفُ" + Constants.FIELD_SEPARATOR + "Yâ Latîf"
                    + Constants.FIELD_SEPARATOR + "Ey sonsuz lütuf ve ihsan sahibi, " +
                    "en ince işlerin iç yüzünü bilen," +
                    "kullarına şefkatle muamele eden Allah.";
            tr.setDescription(content);

            tr.setResource(yaLatif);
            yaLatif.setTranslations(List.of(tr));
            resourceRepository.save(yaLatif);
        }

        // 8. YÂ HAFÎZ (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("YAHAFIZ") == null) {
            Resource yaHafiz = new Resource();
            yaHafiz.setCodeKey("YAHAFIZ");
            yaHafiz.setType(ResourceType.JOINT);
            yaHafiz.setTotalUnits(998);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Hafîz");
            tr.setUnitName("Adet");

            // "|||" YERİNE SABİT KULLANILDI
            String content = "يَا حَفِيظُ" + Constants.FIELD_SEPARATOR + "Yâ Hafîz"
                    + Constants.FIELD_SEPARATOR + "Ey her şeyi koruyan, muhafaza eden, " +
                    "hiç bir şeyin kaybolmasına müsaade etmeyen ve belalardan saklayan Allah.";
            tr.setDescription(content);

            tr.setResource(yaHafiz);
            yaHafiz.setTranslations(List.of(tr));
            resourceRepository.save(yaHafiz);
        }

        // 9. YÂ FETTÂH (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("YAFETTAH") == null) {
            Resource yaFettah = new Resource();
            yaFettah.setCodeKey("YAFETTAH");
            yaFettah.setType(ResourceType.JOINT);
            yaFettah.setTotalUnits(489);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Fettâh");
            tr.setUnitName("Adet");

            // "|||" YERİNE SABİT KULLANILDI
            String content = "يَا فَتَّاحُ" + Constants.FIELD_SEPARATOR + "Yâ Fettâh"
                    + Constants.FIELD_SEPARATOR + "Ey her türlü hayır kapılarını açan, " +
                    "maddi-manevi darlıkları gideren, zorlukları kolaylaştıran Allah.";
            tr.setDescription(content);

            tr.setResource(yaFettah);
            yaFettah.setTranslations(List.of(tr));
            resourceRepository.save(yaFettah);
        }

        // 10. HASBUNALLAH (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("HASBUNALLAH") == null) {
            Resource hasbunallah = new Resource();
            hasbunallah.setCodeKey("HASBUNALLAH");
            hasbunallah.setType(ResourceType.JOINT);
            hasbunallah.setTotalUnits(450);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Hasbunallâh");
            tr.setUnitName("Adet");

            // "|||" YERİNE SABİT KULLANILDI
            String content = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ" + Constants.FIELD_SEPARATOR +
                    "Hasbunallâhu ve ni'mel vekîl" + Constants.FIELD_SEPARATOR + "Allah bize yeter," +
                    " O ne güzel vekildir.";
            tr.setDescription(content);

            tr.setResource(hasbunallah);
            hasbunallah.setTranslations(List.of(tr));
            resourceRepository.save(hasbunallah);
        }

        // 11. LÂ HAVLE (GÜNCELLENDİ)
        if (resourceRepository.findByCodeKey("LAHAVLE") == null) {
            Resource lahavle = new Resource();
            lahavle.setCodeKey("LAHAVLE");
            lahavle.setType(ResourceType.JOINT);
            lahavle.setTotalUnits(199);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Lâ Havle");
            tr.setUnitName("Adet");

            // "|||" YERİNE SABİT KULLANILDI
            String content = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ" + Constants.FIELD_SEPARATOR +
                    "Lâ havle ve lâ kuvvete illâ billâh" + Constants.FIELD_SEPARATOR + "Güç ve kuvvet, sadece " +
                    "Yüce ve Büyük olan Allah'ın yardımıyladır.";
            tr.setDescription(content);

            tr.setResource(lahavle);
            lahavle.setTranslations(List.of(tr));
            resourceRepository.save(lahavle);
        }

        // 12. FETİH SURESİ
        if (resourceRepository.findByCodeKey("FETIH") == null) {
            Resource fetih = new Resource();
            fetih.setCodeKey("FETIH");
            fetih.setType(ResourceType.JOINT);
            fetih.setTotalUnits(1);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Fetih Suresi");
            tr.setUnitName("Adet");

            String content = resourceLoaderService.loadTextFile("fetih.txt");

            tr.setDescription(content);
            tr.setResource(fetih);
            fetih.setTranslations(List.of(tr));
            resourceRepository.save(fetih);
        }

        // 13. YASİN SURESİ
        if (resourceRepository.findByCodeKey("YASIN") == null) {
            Resource yasin = new Resource();
            yasin.setCodeKey("YASIN");
            yasin.setType(ResourceType.JOINT);
            yasin.setTotalUnits(1);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yasin Suresi");
            tr.setUnitName("Adet");

            String content = resourceLoaderService.loadTextFile("yasin.txt");

            tr.setDescription(content);
            tr.setResource(yasin);
            yasin.setTranslations(List.of(tr));
            resourceRepository.save(yasin);
        }

        // 14. BÜYÜK SALAVAT
        if (resourceRepository.findByCodeKey("OZELSALAVAT") == null) {
            Resource res = new Resource();
            res.setCodeKey("OZELSALAVAT");
            res.setType(ResourceType.JOINT);
            res.setTotalUnits(1);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Büyük Salavat (Resimli)");
            tr.setUnitName("Adet");

            String content = resourceLoaderService.loadTextFile("salavat.txt");

            tr.setDescription(content);
            tr.setResource(res);
            res.setTranslations(List.of(tr));
            resourceRepository.save(res);
        }

        // 15. TEVHİDNAME
        if (resourceRepository.findByCodeKey("TEVHIDNAME") == null) {
            Resource tevhidname = new Resource();
            tevhidname.setCodeKey("TEVHIDNAME");
            tevhidname.setType(ResourceType.LIST_BASED);
            tevhidname.setTotalUnits(133);

            ResourceTranslation trTevhid = new ResourceTranslation();
            trTevhid.setLangCode("tr");
            trTevhid.setName("Tevhidnâme");
            trTevhid.setUnitName("Bölüm");

            String finalDescription = resourceLoaderService.mergeThreeFiles(
                    "tevhidname.txt",
                    "tevhidname_latin.txt",
                    "tevhidname_meaning.txt"
            );
            trTevhid.setDescription(finalDescription);
            trTevhid.setResource(tevhidname);

            List<ResourceTranslation> translations = new ArrayList<>();
            translations.add(trTevhid);
            tevhidname.setTranslations(translations);
            resourceRepository.save(tevhidname);
        }
    }
}