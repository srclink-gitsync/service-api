plugins {
    id("java")
}

group = "cn.srclink"
version = "0.0.2"

repositories {
    maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}