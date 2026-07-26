plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}


android {
    namespace = "dev.metinkale.prayer.wear"
    compileSdk {
        version = release(36)
    }




    sourceSets {
        named("main") {
            res.srcDir("src/main/translations")
        }
    }

    defaultConfig {
        applicationId = "dev.metinkale.prayer.wear"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")


    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.wear.compose:compose-foundation:1.6.2")
    implementation("androidx.wear.compose:compose-material3:1.6.2")
    implementation("androidx.wear.compose:compose-ui-tooling:1.6.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.1")
    implementation("androidx.wear.protolayout:protolayout:1.4.1")
    implementation("androidx.wear.tiles:tiles-tooling-preview:1.6.1")
    implementation("androidx.wear.tiles:tiles:1.6.1")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation("androidx.wear:wear-tooling-preview:1.0.0")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")
    implementation("com.google.guava:guava:33.6.0-android")

}

