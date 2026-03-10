package fr.ladder.releasr

import java.util.concurrent.TimeUnit

/**
 * @author Snowtyy
 */
object ReleasrContext {

    private val refType: String = System.getenv("refType") ?: "branch"

    /**
     * Get the latest version of the project.
     */
    internal val latestVersion: String?
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
            } catch (_: Exception) {
            }

            return null
        }

    /**
     * Get the next version of the project.
     *
     * This version is calculated from
     * latestVersion with a patch increment.
     */
    internal val nextVersion: String
        get() {
            val latestVersion = latestVersion ?: return "0.1.0"

            val parts = latestVersion.split('.')
            val major = parts[0].toInt()
            val minor = parts[1].toInt()
            val patch = parts[2].toInt() + 1
            return "$major.$minor.$patch"
        }

    val versionType: VersionType = if (refType == "tag") VersionType.RELEASE else VersionType.PRERELEASE

    val version: String
        get() {
            val timestamp: String = System.currentTimeMillis().toString(16)
            val context = System.getenv("commitHash")?.take(7) ?: "local"

            return System.getenv("refName")
                ?.replace("v", "")
                ?.replace("/", "-")
                ?: "${nextVersion}-${timestamp}-${context}"
        }
}

private fun compareSemVer(v1: String, v2: String): Int {
    val parts1 = v1.removePrefix("v").split(".").map { it.toInt() }
    val parts2 = v2.removePrefix("v").split(".").map { it.toInt() }

    for (i in 0 until maxOf(parts1.size, parts2.size)) {
        val p1 = parts1.getOrElse(i) { 0 }
        val p2 = parts2.getOrElse(i) { 0 }
        if (p1 != p2) return p1.compareTo(p2)
    }
    return 0
}

enum class VersionType {
    RELEASE,
    PRERELEASE
}