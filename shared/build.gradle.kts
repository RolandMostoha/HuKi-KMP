import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.mokoResources)
    alias(libs.plugins.skie)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val secretsProperties = rootProject.layout.projectDirectory.file("secrets.properties")
val generateSecrets = tasks.register<GenerateSecretsTask>("generateSecrets") {
    secretsFile.set(secretsProperties)
    outputDirectory.set(layout.buildDirectory.dir("generated/secrets/commonMain"))
}
val generateSecretsAndroid = tasks.register<GenerateSecretsTask>("generateSecretsAndroid") {
    secretsFile.set(secretsProperties)
    platformSuffix.set("_ANDROID")
    outputDirectory.set(layout.buildDirectory.dir("generated/secrets/androidMain"))
}
val generateSecretsIos = tasks.register<GenerateSecretsTask>("generateSecretsIos") {
    secretsFile.set(secretsProperties)
    platformSuffix.set("_IOS")
    outputDirectory.set(layout.buildDirectory.dir("generated/secrets/iosMain"))
}

val generateVersionXcconfig = tasks.register<GenerateVersionXcconfigTask>("generateVersionXcconfig") {
    versionFile.set(rootProject.layout.projectDirectory.file("version.properties"))
    outputFile.set(rootProject.layout.projectDirectory.file("iosApp/Configuration/Version.xcconfig"))
}

val generateWhatsNew = tasks.register<GenerateWhatsNewTask>("generateWhatsNew") {
    versionFile.set(rootProject.layout.projectDirectory.file("version.properties"))
    whatsNewRootDir.set(rootProject.layout.projectDirectory.dir("tools/release/whatsnew"))
    commonKotlinOutputDir.set(layout.buildDirectory.dir("generated/whatsnew/commonMain"))
    androidKotlinOutputDir.set(layout.buildDirectory.dir("generated/whatsnew/androidMain"))
    iosKotlinOutputDir.set(layout.buildDirectory.dir("generated/whatsnew/iosMain"))
    baseStringsFile.set(layout.projectDirectory.file("src/commonMain/moko-resources/base/strings_whatsnew.xml"))
    huStringsFile.set(layout.projectDirectory.file("src/commonMain/moko-resources/hu/strings_whatsnew.xml"))
}

// Not wired into compilation: only fastlane (`deliver` / `supply`) consumes these files.
tasks.register<GenerateStoreReleaseNotesTask>("generateStoreReleaseNotes") {
    versionFile.set(rootProject.layout.projectDirectory.file("version.properties"))
    appVersionKey.set("iosAppVersion")
    maxLength.set(4000)
    whatsNewRootDir.set(rootProject.layout.projectDirectory.dir("tools/release/whatsnew/ios"))
    enReleaseNotesFile.set(
        rootProject.layout.projectDirectory.file("iosApp/fastlane/metadata/en-US/release_notes.txt"),
    )
    huReleaseNotesFile.set(
        rootProject.layout.projectDirectory.file("iosApp/fastlane/metadata/hu/release_notes.txt"),
    )
}

// `default.txt` is the changelog supply uploads for any versionCode without its own `<versionCode>.txt`.
tasks.register<GenerateStoreReleaseNotesTask>("generateAndroidStoreReleaseNotes") {
    versionFile.set(rootProject.layout.projectDirectory.file("version.properties"))
    appVersionKey.set("androidAppVersion")
    maxLength.set(500)
    whatsNewRootDir.set(rootProject.layout.projectDirectory.dir("tools/release/whatsnew/android"))
    enReleaseNotesFile.set(
        rootProject.layout.projectDirectory.file("composeApp/fastlane/metadata/android/en-US/changelogs/default.txt"),
    )
    huReleaseNotesFile.set(
        rootProject.layout.projectDirectory.file("composeApp/fastlane/metadata/android/hu-HU/changelogs/default.txt"),
    )
}

kotlin {
    androidLibrary {
        namespace = "hu.mostoha.mobile.huki.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
        androidResources {
            enable = true
        }
        withHostTestBuilder { }.configure {
            isIncludeAndroidResources = true
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
            kotlin.srcDir(generateWhatsNew.flatMap { it.commonKotlinOutputDir })
        }
        commonMain.dependencies {
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelSavedstate)
            api(libs.moko.resources)
            api(libs.moko.graphics)
            api(libs.moko.permissions)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.moko.permissions.location)
            implementation(libs.kermit)
            implementation(libs.bundles.ktor)
            implementation(libs.filekit.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.maplibre.gpx)
            implementation(libs.maplibre.turf)
            implementation(libs.maplibre.units)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.okio)
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
            implementation(libs.androidx.core.location.altitude)
            implementation(libs.google.play.services.location)
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.mapbox.android)
        }
        androidMain {
            kotlin.srcDir(generateSecretsAndroid.flatMap { it.outputDirectory })
            kotlin.srcDir(generateWhatsNew.flatMap { it.androidKotlinOutputDir })
        }
        iosMain {
            kotlin.srcDir(generateSecretsIos.flatMap { it.outputDirectory })
            kotlin.srcDir(generateWhatsNew.flatMap { it.iosKotlinOutputDir })
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

multiplatformResources {
    resourcesPackage.set("hu.mostoha.mobile.huki.shared")
    resourcesClassName.set("SharedRes")
    iosMinimalDeploymentTarget.set("18.0")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

skie {
    features {
        enableSwiftUIObservingPreview = true
    }
}

tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
    dependsOn(generateSecrets, generateSecretsAndroid, generateSecretsIos)
    dependsOn(generateWhatsNew)
}

tasks.matching { it.name.startsWith("generateMR") }.configureEach {
    dependsOn(generateWhatsNew)
}

tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach {
    dependsOn(generateVersionXcconfig)
}

tasks.matching { it.name == "generateAndroidHostTestLintModel" || it.name == "lintAnalyzeAndroidHostTest" }
    .configureEach {
        dependsOn("kspAndroidHostTest")
    }
