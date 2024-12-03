dependencies {
    implementation(project(":azure-intellij-plugin-lib"))
    implementation(project(":azure-intellij-plugin-lib-java"))
    implementation("com.microsoft.azure:azure-toolkit-common-lib")
    implementation("com.microsoft.azure:azure-toolkit-ide-common-lib")

    intellijPlatform {
        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.idea.maven")
        bundledPlugin("org.jetbrains.idea.maven.model")
        bundledPlugin("com.intellij.gradle")
    }
}
