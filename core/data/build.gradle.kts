plugins {
    alias(libs.plugins.resilia.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:content"))
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.room.testing)
}

// Exported schemas are committed so migrations can be tested (MigrationTestHelper reads them as assets).
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
android {
    sourceSets["test"].assets.directories.add("$projectDir/schemas")
}

/**
 * /content (repo root) is the single source of truth. It is copied into the
 * assets at build time under "content/" (schemas excluded).
 */
abstract class CopyContentTask : DefaultTask() {
    @get:InputDirectory
    abstract val contentDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val fs: FileSystemOperations

    @TaskAction
    fun copy() {
        fs.sync {
            from(contentDir) { exclude("schema/**") }
            into(outputDir.dir("content"))
        }
    }
}

val copyContentAssets = tasks.register<CopyContentTask>("copyContentAssets") {
    contentDir.set(rootProject.layout.projectDirectory.dir("content"))
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(copyContentAssets, CopyContentTask::outputDir)
    }
}
