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
            String meaning = "Meal hazırlanıyor..."; // Şimdilik sabit

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
            tr.setName("Cevşen-ül Kebir");
            tr.setUnitName("Bab");

            String mergedContent = mergeFiles("cevsen.txt", "cevsen_latin.txt");
            tr.setDescription(mergedContent);

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