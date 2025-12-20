pluginManagement {
    includeBuild("build-logic")
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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GraceOn"
include(":app")

// Core modules
include(":core:core-common")
include(":core:core-ui")
include(":core:core-network")

// Domain
include(":domain")

// Data
include(":data")

// Feature modules
include(":feature:feature-onboarding")
include(":feature:feature-worry")
include(":feature:feature-gacha")
include(":feature:feature-result")
include(":feature:feature-saved")
