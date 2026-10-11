pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
            }
        }
    }
    // The Aliyun mirrors are for local builds only. GitHub Actions runners sit
    // outside mainland China, where maven.aliyun.com intermittently answers
    // HTTP 502 — and Gradle treats any 5xx from a repository as fatal for the
    // rest of the build ("Repository ... is disabled due to earlier error"),
    // even though the official repositories are listed right after it.
    val useChinaMirrors =
        System.getenv("CI").isNullOrBlank() && System.getenv("GITHUB_ACTIONS").isNullOrBlank()

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
        if (useChinaMirrors) {
            maven { url = uri("https://maven.aliyun.com/repository/releases") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/central") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
    }
}
dependencyResolutionManagement {
    // Same rule as pluginManagement: keep the mirrors first for local builds
    // (this machine's proxy is often unavailable), but never consult them from
    // CI, where a single 502 poisons the repository for the whole build.
    val useChinaMirrors =
        System.getenv("CI").isNullOrBlank() && System.getenv("GITHUB_ACTIONS").isNullOrBlank()

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (useChinaMirrors) {
            maven { url = uri("https://www.jitpack.io") }
            maven { url = uri("https://maven.aliyun.com/repository/releases") }
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/central") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        google()
        mavenCentral()
        if (!useChinaMirrors) {
            maven { url = uri("https://www.jitpack.io") }
        }
    }
}

rootProject.name = "location"
include(":app")
include(":NewBlackbox:Bcore")
include(":NewBlackbox:black-reflection")
include(":NewBlackbox:compiler")
// Note: ":NewBlackbox:app" (BlackBox's standalone demo launcher) is intentionally
// not included — the main app only depends on :NewBlackbox:Bcore.
 
