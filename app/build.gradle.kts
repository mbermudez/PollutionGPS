plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.pollutiongps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pollutiongps"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Ktor
   // def ktor_version = "1.6.1"
    implementation("io.ktor:ktor:1.6.1")
    implementation("io.ktor:ktor-server-netty:1.6.1")
    implementation("io.ktor:ktor-gson:1.6.1")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:3.1.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
 
    // Serialization in sharedPreferences
    implementation("com.google.code.gson:gson:2.8.8")

}