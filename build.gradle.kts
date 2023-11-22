import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "8.1.4" apply false
  id("org.jetbrains.kotlin.android") version "1.9.10" apply false
  id("com.google.dagger.hilt.android") version "2.48.1" apply false
  id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
}

// from https://chrisbanes.me/posts/composable-metrics/
// to use: gradlew assembleRelease -PenableComposeCompilerReports=true
// metrics can be found after inside: ./app/build/compose_metrics
// if things seem stale, use "--rerun-tasks" as well:
// gradlew assembleRelease -PenableComposeCompilerReports=true --rerun-tasks
subprojects {
  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      if (project.findProperty("enableComposeCompilerReports") == "true") {
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    project.buildDir.absolutePath +
                    "/compose_metrics")
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.buildDir.absolutePath +
                    "/compose_metrics")
      }
    }
  }
}
