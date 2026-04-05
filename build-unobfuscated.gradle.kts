plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
}

val minecraft = stonecutter.current.version
val loader = stonecutter.current.project.substringAfterLast('-')

version = "${mod.version}+$minecraft"
group = mod.group
base { archivesName.set("${mod.id}-$loader") }

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    implementation("curse.maven:libanalog-1159992:${mod.dep("libanalog")}")
    if (loader == "fabric") {
        implementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
        implementation("net.fabricmc.fabric-api:fabric-api:${mod.dep("fabric_api")}")
    }
}

tasks.jar {
    archiveClassifier = ""
}

loom {
    decompilers {
        get("vineflower").apply {
            options.put("mark-corresponding-synthetics", "1")
        }
    }
}

val requiredJava = when {
    sc.current.parsed >= "26.1-pre-1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks.processResources {
    properties(
        listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to mod.prop("mc_dep_fabric")
    )
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.jar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn(tasks.jar)
}

publishMods {
    file = tasks.jar.get().archiveFile
    displayName = "${mod.name} ${mod.version} for $minecraft"
    version = mod.version
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.environmentVariable("MODRINTH_TOKEN")
        .getOrNull() == null || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.add(minecraft)
        requires("libanalog")
        if (loader == "fabric") {
            requires("fabric-api")
        }
    }

    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.add(minecraft)
        requires("libanalog")
        if (loader == "fabric") {
            requires("fabric-api")
        }
    }
}

publishing {
    repositories {
        maven("...") {
            name = "..."
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${mod.id}"
            artifactId = mod.version
            version = "${project.version}-$loader"

            from(components["java"])
        }
    }
}
