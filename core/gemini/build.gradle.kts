plugins {
  id("voice.library")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.common)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
  implementation(libs.serialization.json)
  implementation(libs.okhttp)
  implementation(libs.metro.runtime)
}

dependencies {
  testImplementation(libs.bundles.testing.jvm)
}
