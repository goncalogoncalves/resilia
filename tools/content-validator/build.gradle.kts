plugins {
    alias(libs.plugins.resilia.jvm.library)
    application
}

dependencies {
    implementation(project(":core:content"))
    implementation(libs.json.schema.validator)
    testImplementation(libs.junit)
}

application {
    mainClass.set("com.goncalogoncalves.resilia.tools.contentvalidator.MainKt")
}

val contentDir = rootProject.file("content")

tasks.named<JavaExec>("run") {
    args(contentDir.absolutePath)
}

/** `./gradlew validateContent` — JSON Schema + semantic rules. Runs in CI. */
tasks.register<JavaExec>("validateContent") {
    group = "verification"
    description = "Validates /content against the JSON Schemas and the content rules."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(application.mainClass)
    args(contentDir.absolutePath)
    inputs.dir(contentDir)
}

tasks.withType<Test>().configureEach {
    systemProperty("resilia.contentDir", contentDir.absolutePath)
    inputs.dir(contentDir).withPropertyName("content")
}
