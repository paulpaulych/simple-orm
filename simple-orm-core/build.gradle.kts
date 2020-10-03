plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.noarg")
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-serialization")
    implementation("io.mockk:mockk:1.9.3")

    implementation("com.charleskorn.kaml:kaml:0.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    implementation("cglib:cglib:3.3.0")

    val jacksonVersion = "2.9.0"
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("com.zaxxer:HikariCP:3.4.2")
    testImplementation("com.h2database:h2:1.4.200")
    api(project(":common-utils"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

configurations{
    allOpen {
        annotation("paulpaulych.utils.Open")
    }
}


