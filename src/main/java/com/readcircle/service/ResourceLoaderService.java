package com.readcircle.service;

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

        String[] arabicParts = arabicRaw.split("###");
        String[] latinParts = latinRaw.split("###");

        StringBuilder combinedBuilder = new StringBuilder();
        int length = Math.min(arabicParts.length, latinParts.length);

        for (int i = 0; i < length; i++) {
            combinedBuilder.append(arabicParts[i].trim())
                    .append("|||")
                    .append(latinParts[i].trim())
                    .append("|||")
                    .append(defaultMeaning);

            if (i < length - 1) {
                combinedBuilder.append("###");
            }
        }
        return combinedBuilder.toString();
    }


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