import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * A feature module: Compose UI + Hilt-injected ViewModels.
 * Features depend on core modules, never on each other.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("resilia.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        extensions.configure<LibraryExtension> {
            buildFeatures { compose = true }
        }

        dependencies {
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:designsystem"))

            add("implementation", platform(libs.lib("androidx-compose-bom")))
            add("implementation", libs.lib("androidx-compose-ui"))
            add("implementation", libs.lib("androidx-compose-material3"))
            add("implementation", libs.lib("androidx-compose-material-icons-extended"))
            add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
            add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-navigation-compose"))
            add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("kotlinx-serialization-json"))
            add("implementation", libs.lib("hilt-android"))
            add("ksp", libs.lib("hilt-compiler"))

            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("robolectric"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
            add("testImplementation", platform(libs.lib("androidx-compose-bom")))
            add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
            add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
        }
    }
}
