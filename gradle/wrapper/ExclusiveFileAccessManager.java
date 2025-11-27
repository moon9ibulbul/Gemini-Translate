package org.gradle.internal.file.locking;

import java.io.File;
import java.util.concurrent.Callable;

public class ExclusiveFileAccessManager {
    public ExclusiveFileAccessManager(int lockTimeoutMs, int retryDelayMs) {
        // No-op stub: avoids dependency on Gradle internals while keeping interface compatible.
    }

    public <T> T access(File file, Callable<T> action) throws Exception {
        return action.call();
    }
}
