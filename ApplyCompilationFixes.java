import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility to apply compilation fixes to callcard modules
 */
public class ApplyCompilationFixes {

    public static void main(String[] args) throws Exception {
        System.out.println("Applying compilation fixes...");

        // Fix 1: Add GENERIC_ERROR to ExceptionTypeTO
        fixExceptionTypeTO();

        System.out.println("\nAll fixes applied successfully!");
        System.out.println("Now rebuild with: cd callcard-ws-api && mvn clean install -DskipTests -U");
    }

    private static void fixExceptionTypeTO() throws Exception {
        String filePath = "callcard-ws-api/src/main/java/com/saicon/games/callcard/exception/ExceptionTypeTO.java";
        System.out.println("\n1. Fixing ExceptionTypeTO.java...");

        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        if (content.contains("GENERIC_ERROR")) {
            System.out.println("   GENERIC_ERROR already exists - skipping");
            return;
        }

        // Add GENERIC_ERROR after GENERIC
        String original = "    public static final String GENERIC = \"9999\";";
        String replacement = "    public static final String GENERIC = \"9999\";\n" +
                           "    public static final String GENERIC_ERROR = \"9999\"; // Alias for GENERIC";

        content = content.replace(original, replacement);

        Files.write(Paths.get(filePath), content.getBytes());
        System.out.println("   Added GENERIC_ERROR constant");
    }
}
