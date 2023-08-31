plugins {
    application
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("ru.novolmob.cicdhelper.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.5.1")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.7.3")

    implementation(group = "io.ktor", name = "ktor-client-core", version = "2.3.3")
    implementation(group = "io.ktor", name = "ktor-client-content-negotiation", version = "2.3.3")
    implementation(group = "io.ktor", name = "ktor-serialization-kotlinx-json", version = "2.3.3")
    implementation(group = "io.ktor", name = "ktor-client-cio", version = "2.3.3")
    implementation(group = "io.ktor", name = "ktor-client-resources", version = "2.3.3")

    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.20.0")
    implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = "2.20.0")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}