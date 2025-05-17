import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.tacoclient.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tacoclient.launcher"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    externalNativeBuild {
        ndkBuild {
            path ("src/main/cpp/Android.mk")
        }
    }
    ndkVersion = "27.1.12297006"
}

val prepareLauncherDex by tasks.registering {
    group = "build"
    description = "build launcher jar, dex it, rename & copy into assets, then update client"
    dependsOn(":launcher:createFullJarRelease")
    val launcherJar = project(":launcher").layout.buildDirectory.file(
        "intermediates/full_jar/release/createFullJarRelease/full.jar"
    )
    val assetsDir = layout.projectDirectory.dir("src/main/assets")
    inputs.file(launcherJar)
    outputs.file(assetsDir.file("launcher.dex"))

    doLast {
        val jarFile = launcherJar.get().asFile
        val outDir = assetsDir.asFile
        val sdkDir = android.sdkDirectory.absolutePath
        val btVersion = android.buildToolsVersion
        val d8Path = "$sdkDir${File.separator}build-tools${File.separator}$btVersion${File.separator}d8"
        val execOperations = project.serviceOf<ExecOperations>()

        execOperations.exec {
            commandLine(d8Path, jarFile.absolutePath, "--output", outDir.absolutePath)
        }

        val classesDex = outDir.resolve("classes.dex")
        val launcherDex = outDir.resolve("launcher.dex")
        if (launcherDex.exists()) launcherDex.delete()
        classesDex.renameTo(launcherDex)
    }
}

tasks.named("preBuild") {
    dependsOn(prepareLauncherDex)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.appcompat)
}