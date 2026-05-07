plugins {
    id("org.springframework.boot")
    id("org.openapi.generator")
}

dependencies {
    implementation(project(":auth:auth-service"))
    implementation(project(":auth:auth-model"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/../auth-ws/src/main/resources/openapi/cp-users-api.yml")
    outputDir.set("${projectDir}/build/generated")
    apiPackage.set("com.insureth.auth.ws.api")
    modelPackage.set("com.insureth.auth.model.dto")

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
            "skipDefaultInterface" to "true"
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
