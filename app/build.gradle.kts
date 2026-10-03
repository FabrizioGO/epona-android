import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fabriziogo.epona"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fabriziogo.epona"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties["SUPABASE_ANON_KEY"]}\"")
        buildConfigField("String", "MAPS_API_KEY", "\"${localProperties["MAPS_API_KEY"]}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties["GOOGLE_WEB_CLIENT_ID"]}\"")
        manifestPlaceholders["MAPS_API_KEY"] = localProperties["MAPS_API_KEY"] ?: ""
    }

    // Upload-key credentials live in local.properties (gitignored) or, on CI, in
    // environment variables of the same name. See PLAY_STORE_CHECKLIST.md.
    val releaseSigningKeys = listOf(
        "RELEASE_STORE_FILE", "RELEASE_STORE_PASSWORD", "RELEASE_KEY_ALIAS", "RELEASE_KEY_PASSWORD"
    )
    fun signingValue(key: String): String? =
        (localProperties[key] as String?) ?: System.getenv(key)
    val hasReleaseSigning = releaseSigningKeys.all { !signingValue(it).isNullOrBlank() }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(signingValue("RELEASE_STORE_FILE")!!)
                storePassword = signingValue("RELEASE_STORE_PASSWORD")
                keyAlias = signingValue("RELEASE_KEY_ALIAS")
                keyPassword = signingValue("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Fail release builds that would otherwise ship unsigned or with "null" baked into
// BuildConfig (e.g. on a machine without this project's local.properties).
gradle.taskGraph.whenReady {
    val buildingRelease = allTasks.any {
        it.project == project && (it.name.contains("Release") && (it.name.startsWith("bundle") ||
            it.name.startsWith("assemble") || it.name.startsWith("package")))
    }
    if (!buildingRelease) return@whenReady
    val required = listOf(
        "SUPABASE_URL", "SUPABASE_ANON_KEY", "MAPS_API_KEY", "GOOGLE_WEB_CLIENT_ID",
        "RELEASE_STORE_FILE", "RELEASE_STORE_PASSWORD", "RELEASE_KEY_ALIAS", "RELEASE_KEY_PASSWORD"
    )
    val missing = required.filter {
        ((localProperties[it] as String?) ?: System.getenv(it)).isNullOrBlank()
    }
    if (missing.isNotEmpty()) {
        throw GradleException("Release build is missing values in local.properties: $missing")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.compose.navigation)
    ksp(libs.room.compiler)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)
    // Firebase versions come from the BOM below; never pin a Firebase artifact by hand.
    // firebase-analytics is deliberately not included: it would collect device/app data
    // this app never asked for, and the Privacy Policy / Play Data Safety form would
    // have to disclose it for a feature nothing here actually uses.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.timber)
    implementation(libs.material)
}