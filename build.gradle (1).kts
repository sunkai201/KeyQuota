plugins {
    id("com.android.application")
}

android {
    namespace = "com.chaos.keyquota"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.chaos.keyquota"
        minSdk = 24
        targetSdk = 34
        versionCode = 12
        versionName = "2.1.9"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
