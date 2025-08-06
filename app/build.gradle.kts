plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.example.yoga_admin_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.yoga_admin_app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load Firebase configuration from properties file
        val firebaseProperties = Properties()
        val firebasePropertiesFile = rootProject.file("app/firebase.properties")
        if (firebasePropertiesFile.exists()) {
            firebaseProperties.load(FileInputStream(firebasePropertiesFile))
        }
        
        // Add Firebase config to BuildConfig
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${firebaseProperties.getProperty("FIREBASE_PROJECT_ID", "")}\"")
        buildConfigField("String", "FIREBASE_DATABASE_REGION", "\"${firebaseProperties.getProperty("FIREBASE_DATABASE_REGION", "")}\"")
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
    }
    
    lint {
        baseline = file("lint-baseline.xml")
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
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}