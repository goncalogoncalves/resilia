import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

internal val JAVA_VERSION = JavaVersion.VERSION_17

/** Base package; each module's namespace derives from it. */
internal const val BASE_NAMESPACE = "com.goncalogoncalves.resilia"

/** ":core:designsystem" -> "com.goncalogoncalves.resilia.core.designsystem" */
internal fun Project.derivedNamespace(): String =
    BASE_NAMESPACE + path.replace(':', '.').replace('-', '_')

/** "37.2" -> release(37) { minorApiLevel = 2 } */
internal fun com.android.build.api.dsl.CompileSdkSpec.compileSdkRelease(value: String) =
    value.split('.').let { parts ->
        release(parts[0].toInt()) { parts.getOrNull(1)?.let { minorApiLevel = it.toInt() } }
    }
