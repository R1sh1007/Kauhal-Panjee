import java.io.File
import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")

}

android {
    namespace = "com.kaushalpanjee"
    compileSdk = 35

    val  keystorePropertiesFile = rootProject.file("keystore.properties")
    val projectProperties = readProperties(keystorePropertiesFile)

    defaultConfig {
        applicationId = "com.kaushalpanjee"
        minSdk = 28
        targetSdk = 35
        versionCode = 21
        versionName = "2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Correct Kotlin DSL syntax for keeping all language resources
        resourceConfigurations += listOf("en", "hi", "as", "bn", "gu", "kn", "ml", "mr", "or", "pa", "ta", "te", "ur")

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }



    // ✅ Prevent Google Play from splitting languages (needed for in-app switching)
    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    flavorDimensions += listOf("app")
    productFlavors {
        create("dev") {
            dimension = "app"
            buildConfigField("String", "BASE_URL", projectProperties["BASE_URL_DEV"] as String)

        }
        create("prod") {
            dimension = "app"
            buildConfigField("String", "BASE_URL", projectProperties["BASE_URL_PROD"] as String)

        }
    }

    configurations.all {

        exclude(group = "xpp3", module = "xpp3")

    }
}

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}

dependencies {
    // Local AAR Library
    implementation(files("libs/pehchaanlib.aar"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation (libs.material.v150)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewbinding)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.core.v300)

    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.room.ktx)
    implementation (libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation (libs.androidx.viewpager2)


    implementation (libs.bundles.retrofitBundle)
    implementation (libs.chucker)

    //circle image

    implementation (libs.circleimageview)


    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.datastore.preferences)


    //Glide
    implementation (libs.glide)
    annotationProcessor(libs.compiler)

    implementation (libs.androidx.core.splashscreen)

    //Xml
    implementation(libs.jackson.dataformat.xml)
    implementation("com.thoughtworks.xstream:xstream:1.4.7") {
        exclude(group = "xmlpull", module = "xmlpull")
    }

    implementation(libs.bcprov.jdk16)
    implementation(libs.jsr105.api)
    implementation("org.apache.santuario:xmlsec:2.0.3") {
        exclude(group = "org.codehaus.woodstox")
    }
   // implementation(libs.stax.api)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation (libs.simple.xml)

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Transport runtime
    implementation("com.google.android.datatransport:transport-runtime:2.2.6")

    // ML Kit (Vision)
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:vision-common:16.1.7")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // SweetAlert Dialog
    implementation("com.github.f0ris.sweetalert:library:1.5.6")

    // SDP & SSP for responsive UI
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Kotlin Coroutines with Play Services
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0-RC")

    // TensorFlow Lite
    implementation("com.google.ai.edge.litert:litert:1.1.2")
    implementation("com.google.ai.edge.litert:litert-gpu:1.1.2")
    implementation("com.google.ai.edge.litert:litert-gpu-api:1.1.2")
    implementation("com.google.ai.edge.litert:litert-support:1.2.0")

    // MediaPipe Tasks Vision
    implementation("com.google.mediapipe:tasks-vision:0.10.14")

    // Media3 -Video Player
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")

}

kapt {
    correctErrorTypes = true
}