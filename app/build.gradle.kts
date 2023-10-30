plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  kotlin("kapt")
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
    versionCode = 1
    versionName = "1.0"

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
}

dependencies {
  implementation("androidx.core:core-ktx:1.12.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0-alpha03")
  implementation("androidx.activity:activity-compose:1.8.0")
  implementation(platform("androidx.compose:compose-bom:2023.10.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3:1.2.0-alpha09")
  // for adaptive layout
  implementation("androidx.compose.material3:material3-window-size-class")

  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

  // navigation
  implementation("androidx.navigation:navigation-compose:2.7.4")

  // hilt
  implementation("com.google.dagger:hilt-android:2.48")
  kapt("com.google.dagger:hilt-android-compiler:2.48")
  implementation("androidx.hilt:hilt-navigation-compose:1.1.0-rc01")
  implementation("androidx.hilt:hilt-navigation-fragment:1.1.0-rc01")
  // for work manager
  implementation("androidx.hilt:hilt-work:1.0.0")
  implementation("androidx.work:work-runtime-ktx:2.8.1")

  // for locale change to work, all activities need to extend AppCompatActivity
  val appcompatVersion = "1.7.0-alpha03"
  implementation("androidx.appcompat:appcompat:$appcompatVersion")
  // For loading and tinting drawables on older versions of the platform
  implementation("androidx.appcompat:appcompat-resources:$appcompatVersion")
  // for more icons:
  implementation("androidx.compose.material:material-icons-extended")
  // for json serialization
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
  // for requesting network and parsing json responses into data class
  val ktorVersion = "2.3.2"
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktorVersion")
  implementation("io.ktor:ktor-client-android:$ktorVersion")
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
  implementation("androidx.paging:paging-compose:3.3.0-alpha02")

  // for immutable collections (stability fix for compose)
  implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")
}

// Allow references to generated code
kapt { correctErrorTypes = true }

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
