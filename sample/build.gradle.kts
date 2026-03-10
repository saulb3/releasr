plugins {
    id("java")
    id("fr.ladder.releasr")
}

group = "fr.ladder"

repositories {
    mavenCentral()
}

releasr {
    url = "https://repo.lylaw.fr/repository/maven-releases/"
    username = findProperty("REPO_USER") as String? ?: System.getenv("repoUser")
    password = findProperty("REPO_PASSWORD") as String? ?: System.getenv("repoPassword")
}