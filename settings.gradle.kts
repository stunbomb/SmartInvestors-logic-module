pluginManagement {
    repositories {
        google()                  // 🔹 Needed to resolve Android and Compose plugins
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartInvestorsLogic"
