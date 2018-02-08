package com.novoda.staticanalysis.internal

import org.gradle.api.DomainObjectSet
import org.gradle.api.NamedDomainObjectSet

trait VariantAware {

    Closure<Boolean> includeVariantsFilter = { true }

    DomainObjectSet<Object> getFilteredApplicationVariants() {
        project.android.applicationVariants.matching { includeVariantsFilter(it) }
    }

    DomainObjectSet<Object> getFilteredApplicationAndTestVariants() {
        getAllVariants(project.android.applicationVariants).matching { includeVariantsFilter(it) }
    }

    DomainObjectSet<Object> getFilteredLibraryVariants() {
        project.android.libraryVariants.matching { includeVariantsFilter(it) }
    }

    DomainObjectSet<Object> getFilteredLibraryAndTestVariants() {
        getAllVariants(project.android.libraryVariants).matching { includeVariantsFilter(it) }
    }

    private NamedDomainObjectSet<Object> getAllVariants(variants1) {
        NamedDomainObjectSet<Object> variants = project.container(Object)
        variants.addAll(variants1)
        variants.addAll(project.android.testVariants)
        variants.addAll(project.android.unitTestVariants)
        return variants
    }
}
