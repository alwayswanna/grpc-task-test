buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    id("org.springframework.boot") version "2.7.11" apply false
    id("io.spring.dependency-management") version "1.0.15.RELEASE" apply false
    id("com.google.protobuf") version "0.9.2" apply false
}

allprojects {
    group = "a.gleb"
    version = "1.0.0-RELEASE"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("io.spring.dependency-management")
        plugin("com.google.protobuf")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}