import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            namespace = derivedNamespace()
            compileSdk { version = compileSdkRelease(libs.version("compileSdk")) }
            defaultConfig {
                minSdk = libs.version("minSdk").toInt()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            testOptions { unitTests.isIncludeAndroidResources = true }
        }
        // Modules without tests yet (Hilt generates test sources) must not fail the build.
        tasks.withType<Test>().configureEach { failOnNoDiscoveredTests.set(false) }
    }
}
