plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

group = "fr.ladder"

val timestamp: String = System.currentTimeMillis().toString(16)
val context = System.getenv("commitHash")?.take(7) ?: "local"

version = "${nextVersion}-${timestamp}-${context}"
version = System.getenv("refName")
    ?.replace("v", "")
    ?.replace("/", "-")
    ?: "${nextVersion}-${timestamp}-${context}"

repositories {
    mavenCentral()
    mavenLocal()
}

gradlePlugin {
    plugins {
        when (System.getenv("refType") ?: "branch") {
            "branch" -> {
                val timestamp: String = System.currentTimeMillis().toString(16)
                val context = System.getenv("commitHash")?.take(7) ?: "local"
                // create commit package on push to branch main
                create("releasr", Action {
                    id = "fr.ladder.releasr"
                    implementationClass = "fr.ladder.releasr.ReleasrPlugin"
                })
            }

            "tag" -> {
                // create release version
                create("releasr", Action {
                    id = "fr.ladder.releasr"
                    implementationClass = "fr.ladder.releasr.ReleasrPlugin"
                    version = System.getenv("refName")
                        ?.replace("v", "")
                        ?.replace("/", "-")
                        ?: error("'refName' not found")
                })
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

        val nexusUser: String? = findProperty("REPO_USER") as String? ?: System.getenv("repoUser")
        val nexusPassword: String? = findProperty("REPO_PASSWORD") as String? ?: System.getenv("repoPassword")

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

// ============= METHODS OF RELEASRCONTEXT ==============

val latestVersion: String?
    get() {
        try {
            val process = ProcessBuilder()
                .command("git", "tag", "-l", "v[0-9]*")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            process.waitFor(1, TimeUnit.SECONDS)
            val tag = process.inputStream.bufferedReader()
                .readText()
                .lines()
                .filter { it.matches(Regex("^v\\d+\\.\\d+\\.\\d+$")) }
                .sortedWith { a, b -> compareSemVer(a, b) }
                .lastOrNull()

            return tag?.replace("v", "")
        } catch (_: Exception) { }

        return null;
    }

val nextVersion: String
    get() {
        val latestVersion = latestVersion ?: return "0.1.0"

        val parts = latestVersion.split('.')
        val major = parts[0].toInt()
        val minor = parts[1].toInt()
        val patch = parts[2].toInt() + 1
        return "$major.$minor.$patch"
    }

fun compareSemVer(v1: String, v2: String): Int {
    val parts1 = v1.removePrefix("v").split(".").map { it.toInt() }
    val parts2 = v2.removePrefix("v").split(".").map { it.toInt() }

    for (i in 0 until maxOf(parts1.size, parts2.size)) {
        val p1 = parts1.getOrElse(i) { 0 }
        val p2 = parts2.getOrElse(i) { 0 }
        if (p1 != p2) return p1.compareTo(p2)
    }
    return 0
}

