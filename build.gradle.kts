repositories {
    mavenCentral()
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.mokoResources) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

subprojects {
    // Detekt
    apply(plugin = "dev.detekt")
    detekt {
        config.setFrom("${rootProject.projectDir}/tools/quality/HuKi-detekt.yml")
        allRules = true
        buildUponDefaultConfig = true
        autoCorrect = false
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/jvmMain/kotlin",
            "src/desktopMain/kotlin",
            "src/main/kotlin",
        )
    }
    // Ktlint
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        android.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}
