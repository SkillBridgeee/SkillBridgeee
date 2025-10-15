// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}

// Force JaCoCo version globally to support Java 21
allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jacoco:org.jacoco.core:0.8.11")
            force("org.jacoco:org.jacoco.agent:0.8.11") 
            force("org.jacoco:org.jacoco.report:0.8.11")
        }
    }
}