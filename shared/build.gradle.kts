import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.nativeCoroutines)
    alias(libs.plugins.mokoResources)
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
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.moko.resources)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.moko.resources.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
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
