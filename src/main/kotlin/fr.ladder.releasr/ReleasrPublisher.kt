package fr.ladder.releasr

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class ReleasrPublisher(
    private val target: Project,
    private val githubUser: String?,
    private val githubPassword: String?,
    private val githubRepository: String?,
    private val nexusUser: String?,
    private val nexusPassword: String?,
    private val refType: String
) {

    constructor(target: Project) : this(
        target = target,
        githubUser = System.getenv("githubUser"),
        githubPassword = System.getenv("githubPassword"),
        githubRepository = System.getenv("githubRepository"),
        nexusUser = target.findProperty("NEXUS_USER") as String? ?: System.getenv("nexusUser"),
        nexusPassword = target.findProperty("NEXUS_PASSWORD") as String? ?: System.getenv("nexusPassword"),
        refType = System.getenv("refType") ?: ""
    ) {

    }

    fun registerGitHubRepository() {
        target.extensions.configure(PublishingExtension::class.java) { pub ->
            if(refType != "tag")
                return@configure;

            // register GitHub repository
            if(githubUser != null) {
                println("- register 'GitHubPackages' repository.")
                pub.repositories.maven { repo ->
                    repo.name = "GitHubPackages"
                    repo.url = target.uri("https://maven.pkg.github.com/$githubRepository")
                    repo.credentials { credentials ->
                        credentials.username = githubUser
                        credentials.password = githubPassword
                    }
                }
            } else {
                println("- 'githubUser' is null.")
            }
        }
    }

    fun registerNexusRepository() {
        target.extensions.configure(PublishingExtension::class.java) { pub ->
            // register Private repository
            if(nexusUser != null) {
                println("- register 'MavenReleases' repository.")
                pub.repositories.maven { repo ->
                    repo.name = "MavenReleases"
                    repo.url = target.uri("https://repo.lylaw.fr/repository/maven-releases/")
                    repo.credentials { credentials ->
                        credentials.username = nexusUser
                        credentials.password = nexusPassword
                    }
                }
            } else {
                println("- 'nexusUser' is null.")
            }
        }
    }

    fun createPublication() {
        target.extensions.configure(PublishingExtension::class.java) { pub ->
            when (refType) {
                "branch" -> {
                    // create commit package on push to branch main
                    pub.publications.create("maven", MavenPublication::class.java) { publication ->
                        publication.groupId = target.group.toString()
                        publication.artifactId = target.name
                        publication.version = target.version.toString()

                        publication.from(target.components.getByName("java"))
                    }
                }
                "tag" -> {
                    val refName = (System.getenv("refName") ?: "")
                        .replace("v", "")
                        .replace("/", "-")
                    // create a publication with the classifier
                    pub.publications.create("maven", MavenPublication::class.java) { publication ->
                        publication.groupId = target.group.toString()
                        publication.artifactId = target.name
                        publication.version = refName

                        publication.from(target.components.getByName("java"))
                    }
                }
                else -> println("No publication created because refType is not branch or tag")
            }
        }
    }



}