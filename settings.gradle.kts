pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "resilia"

include(":app")
include(":core:domain")
include(":core:content")
include(":core:data")
include(":core:designsystem")
include(":feature:home")
include(":feature:guides")
include(":feature:emergency")
include(":feature:prepare")
include(":feature:morale")
include(":feature:tools")
include(":feature:settings")
include(":feature:alerts")
include(":feature:places")
include(":tools:content-validator")
