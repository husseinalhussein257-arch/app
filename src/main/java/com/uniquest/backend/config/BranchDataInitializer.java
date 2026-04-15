package com.uniquest.backend.config;

import com.uniquest.backend.model.Branch;
import com.uniquest.backend.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchDataInitializer implements CommandLineRunner {

    private final BranchRepository branchRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeBranches();
    }

    private void initializeBranches() {
        List<String> branches = Arrays.asList(
            "الطب البشري",
            "طب الأسنان",
            "الصيدلة",
            "الهندسة المدنية",
            "الهندسة المعمارية",
            "الهندسة الميكانيكية",
            "الهندسة الكهربائية والإلكترونية",
            "الهندسة المعلوماتية",
            "كلية العلوم",
            "كلية الاقتصاد",
            "كلية الحقوق",
            "كلية الآداب والعلوم الإنسانية",
            "كلية التربية",
            "كلية التربية الرياضية",
            "كلية الشريعة",
            "كلية الزراعة",
            "كلية الفنون الجميلة",
            "كلية السياحة",
            "كلية الإعلام"
        );

        int insertedCount = 0;

        for (String branchName : branches) {
            // Check if branch already exists by name
            if (!branchRepository.existsByNameAndDeletedFalse(branchName)) {
                Branch branch = Branch.builder()
                    .name(branchName)
                    .deleted(false)
                    .build();

                branchRepository.save(branch);
                insertedCount++;
            }
        }

        log.info("Branch initialization completed. Inserted {} branches.", insertedCount);
    }
}
