plugins {
  kotlin("jvm") version "2.3.20"
  kotlin("plugin.compose") version "2.3.20"
  application
  id("dev.detekt") version "2.0.0-alpha.2"
}

repositories {
  mavenCentral()
  google()
}

kotlin { jvmToolchain(21) }

detekt {
  config.setFrom(file("configs/detekt.yml"))
  buildUponDefaultConfig = true
}

dependencies {
  implementation("com.jakewharton.mosaic:mosaic-runtime:0.17.0")
  testImplementation(kotlin("test"))
}

application { mainClass.set("xyz.luan.bpb.MainKt") }
