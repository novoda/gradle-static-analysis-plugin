package com.novoda.staticanalysis.internal

import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectSet

trait VariantAware {

    Closure<Boolean> includeVariantsFilter

    final variantSelector = { variants ->
        includeVariantsFilter != null ? variants.matching { includeVariantsFilter(it) } : variants
    }

    DomainObjectSet<Object> getFilteredApplicationVariants() {
        variantSelector(project.android.applicationVariants)
    }

    NamedDomainObjectSet<Object> getFilteredApplicationAndTestVariants() {
        variantSelector(getAllVariants(project.android.applicationVariants))
    }

    DomainObjectSet<Object> getFilteredLibraryVariants() {
        variantSelector(project.android.libraryVariants)
    }

    NamedDomainObjectSet<Object> getFilteredLibraryAndTestVariants() {
        variantSelector(getAllVariants(project.android.libraryVariants))
    }

    private NamedDomainObjectSet<Object> getAllVariants(variants1) {
        NamedDomainObjectSet<Object> variants = project.container(Object)
        variants.addAll(variants1)
        variants.addAll(project.android.testVariants)
        variants.addAll(project.android.unitTestVariants)
        return variants
    }
}
