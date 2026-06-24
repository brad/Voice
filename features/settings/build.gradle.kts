plugins {
  id("voice.library")
  id("voice.compose")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.navigation)
  implementation(projects.core.strings)
  implementation(projects.core.featureflag)
  implementation(projects.core.playback)
  implementation(projects.core.ui)
  implementation(projects.core.remoteconfig.api)
  implementation(projects.core.data.api)
  implementation(projects.core.gemini)

  implementation(libs.androidxCore)
  implementation(libs.compose.material.icons.extended)

  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
  testImplementation(libs.retrofit.core)
}
