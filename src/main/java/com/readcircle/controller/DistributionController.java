package com.readcircle.controller;

import com.readcircle.model.*;
import com.readcircle.repository.ResourceRepository;
import com.readcircle.service.DistributionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/distribution")
public class DistributionController {

    private final DistributionService service;
    private final ResourceRepository resourceRepository;

    public DistributionController(DistributionService service, ResourceRepository resourceRepository) {
        this.service = service;
        this.resourceRepository = resourceRepository;
    }

    private String loadBedirNamesFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("bedir_latin.txt");
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            return content.replace("\n", " ").replace("\r", " ");

        } catch (IOException e) {
            e.printStackTrace();
            return "Liste yüklenirken hata oluştu.";
        }
    }

    private String loadCevsenFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("cevsen.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Cevşen yüklenirken hata oluştu.";
        }
    }

    private String loadFileContent(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String mergeCevsenFiles() {
        String arabicRaw = loadFileContent("cevsen.txt");
        String latinRaw = loadFileContent("cevsen_latin.txt");
        String meaningRaw = loadFileContent("cevsen_.txt");


        String[] arabicParts = arabicRaw.split("###");
        String[] latinParts = latinRaw.split("###");

        StringBuilder combinedBuilder = new StringBuilder();

        int length = Math.min(arabicParts.length, latinParts.length);

        for (int i = 0; i < length; i++) {
            String arabic = arabicParts[i].trim();
            String latin = latinParts[i].trim();
            String meaning = "Meal yakında eklenecek...";

            combinedBuilder.append(arabic)
                    .append("|||")
                    .append(latin)
                    .append("|||")
                    .append(meaning);

            if (i < length - 1) {
                combinedBuilder.append("###");
            }
        }
        return combinedBuilder.toString();
    }
    private String loadTextFile(String fileName) {
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(fileName);

            byte[] data = resource.getInputStream().readAllBytes();
            return new String(data, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "Metin yüklenemedi: " + fileName; // Hata olursa bunu yazar
        }
    }

    private String mergeFiles(String arabicFile, String latinFile) {
        String arabicRaw = loadFileContent(arabicFile);
        String latinRaw = loadFileContent(latinFile);

        String[] arabicParts = arabicRaw.split("###");
        String[] latinParts = latinRaw.split("###");

        StringBuilder combinedBuilder = new StringBuilder();

        int length = Math.min(arabicParts.length, latinParts.length);

        for (int i = 0; i < length; i++) {
            String arabic = arabicParts[i].trim();
            String latin = latinParts[i].trim();
            String meaning = "Meal hazırlanıyor...";

            combinedBuilder.append(arabic)
                    .append("|||")
                    .append(latin)
                    .append("|||")
                    .append(meaning);

            if (i < length - 1) {
                combinedBuilder.append("###");
            }
        }
        return combinedBuilder.toString();
    }

    @GetMapping("/init")
    public String initData() {
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

        if (resourceRepository.findByCodeKey("BEDIR") == null) {
            Resource bedir = new Resource();
            bedir.setCodeKey("BEDIR");
            bedir.setType(ResourceType.LIST_BASED);
            bedir.setTotalUnits(32); // 319 isim / 10 = ~32 parça

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Ashab-ı Bedir");
            tr.setUnitName("Grup");

            String mergedContent = mergeFiles("bedir.txt", "bedir_latin.txt");
            tr.setDescription(mergedContent);

            tr.setResource(bedir);
            bedir.setTranslations(List.of(tr));
            resourceRepository.save(bedir);
        }

        if (resourceRepository.findByCodeKey("TEFRICIYE") == null) {
            Resource tefriciye = new Resource();
            tefriciye.setCodeKey("TEFRICIYE");
            tefriciye.setType(ResourceType.COUNTABLE);
            tefriciye.setTotalUnits(4444);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Salat-ı Tefriciye");
            tr.setUnitName("Adet");

            String content = loadFileContent("tefriciye.txt");
            tr.setDescription(content);

            tr.setResource(tefriciye);
            tefriciye.setTranslations(List.of(tr));
            resourceRepository.save(tefriciye);
        }

        if (resourceRepository.findByCodeKey("CEVSEN") == null) {
            Resource cevsen = new Resource();
            cevsen.setCodeKey("CEVSEN");
            cevsen.setType(ResourceType.LIST_BASED);
            cevsen.setTotalUnits(100);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Cevşenü'l Kebir");
            tr.setUnitName("Bab");

            String arabicContent = loadTextFile("cevsen.txt");
            String latinContent = loadTextFile("cevsen_latin.txt");
            String meaningContent = loadTextFile("cevsen_meaning.txt");

            String[] arabicParts = arabicContent.split("###");
            String[] latinParts = latinContent.split("###");
            String[] meaningParts = meaningContent.split("###");

            StringBuilder finalDescription = new StringBuilder();

            int limit = Math.min(arabicParts.length, Math.min(latinParts.length, meaningParts.length));

            for (int i = 0; i < limit; i++) {
                finalDescription.append(arabicParts[i].trim())
                        .append("|||")
                        .append(latinParts[i].trim())
                        .append("|||")
                        .append(meaningParts[i].trim());

                if (i < limit - 1) {
                    finalDescription.append("###");
                }
            }

            tr.setDescription(finalDescription.toString());
            tr.setResource(cevsen);
            cevsen.setTranslations(List.of(tr));
            resourceRepository.save(cevsen);
        }
        if (resourceRepository.findByCodeKey("MUNCIYE") == null) {
            Resource munciye = new Resource();
            munciye.setCodeKey("MUNCIYE");
            munciye.setType(ResourceType.COUNTABLE);
            munciye.setTotalUnits(1000);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Salat-ı Münciye");
            tr.setUnitName("Adet");

            String content = loadFileContent("munciye.txt");
            tr.setDescription(content);

            tr.setResource(munciye);
            munciye.setTranslations(List.of(tr));
            resourceRepository.save(munciye);
        }

        if (resourceRepository.findByCodeKey("YALATIF") == null) {
            Resource yaLatif = new Resource();
            yaLatif.setCodeKey("YALATIF");
            yaLatif.setType(ResourceType.JOINT);
            yaLatif.setTotalUnits(129);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Latîf");
            tr.setUnitName("Adet");

            String content = "يَا لَطِيفُ|||Yâ Latîf|||Ey sonsuz lütuf ve ihsan sahibi, " +
                    "en ince işlerin iç yüzünü bilen," +
                    "kullarına şefkatle muamele eden Allah.";
            tr.setDescription(content);

            tr.setResource(yaLatif);
            yaLatif.setTranslations(List.of(tr));
            resourceRepository.save(yaLatif);
        }

        if (resourceRepository.findByCodeKey("YAHAFIZ") == null) {
            Resource yaHafiz = new Resource();
            yaHafiz.setCodeKey("YAHAFIZ");
            yaHafiz.setType(ResourceType.JOINT);
            yaHafiz.setTotalUnits(998);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Hâfîz");
            tr.setUnitName("Adet");

            String content = "يَا حَفِيظُ|||Yâ Hafîz|||Ey her şeyi koruyan, muhafaza eden, " +
                    "hiç bir şeyin kaybolmasına müsaade etmeyen ve belalardan saklayan Allah.";
            tr.setDescription(content);

            tr.setResource(yaHafiz);
            yaHafiz.setTranslations(List.of(tr));
            resourceRepository.save(yaHafiz);
        }

        if (resourceRepository.findByCodeKey("YAFETTAH") == null) {
            Resource yaFettah = new Resource();
            yaFettah.setCodeKey("YAFETTAH");
            yaFettah.setType(ResourceType.JOINT);
            yaFettah.setTotalUnits(489);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yâ Fettâh");
            tr.setUnitName("Adet");

            String content = "يَا فَتَّاحُ|||Yâ Fettâh|||Ey her türlü hayır kapılarını açan, " +
                    "maddi-manevi darlıkları gideren, zorlukları kolaylaştıran Allah.";
            tr.setDescription(content);

            tr.setResource(yaFettah);
            yaFettah.setTranslations(List.of(tr));
            resourceRepository.save(yaFettah);
        }

        if (resourceRepository.findByCodeKey("HASBUNALLAH") == null) {
            Resource hasbunallah = new Resource();
            hasbunallah.setCodeKey("HASBUNALLAH");
            hasbunallah.setType(ResourceType.JOINT);
            hasbunallah.setTotalUnits(450);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Hasbunallâh");
            tr.setUnitName("Adet");

            String content = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ|||Hasbunallâhu ve ni'mel vekîl|||Allah bize yeter," +
                    " O ne güzel vekildir.";
            tr.setDescription(content);

            tr.setResource(hasbunallah);
            hasbunallah.setTranslations(List.of(tr));
            resourceRepository.save(hasbunallah);
        }

        if (resourceRepository.findByCodeKey("LAHAVLE") == null) {
            Resource lahavle = new Resource();
            lahavle.setCodeKey("LAHAVLE");
            lahavle.setType(ResourceType.JOINT);
            lahavle.setTotalUnits(199);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Lâ Havle");
            tr.setUnitName("Adet");

            String content = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ|||Lâ havle ve lâ kuvvete illâ billâh|||Güç ve kuvvet, sadece " +
                    "Yüce ve Büyük olan Allah'ın yardımıyladır.";
            tr.setDescription(content);

            tr.setResource(lahavle);
            lahavle.setTranslations(List.of(tr));
            resourceRepository.save(lahavle);
        }

        if (resourceRepository.findByCodeKey("FETIH") == null) {
            Resource fetih = new Resource();
            fetih.setCodeKey("FETIH");
            fetih.setType(ResourceType.JOINT);
            fetih.setTotalUnits(1);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Fetih Suresi");
            tr.setUnitName("Adet");

            String content = loadTextFile("fetih.txt");

            tr.setDescription(content);
            tr.setResource(fetih);
            fetih.setTranslations(List.of(tr));
            resourceRepository.save(fetih);
        }

        if (resourceRepository.findByCodeKey("YASIN") == null) {
            Resource yasin = new Resource();
            yasin.setCodeKey("YASIN");
            yasin.setType(ResourceType.JOINT);
            yasin.setTotalUnits(1);

            ResourceTranslation tr = new ResourceTranslation();
            tr.setLangCode("tr");
            tr.setName("Yasin Suresi");
            tr.setUnitName("Adet");

            String content = loadTextFile("yasin.txt");

            tr.setDescription(content);
            tr.setResource(yasin);
            yasin.setTranslations(List.of(tr));
            resourceRepository.save(yasin);
        }


        return "Veritabanı güncellendi!";
    }

    @GetMapping("/resources")
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @GetMapping("/create")
    public DistributionSession createSession(
            @RequestParam List<Long> resourceIds,
            @RequestParam int participants,
            @RequestParam(required = false) String customTotals
    ) {

        java.util.Map<Long, Integer> customCountsMap = new java.util.HashMap<>();

        if (customTotals != null && !customTotals.isEmpty()) {
            String[] pairs = customTotals.split(",");
            for (String pair : pairs) {
                try {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        Long resId = Long.parseLong(parts[0]);
                        Integer count = Integer.parseInt(parts[1]);
                        customCountsMap.put(resId, count);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Parse hatası: " + pair);
                }
            }
        }

        return service.createDistribution(resourceIds, participants, customCountsMap);
    }
    @GetMapping("/get/{code}")
    public DistributionSession getSession(@PathVariable String code) {
        return service.getSessionByCode(code);
    }

    @GetMapping("/take/{assignmentId}")
    public Assignment takeAssignment(@PathVariable Long assignmentId, @RequestParam String name) {
        return service.claimAssignment(assignmentId, name);
    }
}