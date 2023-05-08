import com.google.protobuf.gradle.id

tasks.register("prepareKotlinBuildScriptModel") {}

val grpcVersion = "1.54.0"
val javaxVersion = "1.3.2"
val openApiVersion = "1.7.0"
val protobufVersion = "3.19.1"
val grpcStarterVersion = "2.14.0.RELEASE"

plugins {
    java
    id("org.springframework.boot")
    id("com.google.protobuf")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("net.devh:grpc-client-spring-boot-starter:$grpcStarterVersion")

    /* GRPC */
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:protoc-gen-grpc-java:$grpcVersion")

    /* Other */
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$openApiVersion")

    /* Test */
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:${protobufVersion}" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        generateProtoTasks {
            ofSourceSet("main").forEach {
                it.plugins {
                    id("grpc")
                }
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
        proto {
            srcDirs("src/main/resources/proto")
        }
    }
}
