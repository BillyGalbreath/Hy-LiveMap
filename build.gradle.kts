plugins {
  `java-library`
}

group = "net.pl3x.livemap"
version = "1.0.0-SNAPSHOT"
description = "LiveMap for Hytale servers"

repositories {
  mavenCentral()
}

dependencies {
  // todo - replace with maven repo when available
  implementation(files(System.getenv("HYTALE_SERVER")))
}

tasks {
  processResources {
    filteringCharset = Charsets.UTF_8.name()

    // work around IDEA-296490
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    with(copySpec {
      include("manifest.json", "web/index.html")
      from("src/main/resources") {
        expand(
          "artifact" to "${rootProject.name}",
          "group" to "${rootProject.group}",
          "version" to "${rootProject.version}",
          "description" to "${rootProject.description}",
        )
      }
    })
  }
}
