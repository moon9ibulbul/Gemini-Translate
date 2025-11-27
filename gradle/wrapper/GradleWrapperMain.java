package org.gradle.wrapper;

import java.io.File;

public class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        File projectDir = new File(System.getProperty("user.dir"));
        WrapperExecutor wrapperExecutor = WrapperExecutor.forProjectDirectory(projectDir);
        Logger logger = new Logger(true);
        File gradleUserHome = GradleUserHomeLookup.gradleUserHome();
        PathAssembler pathAssembler = new PathAssembler(gradleUserHome, projectDir);
        Download download = new Download(logger, Download.UNKNOWN_VERSION, Download.UNKNOWN_VERSION);
        Install install = new Install(logger, download, pathAssembler);
        BootstrapMainStarter starter = new BootstrapMainStarter();
        wrapperExecutor.execute(args, install, starter);
    }
}
