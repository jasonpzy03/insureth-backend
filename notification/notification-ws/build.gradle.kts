plugins {
    id("org.springframework.boot")
    id("org.openapi.generator")
}

dependencies {
    implementation(project(":notification:notification-service"))
    implementation(project(":notification:notification-model"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/src/main/resources/openapi/notification-api.yml")
    outputDir.set("${projectDir}/build/generated")
    apiPackage.set("com.insureth.notification.ws.api")
    modelPackage.set("com.insureth.notification.model.dto")

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
