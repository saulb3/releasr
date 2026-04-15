package fr.snowtyy.releasr

import fr.snowtyy.releasr.configuration.configurePublications
import fr.snowtyy.releasr.configuration.configureRepositories
import fr.snowtyy.releasr.extension.ReleasrExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.compile.JavaCompile

class ReleasrPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.plugins.apply("maven-publish")
        target.plugins.withId("java") {
            // configure default compile parameters
            target.tasks.named("compileJava", JavaCompile::class.java) {
                options.encoding = "UTF-8"
            }
        }

        val releasrExtension = target.extensions.create("releasr", ReleasrExtension::class.java)

        target.afterEvaluate {
            target.extensions.configure(PublishingExtension::class.java) {
                configureRepositories(this, releasrExtension)
                configurePublications(this, target)
            }
        }
    }

}