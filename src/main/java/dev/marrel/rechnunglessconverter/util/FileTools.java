package dev.marrel.rechnunglessconverter.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileTools {
    public static String fileToBase64String(Path file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file);
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
