package io.apicurio.umg;

import io.apicurio.umg.io.SpecificationLoader;
import io.apicurio.umg.models.spec.SpecificationModel;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class GeneratorTest {

    @Test
    public void testGenerator() throws Exception {
        File outputDir;

        String outputDirPath = System.getenv("GENERATE_TEST_OUTPUT_DIR");
        if (outputDirPath != null) {
            outputDir = new File(outputDirPath);
            outputDir.mkdirs();
        } else {
            outputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName()).toFile();
        }

        File umgTestOutputDir = Files.createTempDirectory(GeneratorTest.class.getSimpleName() + "-test").toFile();
        UnifiedModelGeneratorConfig config = UnifiedModelGeneratorConfig.builder()
                .outputDirectory(outputDir)
                .testOutputDirectory(umgTestOutputDir)
                .generateTestFixtures(false)
                .rootNamespace("io.apicurio.umg.test").build();
        // Load the specs
        List<SpecificationModel> specs = List.of(
                SpecificationLoader.loadSpec(GeneratorTest.class.getResource("openapi.yaml"))
        );
        // Create a unified model generator
        UnifiedModelGenerator generator = new UnifiedModelGenerator(config, specs);
        // Generate the source code into the target output directory.
        try {
            generator.generate();
        } finally {
            if (outputDirPath == null) {
                FileUtils.deleteDirectory(outputDir);
                FileUtils.deleteDirectory(umgTestOutputDir);
            }
        }

    }

}