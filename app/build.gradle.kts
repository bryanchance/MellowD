plugins {
    antlr
    application
    `maven-publish`
    jacoco
}

group = "org.mellowd"
version = "3.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr(libs.antlr4)
    implementation(libs.antlr4.runtime)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.gson)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

application {
    mainClass = "org.mellowd.io.Compiler"
}

tasks.generateGrammarSource {
    outputDirectory = file("$outputDirectory/org/mellowd/compiler")
    arguments = arguments + listOf(
        "-package", "org.mellowd.compiler",
        "-visitor",
        "-no-listener"
    )

    inputs.files(source.files)
}

tasks.compileJava {
    options.compilerArgs = options.compilerArgs + listOf(
        // AudioSynthesizer for soft synth
        "--add-exports", "java.desktop/com.sun.media.sound=ALL-UNNAMED"
    )
}

tasks.processResources {
    filesMatching("metadata.properties") {
        expand("projectVersion" to project.version)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

val doccoMode = "linear" /* linear, parallel, classic */
val pathToLanguages = "languages.json"

fun toRelFile(file: File): String {
    var relPath: String = file.absolutePath.replace(rootDir.absolutePath, "");
    if (relPath.startsWith(File.separatorChar)) {
        relPath = relPath.substring(1);
    }
    return relPath;
}

interface InjectedServices {
    @get:Inject val fs: FileSystemOperations
}
tasks.register<Exec>("docco") {
    val outDir = toRelFile(file("${layout.buildDirectory.get().asFile.absolutePath}/docco"))
    val sourceFiles =
        sourceSets.main.get().allSource.plus(sourceSets.test.get().allSource).plus(sourceSets.test.get().resources)
            // Exclude generated source files as they are not documented
            .filter { !it.path.contains("generated-src") && !it.path.endsWith(".properties") }
            .map { toRelFile(file(it)) }
            .toTypedArray()
    executable = "npx"
    workingDir = rootDir
    setArgs(
        listOf(
            "--yes", "docco",
            "-o", outDir,
            // Always compile md files as linear because they look out of place
            // when squeezed on the side with a blank window beside it
            "-l", doccoMode,
            "-L", toRelFile(rootDir.resolve(pathToLanguages)),
            "-t", toRelFile(rootDir.resolve("site/docco.jst")),
            "-c", toRelFile(rootDir.resolve("site/docco.css")),
            *sourceFiles,
            toRelFile(rootDir.resolve("index.md")),
            toRelFile(rootDir.resolve("langRef.md")),
        )
    )

    val publicDir = toRelFile(file("${outDir}/public"))
    val services = project.objects.newInstance<InjectedServices>();
    doLast {
        services.fs.copy {
            from("site/public")
            into(publicDir)
        }
    }
}

tasks.test {
    reports.html.outputLocation = file("${layout.buildDirectory.get().asFile.absolutePath}/docco/tests")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "compiler"
            from(components["java"])
        }
    }
}