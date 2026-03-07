package fr.ladder.releasr.extension

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.net.URI

/**
 * @author Snowtyy
 */
interface GithubRepositoryExtension {
    var user: String?
    var password: String?
    var repository: String?
}

class DefaultGithubRepositoryExtension : GithubRepositoryExtension {
    override var user: String? = null
    override var password: String? = null
    override var repository: String? = null
}

fun RepositoryHandler.githubRepository(configure: GithubRepositoryExtension.() -> Unit): MavenArtifactRepository? {
    val repo = DefaultGithubRepositoryExtension().apply(configure)
    if(repo.repository == null)
        return null;
    // On crée un dépôt Maven standard mais configuré pour GitHub
    return maven {
        name = "GitHubPackages - ${repo.repository ?: "?"}"
        url = URI("https://maven.pkg.github.com/${repo.repository}")
        credentials {
            username = repo.user
            password = repo.password
        }
    }
}