package fr.ladder.releasr

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile

class ReleasrPlugin : Plugin<Project> {

    /**
     * Get the commit hash from the environment variable set in the CI/CD pipeline.
     * The commit hash is truncated to 7 characters.
     */
    val commitHash: String? = System.getenv("commitHash")?.take(7);

    override fun apply(target: Project) {
        target.plugins.withId("java") {
            // configure default compile parameters
            configureCompileJava(target);
        }

        target.plugins.withId("maven-publish") {

        }

        val publisher: ReleasrPublisher = ReleasrPublisher(target);
        target.afterEvaluate {
            // setup repositories
            publisher.registerGitHubRepository()
            publisher.registerNexusRepository()

            // create publication
            publisher.createPublication();
        }
    }

    fun configureCompileJava(target: Project) {
        target.tasks.named("compileJava", JavaCompile::class.java) {
            options.encoding = "UTF-8"
        }
    }

    /**
     * Constructs a version string in the format "vX.Y.Z" based on the latest tag and an incremented patch number.
     */
    fun getVersion(): String {
        val numbers: List<String> = latestTag
            ?.split('.') ?: return "local"
        val builder = StringBuilder("");

        for (i in 0 until numbers.size) {
            if (i < numbers.size - 1) {
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

}