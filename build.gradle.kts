import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

plugins {
    idea
    kotlin("jvm") version Versions.KOTLIN
    id("net.minecrell.plugin-yml.bukkit") version Versions.PLUGIN_YML
    id("io.gitlab.arturbosch.detekt") version Versions.DETEKT
}

detekt {
    toolVersion = "1.21.0"
    config = files("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
val monunLibraries = mutableListOf<String>()
fun DependencyHandlerScope.monunLibrary(name: String, version: String) {
    compileOnly("io.github.monun:$name-api:$version")
//    monunLibraries += "io.github.monun:$name-core:$version"
    library("io.github.monun:$name-core:$version")
}

/*
**************************************************
 */

bukkit.apiVersion = "1.19.2"

group = "io.github.highright1234"
version = "0.0.1"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    monunLibrary("tap", Versions.TAP)
    monunLibrary("invfx", Versions.INVFX)
    monunLibrary("kommand", Versions.KOMMAND)
    bukkitLibrary("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:${Versions.MC_COROUTINE}")
    bukkitLibrary("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:${Versions.MC_COROUTINE}")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINE}")
    compileOnly("io.papermc.paper:paper-api:${project.bukkit.apiVersion}-R0.1-SNAPSHOT")
    library(kotlin("stdlib-jdk8"))
    library(kotlin("reflect"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.12.7")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    testImplementation("org.slf4j:slf4j-simple:2.0.0")
}

/*
**************************************************
 */

val directoryName = project.name.replace("-", "")
val pluginName = project.name.split("-").joinToString(separator = "") { it.capitalizeAsciiOnly() }
bukkit {
    apiVersion = apiVersion!!
        .split(".")
        .slice(0..1)
        .joinToString(separator = ".")
    name = pluginName
    main = "${project.group}.$directoryName.$pluginName"
    website = "http://www.github.com/highright1234/${project.name}"
    author = "HighRight"
    (libraries ?: listOf()).plus(monunLibraries).also {
        libraries = it
    }
}

tasks.register<Jar>("pluginsUpdate") {
    archiveBaseName.set(pluginName)
    from(sourceSets["main"].output)
    val serverDir = File(rootDir, ".server")
    val plugins = File(serverDir, "plugins")
    doLast {
        // 내 마크 서버 환경 불러오기
        val serverFolder = File("E:\\.server\\")
        if (!serverDir.exists() && serverFolder.exists()) {
            serverDir.mkdir()
            copy {
                from(serverFolder)
                include("**/**")
                into(serverDir)
            }
        }
        // 플러그인 적용
        if (!plugins.exists()) return@doLast
        copy {
            from(archiveFile)
            if (File(plugins, archiveFileName.get()).exists()) {
                File(plugins, archiveFileName.get()).delete()
            }
            into(plugins)
        }
        // auto-reloader
        val updateFolder = File(plugins, "update")
        if (!updateFolder.exists()) return@doLast
        File(updateFolder, "RELOAD").delete()
    }
}

tasks.named("build") { finalizedBy("pluginsUpdate") }

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}
