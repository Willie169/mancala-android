
plugins {
    id("com.android.application")
}

android {
    namespace = "com.willie.mancala"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "com.willie.mancala"
        minSdk = 21
        targetSdk = 33
        versionCode = 11
        versionName = "1.1"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            aaptOptions.cruncherEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }
    
    lintOptions {
        disable("all")
    }
}

dependencies {
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
