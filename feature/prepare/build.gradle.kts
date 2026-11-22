plugins {
    alias(libs.plugins.resilia.android.feature)
}

dependencies {
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    testImplementation(libs.androidx.work.testing)
}
