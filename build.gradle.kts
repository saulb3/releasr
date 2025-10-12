plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.2.20"
}

group = "fr.ladder.releasr"
version = "1.0.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("releasr") {
            id = "fr.ladder.releasr"
            implementationClass = "fr.ladder.releasr.ReleasrPlugin"
        }
    }
}



