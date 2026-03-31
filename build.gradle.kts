plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("com.ncorti.ktfmt.gradle") version "0.21.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("xyz.luan.bpb.MainKt")
}
