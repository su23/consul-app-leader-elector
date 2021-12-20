import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.4.32"
	kotlin("plugin.serialization") version "1.4.32"
}

group = "com.github.su23"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.ecwid.consul:consul-api:1.4.5")
	// implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
	implementation("io.github.microutils:kotlin-logging:1.12.0")
	implementation("org.slf4j:slf4j-simple:1.7.29")
	implementation("commons-logging:commons-logging:1.2")

	implementation("org.springframework.boot:spring-boot-autoconfigure:2.6.1")
	// implementation("org.springframework.boot:spring-boot:2.6.1")
	// implementation("org.springframework:spring-context:5.3.13")
	implementation("org.springframework.cloud:spring-cloud-commons:3.1.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
