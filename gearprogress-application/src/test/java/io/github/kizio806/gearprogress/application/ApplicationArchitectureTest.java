package io.github.kizio806.gearprogress.application;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ApplicationArchitectureTest {

  @Test
  void testNoMinecraftDependenciesInApplication() {
    var importedClasses =
        new ClassFileImporter().importPackages("io.github.kizio806.gearprogress.application");

    noClasses()
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.bukkit..", "io.papermc.paper..", "net.minecraft..")
        .because("Application module must remain pure Java and platform-independent")
        .check(importedClasses);
  }
}
