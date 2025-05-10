import org.owasp.dependencycheck.reporting.ReportGenerator.Format

plugins {
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.owasp.dependencycheck") version "8.2.1"
	id("com.google.cloud.tools.jib") version "3.3.1"
    id("java")
    jacoco
}

group = "org.forpdi"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.liquibase:liquibase-core")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")

    implementation("io.jsonwebtoken:jjwt:0.12.3")	
	implementation("commons-io:commons-io:2.17.0")
	implementation("org.apache.commons:commons-email:1.6.0")
	implementation("commons-lang:commons-lang:2.6")
	implementation("commons-codec:commons-codec:1.10")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.777")
	implementation("com.itextpdf:itextpdf:5.5.13.3")
	implementation("org.jfree:jfreechart:1.5.5")
	implementation("com.google.code.gson:gson:2.10.1")
	implementation("com.ibm.icu:icu4j:53.1")
	implementation("org.dom4j:dom4j:2.1.4")
	implementation("com.google.guava:guava:32.0.0-jre")
	implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
    implementation ("org.jsoup:jsoup:1.18.1")
	
    compileOnly("org.springframework.boot:spring-boot-devtools")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("mysql:mysql-connector-java:8.0.28")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
	testImplementation("org.springframework.security:spring-security-test:5.7.12")

	if (System.getProperty ("os.name").equals("Mac OS X") && System.getProperty ("os.arch").equals("aarch64")) {
        testImplementation(group = "io.netty", name = "netty-resolver-dns-native-macos", version = "4.1.92.Final", classifier = "osx-aarch_64")
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            when ("${requested.group}:${requested.name}") {
                "org.jboss.xnio:xnio-api" -> useVersion("3.8.16.Final")
                "ch.qos.logback:logback-core" -> useVersion("1.2.13")
                "com.google.protobuf:protobuf-java" -> useVersion("3.25.5")
                "org.springframework.security:spring-security-core" -> useVersion("5.7.12")
                "org.bouncycastle:bcpkix-jdk15on" -> useTarget("org.bouncycastle:bcpkix-jdk18on:1.78.1")
                "org.springframework:spring-expression" -> useVersion("5.3.39")
            }
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.7")
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
    }
    test {
        java.srcDirs("src/test/java")
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("platfor.jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+UseCompressedClassPointers -XX:+UseZGC -XX:+ZUncommit -XX:+UseDynamicNumberOfGCThreads -XX:-OmitStackTraceInFastThrow -XX:+OptimizeStringConcat -server -ea -Xms8m -XX:MaxRAMPercentage=75 -XX:+HeapDumpOnOutOfMemoryError".split(" "))
    configure<JacocoTaskExtension> {
        excludes = listOf()
    }
    
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = layout.buildDirectory.dir("reports/jacoco/html").get().asFile
    }
}

// OWASP Dependency Check settings
dependencyCheck {
    // let's ignore errors to make builds in Jenkins more stable
    failOnError = false

	// OWASP Dependency Check plugin for Jenkins needs an XML report,
	//   but humans may also need an HTML one
	format = Format.ALL.name

    // set up a quality gate for vulnerabilities with high severity level:
    //   let's consider that a vulnerability has a high severity level if its CVSS score is higher than 7
    //   the build is going to fail if vulnerabilities with high severity level found
    failBuildOnCVSS = 7F

    // specify a list of known issues which contain:
    //   false-positives
    //   confirmed vulnerabilities which are not fixed yet, but we have a ticket for that
    suppressionFile = "dependency-check-known-issues.xml"
}

jib {
    from {
        image = "gcr.io/distroless/java17:nonroot"
    }
    containerizingMode = "packaged"
    container {
        environment = mapOf(
            Pair("JAVA_OPTS", "-server -Dfile.encode=UTF-8 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -XX:+UseZGC -XX:+UseDynamicNumberOfGCThreads -XX:+UseContainerSupport -Xms64m -XX:MaxRAMPercentage=90 -XX:+OptimizeStringConcat -XX:+UseStringDeduplication"),
            Pair("TZ", "America/Sao_Paulo"),
            Pair("LANG", "pt_BR.UTF-8"),
            Pair("LANGUAGE", "pt_BR.UTF-8"),
            Pair("LC_ALL", "pt_BR.UTF-8"),
        )
        ports = listOf("8080","7188")
    }
    to {
        credHelper {
            helper = "ecr-login"
        }
    }
}

jacoco {
    toolVersion = "0.8.10"
	reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}
