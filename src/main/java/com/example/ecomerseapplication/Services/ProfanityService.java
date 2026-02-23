package com.example.ecomerseapplication.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class ProfanityService {

    private final Set<String> swearSet = new HashSet<>();

    @PostConstruct
    public void init() {
        load("profanity/bg.txt");
        load("profanity/en.txt");
    }

    private void load(String path) {

        try(InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            assert is != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            reader.lines()
                    .filter(line -> !line.isEmpty())
                    .map(String::toLowerCase)
                    .filter(s -> !s.contains("#"))
                    .forEach(swearSet::add);
            reader.close();

        } catch (IOException e) {
            System.out.println("Error loading profanity list: " + e.getMessage());}
    }

    public boolean containsProfanity(String text) {

        String normalizedText = normalize(text);
        return swearSet.contains(normalizedText);
    }

    private String normalize(String text) {
        return text.toLowerCase()
//                .replace("4", "ch")
                .replace("@", "a")
                .replace("1", "i")
                .replace("!", "i")
                .replace("0", "o")
//                .replace("6", "sh")
                .replaceAll("[^a-za-я0-9]","");
    }

    public String censorProfanity(String text) {
        String[] words = text.split("\\s+");
        StringBuilder censoredText = new StringBuilder();
        System.out.println("TEXT for censoring: " + text);
        Set<Character> punctuationSet = Set.of('!', '.', '?', ';', ',', ':');

        for (String word : words) {
            if (word.isEmpty()) continue;

            int end = word.length();
            while (end > 0 && punctuationSet.contains(word.charAt(end - 1))) {
                end--;
            }

            String coreWord = word.substring(0, end);
            String punctuation = word.substring(end);

            String normalized = normalize(coreWord);

            if (swearSet.contains(normalized)) {
                if (coreWord.length() > 1) {
                    censoredText.append(coreWord.charAt(0))
                            .append("*".repeat(coreWord.length() - 1));
                } else {
                    censoredText.append("*");
                }
            } else {
                censoredText.append(coreWord);
            }

            censoredText.append(punctuation).append(" ");
        }
        System.out.println("Censored text: " + censoredText.toString().trim());
        return censoredText.toString().trim();
    }
}
