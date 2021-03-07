import org.gradle.internal.os.OperatingSystem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    application
    id("org.panteleyev.jpackageplugin") version "1.1.1"
}

repositories {
    mavenCentral()
}

val javaFxVersion = "16"
val controlsFxVersion = "11.1.0"
val javaFxHelpersVersion = "1.7.0"
val testNgVersion = "7.3.0"

val platform = when {
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isMacOsX -> "mac"
    OperatingSystem.current().isLinux -> "linux"
    else -> throw GradleException("Unsupported build platform")
}

val javaFxModules = listOf(
    "javafx-base", "javafx-controls", "javafx-graphics", "javafx-media", "javafx-swing"
)

val file: String? by project

dependencies {
    javaFxModules.forEach {
        implementation("org.openjfx:$it:$javaFxVersion:$platform")
    }
    implementation("org.controlsfx:controlsfx:$controlsFxVersion") {
        exclude(group = "org.openjfx")
    }
    implementation("org.panteleyev:java-fx-helpers:$javaFxHelpersVersion") {
        exclude(group = "org.openjfx")
    }
    testImplementation("org.testng:testng:$testNgVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("password.manager")
    mainClass.set("org.panteleyev.pwdmanager.PasswordManagerApplication")
    applicationDefaultJvmArgs = listOf(
        "-Dfile.encoding=UTF-8",
        "--enable-preview",
        "-Dpassword.file=${file ?: ""}"
    )
}

tasks.compileJava {
    options.compilerArgs.add("--enable-preview")
}

tasks.compileTestJava {
    options.compilerArgs.add("--enable-preview")
}

tasks.processResources {
    filesMatching("**/buildInfo.properties") {
        expand(mapOf(
            "version" to project.version.toString(),
            "timestamp" to DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now())
        ))
    }
}

tasks.test {
    useTestNG()
    jvmArgs = listOf("--enable-preview")
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jmods")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jmods")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    appName = "Password Manager"
    vendor = "panteleyev.org"
    module = "${application.mainModule.get()}/${application.mainClass.get()}"
    modulePaths = listOf("$buildDir/jmods")
    destination = "$buildDir/dist"
    javaOptions = listOf(
        "-Dfile.encoding=UTF-8",
        "--enable-preview"
    )

    mac {
        icon = "icons/icons.icns"
    }

    windows {
        icon = "icons/icons.ico"
        winMenu = true
        winDirChooser = true
        winUpgradeUuid = "4a8438d2-f56f-4a5a-bfbe-5cf74ea70685"
        winMenuGroup = "panteleyev.org"
    }
}
