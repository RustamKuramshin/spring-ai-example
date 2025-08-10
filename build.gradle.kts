plugins {
    java
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.8.3"
}

group = "ru.kuramshin-dev"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
    maven {
        url = uri("https://repo.spring.io/snapshot")
    }
    maven {
        name = "Central Portal Snapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

extra["springAiVersion"] = "1.0.1"
extra["vaadinVersion"] = "24.8.3"

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation ("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Vaadin UI
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // Spring AI
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation ("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
        mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
