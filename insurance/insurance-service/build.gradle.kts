dependencies {
    implementation(project(":insurance:insurance-model"))
    implementation(project(":insurance:insurance-domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-artemis")
    implementation("org.web3j:core:4.12.3")
}
