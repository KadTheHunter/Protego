plugins {
    id("java")
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"  // Check for newer
    id("xyz.jpenilla.run-paper") version "2.3.1"  // For easy testing
}

group = "org.kaddicus"
version = "1.2"
description = "Protects Item Frames, Paintings, and other Hanging Entities from being destroyed by Projectiles, and provides a way to remove Armor Stands with negative Health / DeathTime values."

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}