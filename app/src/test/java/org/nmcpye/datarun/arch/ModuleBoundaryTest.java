package org.nmcpye.datarun.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing module boundary rules.
 *
 * <p>Rules:
 * <ul>
 *   <li>platform.* must NOT depend on legacy (datarun.*)</li>
 *   <li>platform-core must NOT depend on platform-capture or platform-flow</li>
 *   <li>platform-capture must NOT depend on platform-flow</li>
 * </ul>
 *
 * <p>These tests run in the app module because it has all modules on the classpath.
 */
@AnalyzeClasses(packages = "org.nmcpye", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule platform_must_not_depend_on_legacy =
        noClasses()
            .that().resideInAPackage("org.nmcpye.platform..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.nmcpye.datarun..");

    @ArchTest
    static final ArchRule core_must_not_import_capture_or_flow =
        noClasses()
            .that().resideInAPackage("org.nmcpye.platform.core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.nmcpye.platform.capture..",
                "org.nmcpye.platform.flow..");

    @ArchTest
    static final ArchRule capture_must_not_import_flow =
        noClasses()
            .that().resideInAPackage("org.nmcpye.platform.capture..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.nmcpye.platform.flow..");
}
