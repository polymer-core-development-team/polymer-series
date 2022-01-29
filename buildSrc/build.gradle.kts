plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/") }
}

dependencies {
    implementation("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
        isChanging = true
    }
    implementation(gradleApi())
}