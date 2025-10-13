plugins {
    kotlin("jvm") version "2.2.20"
    `java-gradle-plugin`
    `maven-publish`

}

/**
 * Get the latest tag from the environment variable set in the CI/CD pipeline.
 * The tag is expected to be in the format "vX.Y.Z".
 * The commit hash is also retrieved from the environment variable and truncated to 7 characters.
 */
val latestTag: String? = System.getenv("latestTag")?.replace("v", "")

/**
 * Get the commit hash from the environment variable set in the CI/CD pipeline.
 * The commit hash is truncated to 7 characters.
 */
val commitHash: String? = System.getenv("commitHash")?.take(7);

group = "fr.ladder"
version = getVersion()

println("- version: $version")

repositories {
    mavenCentral()
    mavenLocal()
}

val refType = System.getenv("refType") ?: ""

gradlePlugin {
    plugins {
        when (refType) {
            "branch" -> {
                // create commit package on push to branch main
                create("releasr") {
                    id = "fr.ladder.releasr"
                    implementationClass = "fr.ladder.releasr.ReleasrPlugin"
                    version = version
                }
            }

            "tag" -> {
                val refName: String = (System.getenv("refName") ?: "")
                    .replace("v", "")
                    .replace("/", "-")
                // create a publication with the classifier
                create("releasr") {
                    id = "fr.ladder.releasr"
                    implementationClass = "fr.ladder.releasr.ReleasrPlugin"
                    version = refName
                }
            }

            else -> println("No plugin created because refType is not branch or tag")
        }
    }
}

publishing {
    repositories {
        val githubUser = System.getenv("githubUser")
        val githubPassword = System.getenv("githubPassword")
        val githubRepository = System.getenv("githubRepository")

        if(refType == "tag") {
            if(githubUser != null) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/$githubRepository")
                    credentials {
                        username = githubUser
                        password = githubPassword
                    }
                }
            }
        }

        val nexusUser: String? = findProperty("NEXUS_USER") as String? ?: System.getenv("nexusUser")
        val nexusPassword: String? = findProperty("NEXUS_PASSWORD") as String? ?: System.getenv("nexusPassword")

        if(refType.isNotEmpty()) {
            if (nexusUser != null) {
                maven {
                    name = "MavenReleases"
                    url = uri("https://repo.lylaw.fr/repository/maven-releases/")
                    credentials {
                        username = nexusUser
                        password = nexusPassword
                    }
                }
            }
        }
    }
}

/**
 * Constructs a version string in the format "vX.Y.Z" based on the latest tag and an incremented patch number.
 */
fun getVersion(): String {
    val numbers: List<String> = latestTag
        ?.split('.') ?: return "local"
    val builder = StringBuilder();

    for (i in 0 until numbers.size) {
        if(i < numbers.size - 1) {
            builder
                .append(numbers[i])
                .append(".")
        } else {
            try {
                // increment the last number (patch version)
                builder
                    .append(numbers[i].toInt() + 1)
                    .append(commitHash?.let { "-$it" } ?: "")
            } catch (ex: NumberFormatException) {
                println("Error parsing version number: ${ex.message}")
                throw ex;
            }
        }
    }
    return builder.toString()
}



