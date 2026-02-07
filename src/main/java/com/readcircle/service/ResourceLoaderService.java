package com.readcircle.service;

import com.readcircle.util.Constants; // <-- YENİ IMPORT
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ResourceLoaderService {

    public String loadTextFile(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource(fileName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "Dosya okunamadı: " + fileName;
        }
    }

    public String mergeTwoFiles(String arabicFile, String latinFile, String defaultMeaning) {
        String arabicRaw = loadTextFile(arabicFile);
        String latinRaw = loadTextFile(latinFile);

        // "###" yerine Constants.ROW_SEPARATOR kullanıyoruz
        String[] arabicParts = arabicRaw.split(Constants.ROW_SEPARATOR);
        String[] latinParts = latinRaw.split(Constants.ROW_SEPARATOR);

        StringBuilder combinedBuilder = new StringBuilder();
        int length = Math.min(arabicParts.length, latinParts.length);

        for (int i = 0; i < length; i++) {
            combinedBuilder.append(arabicParts[i].trim())
                    .append(Constants.FIELD_SEPARATOR) // "|||" yerine sabit
                    .append(latinParts[i].trim())
                    .append(Constants.FIELD_SEPARATOR) // "|||" yerine sabit
                    .append(defaultMeaning);

            if (i < length - 1) {
                combinedBuilder.append(Constants.ROW_SEPARATOR); // "###" yerine sabit
            }
        }
        return combinedBuilder.toString();
    }

    public String mergeThreeFiles(String arabicFile, String latinFile, String meaningFile) {
        String arabicRaw = loadTextFile(arabicFile);
        String latinRaw = loadTextFile(latinFile);
        String meaningRaw = loadTextFile(meaningFile);

        // "###" yerine Constants.ROW_SEPARATOR
        String[] arabicParts = arabicRaw.split(Constants.ROW_SEPARATOR);
        String[] latinParts = latinRaw.split(Constants.ROW_SEPARATOR);
        String[] meaningParts = meaningRaw.split(Constants.ROW_SEPARATOR);

        StringBuilder combinedBuilder = new StringBuilder();
        int limit = Math.min(arabicParts.length, Math.min(latinParts.length, meaningParts.length));

        for (int i = 0; i < limit; i++) {
            combinedBuilder.append(arabicParts[i].trim())
                    .append(Constants.FIELD_SEPARATOR) // "|||"
                    .append(latinParts[i].trim())
                    .append(Constants.FIELD_SEPARATOR) // "|||"
                    .append(meaningParts[i].trim());

            if (i < limit - 1) {
                combinedBuilder.append(Constants.ROW_SEPARATOR); // "###"
            }
        }
        return combinedBuilder.toString();
    }
}