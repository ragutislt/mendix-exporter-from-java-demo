package it.cloud37.mendix.export;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import it.cloud37.mendix.exporter.export.MendixExporter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMendixExporter {
    @TempDir
    static Path tempDir;
    static Path tempFile;

    @BeforeAll
    public static void init() throws IOException {
        tempFile = Files.createFile(tempDir.resolve("testResults.txt"));
    }

    @Test
    void testRestExport() throws IOException {
        // GIVEN
        Path resultsFile = Path.of("src/test/resources/mendixRestTestSample.json");

        // WHEN
        MendixExporter.exportRESTLayerTo(tempFile.toAbsolutePath().toString());

        // THEN
        assertEquals(new String(Files.readAllBytes(resultsFile)), new String(Files.readAllBytes(tempFile)));
    }
}
