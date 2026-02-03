package com.readcircle.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ResourceLoaderService {

    /**
     * Verilen dosya adını resources klasöründen okur ve String olarak döner.
     */
    public String loadTextFile(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace(); // Gerçek uygulamada loglama yapılmalı (log.error)
            return "Dosya okunamadı: " + fileName;
        }
    }

    /**
     * İki dosyayı (Arapça ve Latin) ### ayraçlarına göre parçalayıp birleştirir.
     * Bedir vb. kaynaklar için kullanılır.
     */
    public String mergeTwoFiles(String arabicFile, String latinFile, String defaultMeaning) {
        String arabicRaw = loadTextFile(arabicFile);
        String latinRaw = loadTextFile(latinFile);

        String[] arabicParts = arabicRaw.split("###");
        String[] latinParts = latinRaw.split("###");

        StringBuilder combinedBuilder = new StringBuilder();
        int length = Math.min(arabicParts.length, latinParts.length);

        for (int i = 0; i < length; i++) {
            combinedBuilder.append(arabicParts[i].trim())
                    .append("|||")
                    .append(latinParts[i].trim())
                    .append("|||")
                    .append(defaultMeaning); // Genelde "Meal hazırlanıyor..." veya sabit metin

            if (i < length - 1) {
                combinedBuilder.append("###");
            }
        }
        return combinedBuilder.toString();
    }

    /**
     * Üç dosyayı (Arapça, Latin, Meal) birleştirir.
     * Cevşen vb. kaynaklar için kullanılır.
     */
    public String mergeThreeFiles(String arabicFile, String latinFile, String meaningFile) {
        String arabicRaw = loadTextFile(arabicFile);
        String latinRaw = loadTextFile(latinFile);
        String meaningRaw = loadTextFile(meaningFile);

        String[] arabicParts = arabicRaw.split("###");
        String[] latinParts = latinRaw.split("###");
        String[] meaningParts = meaningRaw.split("###");

        StringBuilder combinedBuilder = new StringBuilder();
        int limit = Math.min(arabicParts.length, Math.min(latinParts.length, meaningParts.length));

        for (int i = 0; i < limit; i++) {
            combinedBuilder.append(arabicParts[i].trim())
                    .append("|||")
                    .append(latinParts[i].trim())
                    .append("|||")
                    .append(meaningParts[i].trim());

            if (i < limit - 1) {
                combinedBuilder.append("###");
            }
        }
        return combinedBuilder.toString();
    }
}