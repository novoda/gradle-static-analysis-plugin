package com.novoda.staticanalysis.internal

import org.gradle.api.DomainObjectSet
import org.gradle.api.Project

final class VariantFilter {

    private final Project project
    Closure<Boolean> includeVariantsFilter

    VariantFilter(Project project) {
        this.project = project
    }

    DomainObjectSet getFilteredApplicationVariants() {
        filterVariants(project.android.applicationVariants)
    }

    DomainObjectSet getFilteredLibraryVariants() {
        filterVariants(project.android.libraryVariants)
    }

    DomainObjectSet getFilteredTestVariants() {
        filterVariants(project.android.testVariants)
    }

    DomainObjectSet getFilteredUnitTestVariants() {
        filterVariants(project.android.unitTestVariants)
    }

    private DomainObjectSet filterVariants(DomainObjectSet variants) {
        includeVariantsFilter != null ? variants.matching { includeVariantsFilter(it) } : variants
    }
}
