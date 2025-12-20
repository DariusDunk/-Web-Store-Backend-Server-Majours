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
                    .forEach(swearSet::add);
            reader.close();

        } catch (IOException e) {
            System.out.println("Error loading profanity list: " + e.getMessage());}
    }


    public boolean containsProfanity(String text) {

        String normalizedText = normalize(text);
        return swearSet.stream().anyMatch(normalizedText::contains);
    }


    private String normalize(String text) {
        return text.toLowerCase()
                .replace("4", "a")
                .replace("@", "a")
                .replace("1", "i")
                .replace("!", "i")
                .replace("0", "o")
                .replaceAll("[^a-za-я0-9]","");
    }
}
