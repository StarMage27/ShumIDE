import com.android.build.gradle.tasks.MergeSourceSetFolders
import com.nishtahir.CargoBuildTask


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.mozilla.rust) apply true
}


val rustDirName = "tree-sitter-bridge"
val libName = "treesitterbridge"
cargo {
    module = "../$rustDirName"
    libname = libName
    targets = listOf("arm64", "x86_64")
    verbose = true
}

val task = tasks.register<Exec>("uniffiBindgen") {
    workingDir = file("${project.rootDir}/$rustDirName")
    commandLine("cargo", "run", "--bin", "uniffi-bindgen",
        "generate", "--library",
        "${project.rootDir}/app/build/rustJniLibs/android/arm64-v8a/libtreesitterbridge.so", //make sure .so name is like this lib<libname>.so
        "--language", "kotlin", "--out-dir",
        //"${project.rootDir}/app/src/main/java/io/github/starmage27/shumide/",
        layout.buildDirectory.dir("generated/kotlin").get().asFile.path
    )
}

project.afterEvaluate {
    tasks.withType(CargoBuildTask::class).forEach { buildTask ->
        tasks.withType(MergeSourceSetFolders::class).configureEach {
            this.inputs.dir(
                layout.buildDirectory.dir("rustJniLibs" + File.separatorChar + buildTask.toolchain!!.folder)
            )
            this.dependsOn(buildTask)
        }
    }
}

tasks.preBuild.configure {
    dependsOn.add(tasks.withType(CargoBuildTask::class.java))
    dependsOn.add(task)
}


android {
    namespace = "io.github.starmage27.shumide"
    compileSdk = 36
    ndkVersion = "29.0.13599879"
    sourceSets {
        getByName("main").java.srcDir("build/generated/kotlin")
    }

    defaultConfig {
        applicationId = "io.github.starmage27.shumide"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.jna) {
        artifact {
            extension = "aar"
            type = "aar"
        }
    }

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

    // Compose navigation
    implementation(libs.androidx.compose.navigation)
    implementation(libs.kotlinx.serialization.json)

    // Kotlin coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Difference
    //implementation(libs.difference)
}