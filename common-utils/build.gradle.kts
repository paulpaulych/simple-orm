plugins {
    kotlin("jvm")
}

kotlin {
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        api("org.slf4j:slf4j-api:1.7.30")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}