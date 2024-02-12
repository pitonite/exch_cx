import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.android.application") version "8.2.2" apply false
  id("org.jetbrains.kotlin.android") version "1.9.22" apply false
  id("com.google.dagger.hilt.android") version "2.50" apply false
  id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
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
