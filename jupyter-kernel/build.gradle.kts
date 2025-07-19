import io.github.spencerpark.jupyter.gradle.tasks.InstallKernelTask
import io.github.spencerpark.jupyter.gradle.tasks.ZipKernelTask

plugins {
    application
    `maven-publish`
    id("io.github.spencerpark.jupyter-kernel-installer") version "3.0.0-SNAPSHOT"
}


group = "org.mellowd"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":compiler"))
    implementation(libs.jupyter.jvm.basekernel)
}
configurations.runtimeClasspath.get().exclude(module = "antlr4")

application {
    mainClass = "org.mellowd.jupyter.IMellowD"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Main-Class" to "org.mellowd.jupyter.IMellowD",
            "Class-Path" to configurations.runtimeClasspath.get().joinToString(" ") { "dependencies/${it.name}" },
        )
    }
}

tasks.processResources {
    filesMatching("mellowd-compiler-metadata.properties") {
        expand("projectVersion" to project.version)
    }
}

jupyter {
    kernelName = "mellowd"
    kernelDisplayName = "Mellow D"
    kernelLanguage = "mellowd"
    kernelInterruptMode = "message"

    kernelResources {
        from(files("kernel"))
        from(configurations.runtimeClasspath) {
            into("dependencies")
        }
    }

    kernelParameters {
        string("soundfont", "MELLOWD_SF_PATH")
    }
}

tasks.named<ZipKernelTask>("zipKernel") {
    installers {
        with("python")
    }
}

tasks.named<InstallKernelTask>("installKernel") {
    pythonExecutable = "./playground/venv/bin/python"
    setUseSysPrefixInstallPath(true)
}