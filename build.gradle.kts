plugins {
  kotlin("jvm") version "2.3.20"
  application
  id("dev.detekt") version "2.0.0-alpha.2"
  id("com.ncorti.ktfmt.gradle") version "0.26.0"
}

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

detekt {
  config.setFrom(file("configs/detekt.yml"))
  buildUponDefaultConfig = true
}

dependencies { testImplementation(kotlin("test")) }

application { mainClass.set("xyz.luan.bpb.MainKt") }
