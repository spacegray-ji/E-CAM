dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // https://stackoverflow.com/questions/69163511/build-was-configured-to-prefer-settings-repositories-over-project-repositories-b
        // Repo for project
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Molloo"
include (":app")