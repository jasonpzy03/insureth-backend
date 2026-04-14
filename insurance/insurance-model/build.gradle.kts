plugins {
    id("org.openapi.generator")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.25")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${projectDir}/../insurance-ws/src/main/resources/openapi/flight-insurance-api.yml")
    outputDir.set("${projectDir}/build/generated")
    apiPackage.set("com.insureth.insurance.ws.api")
    modelPackage.set("com.insureth.insurance.model.dto")

    globalProperties.set(
        mapOf(
            "apis" to "false",
            "models" to ""
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

    typeMappings.set(
        mapOf(
            "Date" to "Date"
        )
    )

    importMappings.set(
        mapOf(
            "Date" to "java.util.Date"
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
