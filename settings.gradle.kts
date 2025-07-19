rootProject.name = "mople"

pluginManagement {
    val springBootVersion:           String by settings
    val dependencyManagementVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when(requested.id.id) {
                "org.springframework.boot"            -> useVersion(springBootVersion)
                "io.spring.dependency-management"     -> useVersion(dependencyManagementVersion)
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.toml"))
        }
    }
}