plugins {
  id("voice.library")
  alias(libs.plugins.metro)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  explicitApi()
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.initializer)
  implementation(projects.core.data.api)
  implementation(projects.core.gemini)
  implementation(projects.core.epub)
  implementation(projects.core.logging.api)
  implementation(libs.work.runtime)
  implementation(libs.metro.runtime)
  implementation(libs.serialization.json)
  implementation(libs.datastore)

  testImplementation(libs.bundles.testing.jvm)
  testImplementation(libs.work.testing)
  testImplementation(libs.retrofit.core)
}
