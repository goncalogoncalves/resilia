plugins {
    alias(libs.plugins.resilia.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:domain"))
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
