plugins {
    id("com.android.application")
}

android {
    namespace = "com.rmdssoe.sos"
    compileSdk = 33

    var keystoreFile = rootProject.file("app/keystore.jks")

    defaultConfig {
        applicationId = "com.rmdssoe.sos"
        minSdk = 17
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystoreFile.exists()) {
            getByName("debug") {
                keyAlias = "debug"
                storeFile = keystoreFile
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            }
            create("release") {
                keyAlias = "release"
                storeFile = keystoreFile
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyPassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            }
        } else {
            println("Keystore file not found, skipping signing")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                println("Keystore file not found, skipping signing")
            }
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
