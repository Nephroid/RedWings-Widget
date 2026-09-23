import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { load(it) }
    } else {
        setProperty("VERSION_MAJOR", "26")
        setProperty("VERSION_MINOR", "9")
        setProperty("VERSION_PATCH", "23")
        setProperty("VERSION_BUILD", "1")
    }
}

val vMajor = (versionProps.getProperty("VERSION_MAJOR") ?: "26").toInt()
val vMinor = (versionProps.getProperty("VERSION_MINOR") ?: "9").toInt()
val vPatch = (versionProps.getProperty("VERSION_PATCH") ?: "23").toInt()
val vBuild = (versionProps.getProperty("VERSION_BUILD") ?: "1").toInt()

val appVersionCode = vMajor * 1000000 + vMinor * 10000 + vPatch * 100 + vBuild
val appVersionName = if (vBuild > 0) "$vMajor.$vMinor.$vPatch.$vBuild" else "$vMajor.$vMinor.$vPatch"

android {
  namespace = "com.redwings.widget"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.redwings.widget"
    minSdk = 24
    targetSdk = 35
    versionCode = appVersionCode
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val keystoreFile = rootProject.file("release.keystore")
    if (keystoreFile.exists()) {
      getByName("debug") {
        storeFile = keystoreFile
        storePassword = "redwingswidget"
        keyAlias = "redwingskey"
        keyPassword = "redwingswidget"
      }
      create("release") {
        storeFile = keystoreFile
        storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "redwingswidget"
        keyAlias = System.getenv("KEY_ALIAS") ?: "redwingskey"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "redwingswidget"
      }
    } else {
      create("release") {
        val debugKeystore = getByName("debug")
        storeFile = debugKeystore.storeFile
        storePassword = debugKeystore.storePassword
        keyAlias = debugKeystore.keyAlias
        keyPassword = debugKeystore.keyPassword
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      isDebuggable = true
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

tasks.register("packageVersionedApk") {
  description = "Packages and copies versioned APKs to build/outputs/apk/versioned/"
  dependsOn("assembleDebug")
  val vName = appVersionName
  val debugDir = layout.buildDirectory.dir("outputs/apk/debug")
  val versionedDir = layout.buildDirectory.dir("outputs/apk/versioned")
  doLast {
    val src = debugDir.get().file("app-debug.apk").asFile
    val destDir = versionedDir.get().asFile
    if (!destDir.exists()) destDir.mkdirs()
    if (src.exists()) {
      val dest = File(destDir, "RedWings-Widget-v${vName}.apk")
      src.copyTo(dest, overwrite = true)
      println("Created versioned APK: ${dest.absolutePath}")
    }
  }
}

tasks.register("printVersion") {
  val vName = appVersionName
  val vCode = appVersionCode
  doLast {
    println("Version Name: $vName")
    println("Version Code: $vCode")
  }
}

tasks.register("bumpVersion") {
  description = "Bumps the build number (e.g. 26.9.23 -> 26.9.23.1) in version.properties"
  val file = versionPropsFile
  doLast {
    val props = Properties()
    if (file.exists()) {
      FileInputStream(file).use { props.load(it) }
    }
    val major = (props.getProperty("VERSION_MAJOR") ?: "26").toInt()
    val minor = (props.getProperty("VERSION_MINOR") ?: "9").toInt()
    val patch = (props.getProperty("VERSION_PATCH") ?: "23").toInt()
    val currentBuild = (props.getProperty("VERSION_BUILD") ?: "1").toInt()
    val nextBuild = currentBuild + 1
    props.setProperty("VERSION_BUILD", nextBuild.toString())
    FileOutputStream(file).use { props.store(it, "Detroit Red Wings Widget Version") }
    println("Updated version: $major.$minor.$patch.$nextBuild")
  }
}

tasks.register("bumpPatch") {
  description = "Bumps the patch version (e.g. 26.9.23 -> 26.9.24) in version.properties"
  val file = versionPropsFile
  doLast {
    val props = Properties()
    if (file.exists()) {
      FileInputStream(file).use { props.load(it) }
    }
    val major = (props.getProperty("VERSION_MAJOR") ?: "26").toInt()
    val minor = (props.getProperty("VERSION_MINOR") ?: "9").toInt()
    val currentPatch = (props.getProperty("VERSION_PATCH") ?: "23").toInt()
    val nextPatch = currentPatch + 1
    props.setProperty("VERSION_PATCH", nextPatch.toString())
    props.setProperty("VERSION_BUILD", "0")
    FileOutputStream(file).use { props.store(it, "Detroit Red Wings Widget Version") }
    println("Updated version: $major.$minor.$nextPatch")
  }
}

// Minimal dependency set: Compose UI + Room + Retrofit/Moshi + Coil + coroutines.
// No AI, weather, roster, or Firebase bloat.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
