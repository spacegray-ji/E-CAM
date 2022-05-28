object Versions {
    const val gradle = "7.1.3"
    const val kotlin = "1.6.21"

    object AndroidX {
        const val core = "1.7.0"
        const val appcompat = "1.4.1"
        const val splashScreen = "1.0.0-beta02"
        const val coordinatorLayout = "1.2.0"
        const val constraintLayout = "2.1.3"
        const val dataStore = "1.0.0"
        const val lifecycle = "2.5.0-beta01"
        const val activity = "1.5.0-beta01"
        const val fragment = "1.4.1"
        const val compose_material3 = "1.0.0-alpha11"
        const val compose = "1.2.0-beta02"
        const val navigation = "2.5.0-beta01"
        const val paging3 = "3.1.1"
        const val paging3_compose = "1.0.0-alpha14"
    }

    object Accompanist {
        const val general = "0.24.9-beta"
    }

    object Kotlin {
        const val coroutines = "1.3.9"
        const val serialization_json = "1.3.2"
        const val datetime = "0.3.3"
    }

    const val google_material = "1.6.0"
    const val oneui_design = "2.4.0"
    const val appIntro = "6.2.0"
    const val lottie = "5.0.3"
    const val fuel = "2.3.1"
    const val material_dialog = "3.3.0"
    const val landscapist = "1.5.2"
    const val composeDialog = "0.8.0-beta"

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
        const val dataStore = "androidx.datastore:datastore:${Versions.AndroidX.dataStore}"
        const val dataStore_pref = "androidx.datastore:datastore-preferences:${Versions.AndroidX.dataStore}"
        const val lifecycle_viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
        const val lifecycle_livedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}"
        const val lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
        const val lifecycle_compose = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.AndroidX.lifecycle}"
        const val activity = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        const val fragment = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"
        const val navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.AndroidX.navigation}"
        const val navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.AndroidX.navigation}"
        const val paging3 = "androidx.paging:paging-runtime:${Versions.AndroidX.paging3}"
    }
    object AndroidX_Compose {
        const val activity = "androidx.activity:activity-compose:${Versions.AndroidX.activity}"
        const val material = "androidx.compose.material3:material3:${Versions.AndroidX.compose_material3}"
        const val material_icons = "androidx.compose.material:material-icons-core:${Versions.AndroidX.compose}"
        const val material_icons_ext = "androidx.compose.material:material-icons-extended:${Versions.AndroidX.compose}"
        const val ui = "androidx.compose.ui:ui:${Versions.AndroidX.compose}"
        const val livedata = "androidx.compose.runtime:runtime-livedata:${Versions.AndroidX.compose}"
        const val ui_tooling = "androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}"
        const val ui_tooling_preview = "androidx.compose.ui:ui-tooling-preview:${Versions.AndroidX.compose}"
        const val foundation = "androidx.compose.foundation:foundation:${Versions.AndroidX.compose}"
        const val paging3_compose = "androidx.paging:paging-compose:${Versions.AndroidX.paging3_compose}"
    }
    object Accompanist {
        const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:${Versions.Accompanist.general}"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
        const val serialization_json = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.Kotlin.serialization_json}"
        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.Kotlin.datetime}"
    }

    const val google_material = "com.google.android.material:material:${Versions.google_material}"
    const val oneui_design = "io.github.yanndroid:oneui:${Versions.oneui_design}"
    const val appIntro = "com.github.AppIntro:AppIntro:${Versions.appIntro}"
    const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
    const val lottie_compose = "com.airbnb.android:lottie-compose:${Versions.lottie}"
    const val landscapist_glide = "com.github.skydoves:landscapist-glide:${Versions.landscapist}"

    object Fuel {
        const val core = "com.github.kittinunf.fuel:fuel:${Versions.fuel}"
        const val android = "com.github.kittinunf.fuel:fuel-android:${Versions.fuel}"
        const val coroutines = "com.github.kittinunf.fuel:fuel-coroutines:${Versions.fuel}"
        const val serialization = "com.github.kittinunf.fuel:fuel-kotlinx-serialization:${Versions.fuel}"
        const val livedata = "com.github.kittinunf.fuel:fuel-livedata:${Versions.fuel}"
    }

    object MaterialDialog {
        const val core = "com.afollestad.material-dialogs:core:${Versions.material_dialog}"
        const val input = "com.afollestad.material-dialogs:input:${Versions.material_dialog}"
        const val lifecycle = "com.afollestad.material-dialogs:lifecycle:${Versions.material_dialog}"
    }

    object ComposeDialog {
        const val core = "io.github.vanpra.compose-material-dialogs:core:${Versions.composeDialog}"
        const val timePicker = "io.github.vanpra.compose-material-dialogs:datetime:${Versions.composeDialog}"
    }
}

object TestLibs {
    const val junit = "junit:junit:${Versions.junit}"
}