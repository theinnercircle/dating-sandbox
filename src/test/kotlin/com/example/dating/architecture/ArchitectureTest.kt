package com.example.dating.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val productionClasses =
        ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT_PACKAGE)

    @Test
    fun `layers depend inwards only`() {
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer(DOMAIN)
            .definedBy("$ROOT_PACKAGE..domain..")
            .layer(APPLICATION)
            .definedBy("$ROOT_PACKAGE..application..")
            .layer(INFRASTRUCTURE)
            .definedBy("$ROOT_PACKAGE..infrastructure..")
            .whereLayer(INFRASTRUCTURE)
            .mayNotBeAccessedByAnyLayer()
            .whereLayer(APPLICATION)
            .mayOnlyBeAccessedByLayers(INFRASTRUCTURE)
            .whereLayer(DOMAIN)
            .mayOnlyBeAccessedByLayers(APPLICATION, INFRASTRUCTURE)
            .because("outer layers depend on inner layers, never the reverse (CLAUDE.md, Architecture)")
            .check(productionClasses)
    }

    private companion object {
        const val ROOT_PACKAGE = "com.example.dating"
        const val DOMAIN = "domain"
        const val APPLICATION = "application"
        const val INFRASTRUCTURE = "infrastructure"
    }
}
