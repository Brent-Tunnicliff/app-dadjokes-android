pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            // Allows importing libs from GitHub.
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "REPLACE_ME"
include(":app")
