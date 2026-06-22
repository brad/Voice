plugins {
  id("voice.library")
  alias(libs.plugins.metro)
}

kotlin {
  explicitApi()
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.initializer)
  implementation(libs.work.runtime)
  implementation(libs.metro.runtime)

  testImplementation(libs.bundles.testing.jvm)
}
