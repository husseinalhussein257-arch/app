package com.uniquest.backend.config;

import com.uniquest.backend.model.University;
import com.uniquest.backend.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UniversityRepository universityRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeSyrianUniversities();
    }

    private void initializeSyrianUniversities() {
        List<String> universities = Arrays.asList(
            // Public Universities
            "جامعة دمشق",
            "جامعة حلب",
            "جامعة اللاذقية",
            "جامعة حمص",
            "جامعة الفرات",
            "جامعة حماة",
            "جامعة طرطوس",
            "الجامعة الافتراضية السورية",
            "جامعة إدلب",
            // Private Universities
            "جامعة القلمون",
            "الجامعة السورية الخاصة",
            "الجامعة العربية الدولية",
            "الجامعة الدولية الخاصة للعلوم والتكنولوجيا (IUST)",
            "جامعة الوادي الدولية",
            "جامعة الأندلس للعلوم الطبية",
            "جامعة الحواش",
            "جامعة اليرموك الخاصة",
            "جامعة الشهباء",
            "جامعة قاسيون",
            "جامعة إيبلا",
            "جامعة المنارة",
            "جامعة أنطاكية السورية الخاصة"
        );

        int insertedCount = 0;

        for (String universityName : universities) {
            // Check if university already exists by name
            if (!universityRepository.existsByNameAndDeletedFalse(universityName)) {
                University university = University.builder()
                    .name(universityName)
                    .deleted(false)
                    .build();

                universityRepository.save(university);
                insertedCount++;
            }
        }

        log.info("Data initialization completed. Inserted {} universities.", insertedCount);
    }
}
