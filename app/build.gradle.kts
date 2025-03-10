plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.pedroeopn.calendariocolaborativo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pedroeopn.calendariocolaborativo"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Remove the following line if the plugin is being applied via alias
    // implementation("com.google.gms:google-services:4.4.2")

    // Import the Firebase BoM for consistent versions
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    // Firestore dependency
    implementation("com.google.firebase:firebase-firestore")
}