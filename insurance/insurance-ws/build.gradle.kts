import org.gradle.kotlin.dsl.openApiGenerate

plugins {
    id("org.springframework.boot")
    id("org.openapi.generator")
}

dependencies {
    implementation(project(":insurance:insurance-service"))
    implementation(project(":insurance:insurance-model"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/../insurance-ws/src/main/resources/openapi/flight-insurance-api.yml")
    outputDir.set("${projectDir}/build/generated")
    apiPackage.set("com.insureth.insurance.ws.api")
    modelPackage.set("com.insureth.insurance.model.dto")

    globalProperties.set(
        mapOf(
            "apis" to "",
            "models" to "false",
        )
    )

    configOptions.set(
        mapOf(
            "dateLibrary" to "java8",
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useJakartaEe" to "true",
            "generateConstructorWithAllArgs" to "true",
            "skipDefaultInterface" to "true",
            "useTags" to "true"
        )
    )
}

sourceSets {
    main {
        java {
            srcDir("${projectDir}/build/generated/src/main/java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
