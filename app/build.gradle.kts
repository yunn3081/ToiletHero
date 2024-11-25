plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val googleMapsApiKey = if (project.hasProperty("GOOGLE_MAPS_API_KEY")) {
            project.property("GOOGLE_MAPS_API_KEY") as String
        } else {
            ""
        }
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true // Corrected Kotlin DSL syntax
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.23")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:3.12.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.robolectric:robolectric:4.10")
    testImplementation("io.mockk:mockk:1.13.5")

    // Logging for Tests
    testImplementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.slf4j:slf4j-api:2.0.9")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.slf4j:slf4j-simple:1.7.32")

    // Instrumented Tests (Android Tests)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("com.google.truth:truth:1.1.3")
    debugImplementation("androidx.fragment:fragment-testing:1.6.0")
    implementation(kotlin("test"))



}
