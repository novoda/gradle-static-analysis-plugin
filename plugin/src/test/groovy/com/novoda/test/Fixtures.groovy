package com.novoda.test

import com.google.common.io.Resources

public final class Fixtures {
    private static final Exception NO_INSTANCE_ALLOWED = new UnsupportedOperationException("No instance allowed");
    private static final File ROOT_DIR = new File(Resources.getResource('.').file).parentFile.parentFile.parentFile.parentFile
    private static final File FIXTURES_DIR = new File(ROOT_DIR, 'src/test/fixtures')
    private static final File SOURCES_DIR = new File(FIXTURES_DIR, 'sources')
    private static final File REPORTS_DIR = new File(FIXTURES_DIR, 'reports')
    public static final File RULES_DIR = new File(FIXTURES_DIR, 'rules')
    public static final File BUILD_DIR = new File(ROOT_DIR, 'build')
    public static final File LOCAL_PROPERTIES = new File(ROOT_DIR.parentFile, 'local.properties')
    public static final File ANDROID_MANIFEST = new File(FIXTURES_DIR, 'AndroidManifest.xml')

    private Fixtures() {
        throw NO_INSTANCE_ALLOWED
    }

    public final static class Checkstyle {
        public static final File MODULES = new File(RULES_DIR, 'checkstyle/config/modules.xml')
        public static final File SOURCES_WITH_ERRORS = new File(SOURCES_DIR, 'checkstyle/errors')
        public static final File SOURCES_WITH_WARNINGS = new File(SOURCES_DIR, 'checkstyle/warnings')

        private Checkstyle() {
            throw NO_INSTANCE_ALLOWED
        }
    }

    public final static class Pmd {
        public static final File RULES = new File(RULES_DIR, 'pmd/config/rules.xml')
        public static final File SOURCES_WITH_PRIORITY_1_VIOLATION = new File(SOURCES_DIR, 'pmd/priority1')
        public static final File SOURCES_WITH_PRIORITY_2_VIOLATION = new File(SOURCES_DIR, 'pmd/priority2')
        public static final File SOURCES_WITH_PRIORITY_3_VIOLATION = new File(SOURCES_DIR, 'pmd/priority3')
        public static final File SOURCES_WITH_PRIORITY_4_VIOLATION = new File(SOURCES_DIR, 'pmd/priority4')
        public static final File SOURCES_WITH_PRIORITY_5_VIOLATION = new File(SOURCES_DIR, 'pmd/priority5')
        public static final File SAMPLE_REPORT = new File(REPORTS_DIR, 'pmd/reports/sample.xml')
    }

    public final static class Findbugs {
        public static final File SOURCES_WITH_HIGH_VIOLATION = new File(SOURCES_DIR, 'findbugs/high')
        public static final File SOURCES_WITH_MEDIUM_VIOLATION = new File(SOURCES_DIR, 'findbugs/medium')
        public static final File SOURCES_WITH_LOW_VIOLATION = new File(SOURCES_DIR, 'findbugs/low')
        public static final File SAMPLE_REPORT = new File(REPORTS_DIR, 'findbugs/reports/sample.xml')
    }

    final static class Detekt {
        public static final File SOURCES_WITH_WARNINGS = new File(SOURCES_DIR, 'detekt/warnings')
        public static final File SOURCES_WITH_ERRORS = new File(SOURCES_DIR, 'detekt/errors')
        public static final File RULES = new File(RULES_DIR, 'detekt/detekt.yml')
    }

    final static class Lint {
        public static final File SOURCES_WITH_WARNINGS = new File(SOURCES_DIR, 'lint/warnings')
        public static final File SOURCES_WITH_ERRORS = new File(SOURCES_DIR, 'lint/errors')
    }

}
