import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
  kotlin("plugin.serialization") version "1.9.10"
  id("com.google.protobuf") version "0.9.4"
}

val protobufVersion = "3.24.4"

android {
  namespace = "io.github.pitonite.exch_cx"
  compileSdk = 34

  defaultConfig {
    applicationId = "io.github.pitonite.exch_cx"
    minSdk = 24
    targetSdk = 34
    versionCode = 11
    versionName = "1.1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

  androidResources { generateLocaleConfig = true }

  applicationVariants.all(ApplicationVariantAction())
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0-rc01")
  implementation("androidx.activity:activity-compose:1.8.1")
  implementation(platform("androidx.compose:compose-bom:2023.10.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3:1.2.0-alpha09")
  // for adaptive layout
  implementation("androidx.compose.material3:material3-window-size-class")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

  // navigation
  implementation("androidx.navigation:navigation-compose:2.7.5")

  // hilt
  implementation("com.google.dagger:hilt-android:2.48.1")
  ksp("com.google.dagger:hilt-compiler:2.48.1")
  implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
  implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")
  // for work manager
  implementation("androidx.hilt:hilt-work:1.1.0")
  ksp("androidx.hilt:hilt-compiler:1.1.0")
  implementation("androidx.work:work-runtime-ktx:2.8.1")

  // for locale change to work, all activities need to extend AppCompatActivity
  val appcompatVersion = "1.7.0-alpha03"
  implementation("androidx.appcompat:appcompat:$appcompatVersion")
  // For loading and tinting drawables on older versions of the platform
  implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
  // for more icons:
  implementation("androidx.compose.material:material-icons-extended")
  // for json serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
  // for requesting network and parsing json responses into data class
  val ktorVersion = "3.0.0-beta-1"
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
  implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
  // for parsing xml (these are installed automatically by ktor xml dependency:
  //  implementation("io.github.pdvrieze.xmlutil:core-android:0.86.2")
  //  implementation("io.github.pdvrieze.xmlutil:serialization-android:0.86.2")

  // for datastore
  implementation("androidx.datastore:datastore:1.0.0")
  implementation("com.google.protobuf:protobuf-javalite:$protobufVersion")
  implementation("com.google.protobuf:protobuf-kotlin-lite:$protobufVersion")

  // for room
  val roomVersion = "2.6.0"
  implementation("androidx.room:room-runtime:$roomVersion")
  annotationProcessor("androidx.room:room-compiler:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  implementation("androidx.room:room-paging:$roomVersion")
  val pagingVersion = "3.2.1"
  implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
  implementation("androidx.paging:paging-compose:$pagingVersion")

  // for immutable collections (stability fix for compose)
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")

  // for splash screen
  implementation("androidx.core:core-splashscreen:1.1.0-alpha02")

  // for QRCode generation
  implementation("com.google.zxing:core:3.5.2")
  // for launching scanner
  implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}

hilt { enableAggregatingTask = true }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

// for datastore
protobuf {
  // Configures the Protobuf compilation and the protoc executable
  protoc {
    // Downloads from the repositories
    artifact = "com.google.protobuf:protoc:$protobufVersion"
  }

  // Generates the java Protobuf-lite code for the Protobufs in this project
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        // Configures the task output type
        // Lite has smaller code size and is recommended for Android
        create("java") { option("lite") }
        create("kotlin") { option("lite") }
      }
    }
  }
}

class ApplicationVariantAction : Action<ApplicationVariant> {
  override fun execute(variant: ApplicationVariant) {
    variant.outputs.all(VariantOutputAction(variant))
  }

  class VariantOutputAction(private val variant: ApplicationVariant) : Action<BaseVariantOutput> {
    override fun execute(output: BaseVariantOutput) {
      if (output is ApkVariantOutputImpl) {
        val abi =
            output.getFilter(com.android.build.api.variant.FilterConfiguration.FilterType.ABI.name)
        val abiVersionCode =
            when (abi) {
              "armeabi-v7a" -> 1
              "arm64-v8a" -> 2
              "x86" -> 3
              "x86_64" -> 4
              else -> 0
            }
        // currently splitting is disabled, but if enabled, please add arch name to the file name as well.
        val arch = abi ?: "universal"
        val versionCode = variant.versionCode * 1000 + abiVersionCode
        output.outputFileName = "exch-cx-v${variant.versionName}-${versionCode}.apk"
        output.versionCodeOverride = versionCode
      }
    }
  }
}
