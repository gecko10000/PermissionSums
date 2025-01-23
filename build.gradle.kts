plugins {
    id("java")
    id("de.eldoria.plugin-yml.bukkit") version "0.6.0"
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("res")
        }
    }
}


group = "gecko10000.permissionsums"
val versionString = "1.1"
version = versionString

bukkit {
    name = "PermissionSums"
    main = "$group.$name"
    version = versionString
    author = "gecko10000"
    apiVersion = "1.13" // Don't care to test
    depend = listOf("LuckPerms")
    softDepend = listOf("PlaceholderAPI")
    commands {
        register("permissionsums") {
            description = "Reload configs or check a sum"
            aliases = listOf("permsums")
            permission = "permissionsums.command"
        }
    }
    permissions {
        register("permissionsums.command")
        register("permissionsums.reload")
        register("permissionsums.check")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register("update") {
    dependsOn(tasks.build)
    doLast {
        exec {
            workingDir(".")
            commandLine("../../dot/local/bin/update.sh")
        }
    }
}
