package org.gradle.internal.file;

import java.io.IOException;

/**
 * Minimal implementation to prevent zip entry path traversal when using the Gradle wrapper.
 */
public class PathTraversalChecker {
    private static final String TRAVERSAL = "..";

    public static String safePathName(String entryName) throws IOException {
        if (entryName == null) {
            throw new IOException("Zip entry name cannot be null");
        }

        String normalized = entryName.replace('\\', '/');

        if (normalized.startsWith("/") || normalized.startsWith("../") || normalized.contains("/../")) {
            throw new IOException("Zip entry path traversal detected: " + entryName);
        }

        int colonIndex = normalized.indexOf(":");
        if (colonIndex > 0) {
            throw new IOException("Absolute path detected in zip entry: " + entryName);
        }

        if (normalized.equals(TRAVERSAL) || normalized.contains("/" + TRAVERSAL) || normalized.endsWith("/" + TRAVERSAL)) {
            throw new IOException("Zip entry path traversal detected: " + entryName);
        }

        return normalized;
    }
}
