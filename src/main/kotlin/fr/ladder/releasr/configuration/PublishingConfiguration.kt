package fr.ladder.releasr.configuration

import fr.ladder.releasr.ReleasrContext
import fr.ladder.releasr.VersionType
import fr.ladder.releasr.extension.GitHubPublicationMode
import fr.ladder.releasr.extension.ReleasrExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import java.net.URI

fun configureRepositories(publishing: PublishingExtension, releasr: ReleasrExtension) {
    configureGitHubRepository(publishing, releasr)
    configureReleasrRepository(publishing, releasr)
}

private fun configureGitHubRepository(publishing: PublishingExtension, releasr: ReleasrExtension) {
    val mode = releasr.gitHubPublicationMode.getOrElse(GitHubPublicationMode.NEVER)
    if(mode == GitHubPublicationMode.NEVER)
        return

    if(mode == GitHubPublicationMode.ONLY_RELEASE && ReleasrContext.versionType != VersionType.RELEASE)
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

        VersionType.PRERELEASE -> publishing.publications.create<MavenPublication>("maven") {
                artifactId = target.name
                groupId = target.group.toString()
                version = ReleasrContext.version

                from(target.components.findByName("java"))
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