plugins {
    alias(libs.plugins.resilia.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:domain"))
    testImplementation(libs.junit)
}

// Unit tests also load the real /content folder, so a broken content file fails here too.
tasks.withType<Test>().configureEach {
    systemProperty("resilia.contentDir", rootProject.file("content").absolutePath)
    inputs.dir(rootProject.file("content")).withPropertyName("content")
}
