import net.minecraftforge.gradle.common.util.RunConfig
import java.util.Date
import java.text.SimpleDateFormat

plugins {
    kotlin("jvm") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
}

apply {
    plugin("net.minecraftforge.gradle")
}



version = "1.16.5-b1-1"
group = "com.polymerteam.polymerseries"
configure<BasePluginExtension> {
    archivesName.set("polymer")
}
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

println(
    "Java: ${System.getProperty("java.version")} " +
            "JVM:  ${System.getProperty("java.vm.version")}(${System.getProperty("java.vendor")}) " +
            "Arch:  ${System.getProperty("os.arch")}"
)

val modules = arrayOf(
    "core",
    "machine",
    "components",
    "hinge",
    "schematic",
    "scripts",
    "gui",
)

sourceSets {
    modules.forEach {
        register(it) { resources.srcDir("src/generated/resources/$it") }
    }
}
minecraft {
    mappings("official", "1.16.5")
    accessTransformer(file("src/core/resources/META-INF/polymer_at.cfg"))
    runs {
        val runConfig = Action<RunConfig> {
            workingDirectory = project.file("run").canonicalPath
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                modules.forEach {
                    create("polymer-$it") { source(sourceSets[it]) }
                }
            }
        }
        create("client", runConfig)
        create("server", runConfig)
        create("data") {
            runConfig(this)
            args(
                "--all", "--output", file("src/generated/resources/"),
                *modules.flatMap {
                    listOf("--mod", "polymer-$it",
                        "--existing", file("src/$it/resources/")
                    )
                }.toTypedArray()
            )

        }
//        modules.forEach {
//            create("${it}Data") {
//                runConfig(this)
//                environment("target", "fmluserdevdata")
//                // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
//                args(
//                    "--all", "--output", file("src/$it/generated/resources/"),
//                    "--mod", "polymer-$it",
//                    "--existing", file("src/$it/main/resources/")
//                )
//            }
//        }
    }

}

repositories {
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }

    flatDir {
        dir("libs")
    }
}

configurations {
    val implementation = getByName("implementation")
    val kotlinImplementation = maybeCreate("kotlinImplementation")
    modules.forEach {
        getByName("${it}Implementation").extendsFrom(implementation)
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.16.5-36.2.20")
    // Use the latest version of KotlinForForge
    implementation("thedarkcolour:kotlinforforge:1.14.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

    implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-dependencies:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.6.10")


}

modules.forEach {
    val capitalizeName = it.capitalize() //首字母大写
    tasks.register<Jar>("${it}Jar") {
        archiveClassifier.set(it)
        group = "polymer build"
        manifest {
            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
            attributes(
                mapOf(
                    "Specification-Title" to "Polymer $capitalizeName",
                    "Specification-Vendor" to "WarmthDawn",
                    "Specification-Version" to "1", // We are version 1 of ourselves
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to "${project.version}",
                    "Implementation-Vendor" to "WarmthDawn",
                    "Implementation-Timestamp" to time
                )
            )
        }
        dependsOn("compile${capitalizeName}Java", "compile${capitalizeName}Kotlin")
        from(sourceSets[it].output)
        finalizedBy("reobf${capitalizeName}Jar")
    }

    tasks.named<Copy>("process${capitalizeName}Resources") {
        from(sourceSets[it].resources.srcDirs) {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            include("META-INF/mods.toml")
            doFirst {
                filter<org.apache.tools.ant.filters.ReplaceTokens>(
                    "tokens" to mapOf(
                        "version" to "${project.version}",
                        "mc_version" to "1.16.5"
                    )
                )
            }
        }
    }
}

reobf {
    modules.forEach {
        create("${it}Jar") {
            classpath.from(sourceSets[it].compileClasspath)
        }
    }
}
