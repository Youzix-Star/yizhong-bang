plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.yizhong.bang"
    compileSdk = 37
    compileSdkMinor = 2

    defaultConfig {
        applicationId = "com.yizhong.bang"
        minSdk = 26
        targetSdk = 37
        versionCode = (findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = findProperty("versionName") as String? ?: "1.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.miuix.core)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.material)
    implementation(libs.miuix.blur)
    implementation(libs.miuix.glass)
    implementation(libs.miuix.glass.icons)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)

    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
