import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mokoResources)
    alias(libs.plugins.skie)
    alias(libs.plugins.mokkery)
}

val generateSecrets = tasks.register<GenerateSecretsTask>("generateSecrets") {
    secretsFile.set(rootProject.layout.projectDirectory.file("secrets.properties"))
    outputDirectory.set(layout.buildDirectory.dir("generated/secrets"))
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            export(libs.androidx.lifecycle.viewmodel)
            export(libs.moko.resources)
            export(libs.moko.graphics)
            export(libs.moko.permissions)
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateSecrets.flatMap { it.outputDirectory })
        }
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.moko.resources)
            api(libs.moko.graphics)
            api(libs.moko.permissions)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.moko.permissions.location)
            implementation(libs.kermit)
            implementation(libs.bundles.ktor)
            implementation(libs.filekit.core)
            implementation(libs.maplibre.gpx)
            implementation(libs.maplibre.turf)
            implementation(libs.maplibre.units)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.moko.resources.test)
            implementation(libs.kotest.core)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.moko.permissions.test)
        }
        androidMain.dependencies {
            implementation(libs.google.play.services.location)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.mapbox.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

android {
    namespace = "hu.mostoha.mobile.huki.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

multiplatformResources {
    resourcesPackage.set("hu.mostoha.mobile.huki.shared")
    resourcesClassName.set("SharedRes")
    iosMinimalDeploymentTarget.set("18.0")
}

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}

tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
    dependsOn(generateSecrets)
}
