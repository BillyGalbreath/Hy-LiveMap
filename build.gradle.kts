plugins {
    `java-library`
}

group = "net.pl3x"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // todo - replace with maven repo when available
    implementation(files(System.getenv("HYTALE_SERVER")))
}
