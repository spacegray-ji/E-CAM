import kotlin.reflect.full.memberProperties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version Versions.kotlin
}

val ingrements = 9

android {
    compileSdk = Apps.compileSdk

    defaultConfig {
        applicationId = Apps.applicationId

        minSdk = Apps.minSdk
        targetSdk = Apps.targetSdk
        versionCode = Apps.versionCode
        versionName = Apps.versionName
        setProperty("archivesBaseName", "$applicationId-v$versionName($versionCode)")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.compose
    }

    namespace = "com.unopenedbox.molloo"
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // Kotlin stdlib
    Libs.Kotlin::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    // AndroidX & Google
    Libs.AndroidX::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    Libs.AndroidX_Compose::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    // Accompanist
    Libs.Accompanist::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    // Google Material
    implementation(Libs.google_material)
    // AppIntro
    implementation(Libs.appIntro)
    // Lottie (Animation)
    implementation(Libs.lottie)
    implementation(Libs.lottie_compose)
    // Landscapist (Image Loading)
    implementation(Libs.landscapist_glide)
    // Fuel
    Libs.Fuel::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    // Material-Dialog
    /*
    Libs.MaterialDialog::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
    */
    // Compose Dialog
    /*
    Libs.ComposeDialog::class.memberProperties.forEach {
        implementation(it.getter.call() as String)
    }
     */

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.AndroidX.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Versions.AndroidX.compose}")
}