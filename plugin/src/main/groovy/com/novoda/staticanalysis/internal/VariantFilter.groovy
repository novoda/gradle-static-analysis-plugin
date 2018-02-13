package com.novoda.staticanalysis.internal

import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project

final class VariantFilter {

    private final Project project
    Closure<Boolean> includeVariantsFilter

    VariantFilter(Project project) {
        this.project = project
    }

    DomainObjectSet<Object> getFilteredApplicationVariants() {
        filterVariants(project.android.applicationVariants)
    }

    NamedDomainObjectSet<Object> getFilteredApplicationAndTestVariants() {
        filterVariants(getAllVariants(project.android.applicationVariants))
    }

    DomainObjectSet<Object> getFilteredLibraryVariants() {
        filterVariants(project.android.libraryVariants)
    }

    NamedDomainObjectSet<Object> getFilteredLibraryAndTestVariants() {
        filterVariants(getAllVariants(project.android.libraryVariants))
    }
    
    private def filterVariants(variants) {
        includeVariantsFilter != null ? variants.matching { includeVariantsFilter(it) } : variants
    }

    private NamedDomainObjectSet<Object> getAllVariants(variants1) {
        NamedDomainObjectSet<Object> variants = project.container(Object)
        variants.addAll(variants1)
        variants.addAll(project.android.testVariants)
        variants.addAll(project.android.unitTestVariants)
        return variants
    }
}
