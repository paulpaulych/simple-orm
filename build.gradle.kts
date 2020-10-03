plugins {
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion apply false
}

group = "paulpaulych.simple-orm"
version = "1.4.19"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}


