plugins {
    kotlin("jvm") version "2.2.20"
    `java-gradle-plugin`
    `maven-publish`

}

group = "fr.ladder"
version = getVersion()

repositories {
    mavenCentral()
    mavenLocal()
}

//gradlePlugin {
//    plugins {
//        create("releasr") {
//            id = "fr.ladder.releasr"
//            implementationClass = "fr.ladder.releasr.ReleasrPlugin"
//            version = version
//        }
//    }
//}

publishing {
    val refType = System.getenv("refType") ?: ""

    repositories {
        val githubUser = System.getenv("githubUser")
        val githubPassword = System.getenv("githubPassword")
        val githubRepository = System.getenv("githubRepository")

        println("Trying to add GitHub repository: $githubRepository")
        if(refType == "tag") {
            if(githubUser != null) {
                println("- repository added.")
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/$githubRepository")
                    credentials {
                        username = githubUser
                        password = githubPassword
                    }
                }
            } else {
                println("- repository not added (githubUser is null).")
            }
        } else {
            println("- repository not added (refType is not a branch).")
        }

        val nexusUser: String? = findProperty("NEXUS_USER") as String? ?: System.getenv("nexusUser")
        val nexusPassword: String? = findProperty("NEXUS_PASSWORD") as String? ?: System.getenv("nexusPassword")

        println("Trying to add Private repository.")
        if(refType.isNotEmpty()) {
            if (nexusUser != null) {
                println("- repository added.")
                maven {
                    name = "MavenReleases"
                    url = uri("https://repo.lylaw.fr/repository/maven-releases/")
                    credentials {
                        username = nexusUser
                        password = nexusPassword
                    }
                }
            } else {
                println("- repository not added (nexusUser is null).")
            }
        }
    }

    publications {
        when (refType) {
            "branch" -> {
                // create commit package on push to branch main
                println("Create commit package, version: $version")
                create<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = version.toString()

                    from(components["java"])
                }
            }

            "tag" -> {
                val refName: String = (System.getenv("refName") ?: "")
                    .replace("v", "")
                    .replace("/", "-")
                // create a publication with the classifier
                println("Create tag package, version: $refName")
                create<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = refName

                    from(components["java"])
                }
            }

            else -> println("No publication created because refType is not branch or tag")
        }
    }
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

/**
 * Constructs a version string in the format "vX.Y.Z" based on the latest tag and an incremented patch number.
 */
fun getVersion(): String {
    val numbers: List<String> = latestTag
        ?.replace("v", "")
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



