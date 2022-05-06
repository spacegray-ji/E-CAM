object Versions {
    const val gradle = "7.1.3"
    const val kotlin = "1.6.21"

    object AndroidX {
        const val core = "1.7.0"
        const val appcompat = "1.4.1"
        const val splashScreen = "1.0.0-beta02"
        const val coordinatorLayout = "1.2.0"
        const val constraintLayout = "2.1.3"
    }

    object Kotlin {

    }

    const val google_material = "1.6.0"

    const val junit = "4.12"
}

object Libs {
    object AndroidX {
        const val core = "androidx.core:core-ktx:${Versions.AndroidX.core}"
        const val appcompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appcompat}"
        const val appcompat_res = "androidx.appcompat:appcompat-resources:${Versions.AndroidX.appcompat}"
        const val splashScreen = "androidx.core:core-splashscreen:${Versions.AndroidX.splashScreen}"
        const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:${Versions.AndroidX.coordinatorLayout}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    }

    const val google_material = "com.google.android.material:material:${Versions.google_material}"
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
}