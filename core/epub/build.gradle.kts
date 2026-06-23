plugins {
  id("voice.library")
}

dependencies {
  implementation(libs.epublib) {
    exclude(group = "xmlpull", module = "xmlpull")
  }
}
