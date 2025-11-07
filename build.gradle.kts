import org.jreleaser.model.Active

plugins {
    id("java-library")
    id("maven-publish")
    id("org.jreleaser") version "1.21.0"
    id("signing")
}

group = "cn.srclink"
version = "0.0.3"

repositories {
    maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.name
            from(components["java"])
            pom {
                name = "Service API"
                description =
                    "srclink Service API, support load outer class over customized ServiceProvider as Java SPI."
                url = "https://gitee.com/srclink/service-api"
                licenses {
                    license {
                        name = "GNU Lesser General Public License v3.0"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "srclink"
                        name = "srclink"
                        email = "admin@srclink.cn"
                    }
                }
                scm {
                    connection = "scm:git:https://gitee.com/srclink/service-api.git"
                    developerConnection = "scm:git:git://git@gitee.com:srclink/service-api.git"
                    url = "https://gitee.com/srclink/service-api"
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

jreleaser {
    signing {
        val pub:String = System.getProperty("signing.publicKeyFile") ?: "public.pgp"
        val pri:String = System.getProperty("signing.secretKeyRingFile") ?: "private.pgp"
        active = Active.ALWAYS
        armored = true
        publicKey = file(pub).readText()
        secretKey = file(pri).readText()
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    val user:String = System.getProperty("sonatype.username") ?: ""
                    val pass:String = System.getProperty("sonatype.password") ?: ""

                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    username = user
                    password = pass

                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}