package fr.snowtyy.releasr.extension

import org.gradle.api.provider.Property

/**
 * @author Snowtyy
 */
interface ReleasrExtension {
    val url: Property<String>
    val username: Property<String>
    val password: Property<String>

    val gitHubPublicationMode: Property<GitHubPublicationMode>
}

enum class GitHubPublicationMode {
    ALWAYS,
    ONLY_RELEASE,
    NEVER
}