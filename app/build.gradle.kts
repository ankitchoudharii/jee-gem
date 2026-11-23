plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val signingKeystore = System.getenv("JEE_GEM_KEYSTORE")
val signingStorePassword = System.getenv("JEE_GEM_STORE_PASSWORD")
val signingKeyAlias = System.getenv("JEE_GEM_KEY_ALIAS")
val signingKeyPassword = System.getenv("JEE_GEM_KEY_PASSWORD")
val hasPermanentSigning = signingKeystore != null &&
    file(signingKeystore).exists() &&
    signingStorePassword != null &&
    signingKeyAlias != null

android {
    namespace = "app.jeegem"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "app.jeegem"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (hasPermanentSigning) {
            create("gem") {
                storeFile = file(signingKeystore!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ""
            signingConfig = signingConfigs.getByName(if (hasPermanentSigning) "gem" else "debug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName(if (hasPermanentSigning) "gem" else "debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
}
