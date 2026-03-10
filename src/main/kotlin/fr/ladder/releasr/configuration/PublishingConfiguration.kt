package fr.ladder.releasr.configuration

import fr.ladder.releasr.ReleasrContext
import fr.ladder.releasr.VersionType
import fr.ladder.releasr.extension.ReleasrExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import java.net.URI

fun configureRepositories(publishing: PublishingExtension, releasr: ReleasrExtension) {
    configureGitHubRepository(publishing)
    configureReleasrRepository(publishing, releasr)
}

private fun configureGitHubRepository(publishing: PublishingExtension) {
    if (ReleasrContext.versionType != VersionType.RELEASE)
        return

    val githubRepository = System.getenv("githubRepository")
    if (githubRepository != null) {
        publishing.repositories.maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/$githubRepository")
            credentials {
                username = System.getenv("githubUser")
                password = System.getenv("githubPassword")
            }
        }
    }
}

private fun configureReleasrRepository(publishing: PublishingExtension, releasr: ReleasrExtension) {
    if (releasr.url.orNull == null)
        return

    publishing.repositories.maven {
        name = "ReleasrPackages"
        url = URI(releasr.url.get())
        credentials {
            username = releasr.username.orNull
            password = releasr.password.orNull
        }
    }
}

fun configurePublications(publishing: PublishingExtension, target: Project) {
    when (ReleasrContext.versionType) {
        // create pre-release version

        VersionType.PRERELEASE -> {
            val timestamp: String = System.currentTimeMillis().toString(16)
            val context = System.getenv("commitHash")?.take(7) ?: "local"

            publishing.publications.create<MavenPublication>("maven") {
                artifactId = target.name
                groupId = target.group.toString()
                version = "${ReleasrContext.nextVersion}-${timestamp}-${context}"

                from(target.components.findByName("java"))
            }
        }
        // create release version
        VersionType.RELEASE -> publishing.publications.create<MavenPublication>("maven") {
            artifactId = target.name
            groupId = target.group.toString()
            version = System.getenv("refName")
                ?.replace("v", "")
                ?.replace("/", "-")
                ?: error("'refName' is required")

            from(target.components.findByName("java"))
        }
    }

}