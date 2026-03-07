plugins {
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`

}

group = "fr.ladder"
version = "0.1.0-${System.currentTimeMillis()}"

repositories {
    mavenCentral()
    mavenLocal()
}

gradlePlugin {
    val refType = System.getenv("refType")

    repositories {
        if(refType == "tag") {
//            githubRepository {
//                user = System.getenv("githubUser")
//                password = System.getenv("githubPassword")
//                repository = System.getenv("githubRepository")
//            }
        }

        val nexusUser: String? = findProperty("NEXUS_USER") as String? ?: System.getenv("nexusUser")
        val nexusPassword: String? = findProperty("NEXUS_PASSWORD") as String? ?: System.getenv("nexusPassword")

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

    plugins {
        when (refType) {
            "branch" -> {
                // create commit package on push to branch main$
                create<PluginDeclaration>() {

                }
                create<PluginDeclaration>("releasr") {
                    id = "fr.ladder.releasr"
                    implementationClass = "fr.ladder.releasr.ReleasrPlugin"
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
                    this.version = refName.toString()
                }
            }

            else -> println("No plugin created because refType is not branch or tag")
        }
    }
}



