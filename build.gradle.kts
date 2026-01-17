plugins {
  `java-library`
  alias(libs.plugins.fix.javadoc)
  alias(libs.plugins.shadow)
}

group = "net.pl3x.livemap"
version = "${libs.versions.livemap.get()}"
description = "LiveMap for Hytale servers"

java {
  withJavadocJar()
  withSourcesJar()
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  // todo - replace with maven repo when available
  compileOnly(files(System.getenv("HYTALE_SERVER")))

  implementation(libs.undertow)

  compileOnly(libs.annotations)
  implementation(libs.simpleYaml)
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")

    exclude(
      "META-INF/LICENSE",
      "META-INF/LICENSE.txt",
      "META-INF/maven/**/*",
      "META-INF/jandex.idx",
      "schema/**/*"
    )

    arrayOf(
      "io.smallrye",
      "io.undertow",
      "org.jboss",
      "org.simpleyaml",
      "org.yaml.snakeyaml",
      "org.wildfly",
      "org.xnio",
    ).forEach { it -> relocate(it, "libs.$it") }
  }

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

  javadoc {
    val name = rootProject.name.replaceFirstChar { it.uppercase() }
    val stdopts = options as StandardJavadocDocletOptions
    stdopts.encoding = Charsets.UTF_8.name()
    stdopts.overview = "src/main/javadoc/overview.html"
    stdopts.use()
    stdopts.isDocFilesSubDirs = true
    stdopts.windowTitle = "$name $version API Documentation"
    stdopts.docTitle = "<h1>$name $version API</h1>"
    stdopts.header = """<img src="https://raw.githubusercontent.com/billygalbreath/livemap/master/core/src/main/resources/web/images/livemap.png" style="height:100%">"""
    stdopts.bottom = "Copyright © 2020-2026 William Blake Galbreath"
    stdopts.linkSource(true)
    stdopts.addBooleanOption("html5", true)
    stdopts.links(
      "https://javadoc.io/doc/io.undertow/undertow-core/${libs.versions.undertow.get()}/",
      //"https://javadoc.io/doc/com.google.code.gson/gson/${libs.versions.gson.get()}/",
    )
  }

  withType<com.jeff_media.fixjavadoc.FixJavadoc> {
    configureEach {
      newLineOnMethodParameters.set(false)
      keepOriginal.set(false)
    }
  }
}
