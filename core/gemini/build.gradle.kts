plugins {
  id("voice.library")
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(projects.core.common)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
  implementation(libs.serialization.json)
  implementation(libs.okhttp)
}
