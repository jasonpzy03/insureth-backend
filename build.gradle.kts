import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
	java
	id("org.springframework.boot") version "4.0.3" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
	id("io.freefair.lombok") version "8.6" apply false
	id("org.openapi.generator") version "7.10.0" apply false
}

extra["springCloudVersion"] = "2025.1.0"

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "io.freefair.lombok")

    group = "com.insureth"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-batch-test")
        testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
        testImplementation("org.springframework.boot:spring-boot-starter-security-test")
        testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${rootProject.extra["springCloudVersion"]}")
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    afterEvaluate {
        val controllerChild = childProjects.values.find { it.name.endsWith("-ws") }
        val modelChild = childProjects.values.find { it.name.endsWith("-model") }
        if (controllerChild != null && modelChild != null) {
            tasks.register("bootRun") {
                dependsOn("${controllerChild.path}:bootRun")
                dependsOn("${controllerChild.path}:openApiGenerate")
                dependsOn("${modelChild.path}:openApiGenerate")
                description = "Starts the ${project.name} microservice (all layers compiled automatically)"
            }
            tasks.register("buildAll") {
                dependsOn(childProjects.values.map { "${it.path}:build" })
                description = "Builds all ${project.name} layers in dependency order"
            }
        }
    }
}
