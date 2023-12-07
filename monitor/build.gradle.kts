plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    id("maven-publish")
    id("signing")
}

android {
    namespace = "github.leavesczy.monitor"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        consumerProguardFiles.add(File("consumer-rules.pro"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += setOf(
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.google.gson)
    compileOnly(libs.squareup.okHttp)
}

val signingKeyId = properties["signing.keyId"]?.toString()
val signingPassword = properties["signing.password"]?.toString()
val signingSecretKeyRingFile = properties["signing.secretKeyRingFile"]?.toString()
val mavenCentralUserName = properties["mavenCentral.username"]?.toString()
val mavenCentralEmail = properties["mavenCentral.email"]?.toString()
val mavenCentralPassword = properties["mavenCentral.password"]?.toString()

if (signingKeyId != null
    && signingPassword != null
    && signingSecretKeyRingFile != null
    && mavenCentralUserName != null
    && mavenCentralEmail != null
    && mavenCentralPassword != null
) {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "io.github.leavesczy"
                artifactId = "monitor"
                version = libs.versions.monitor.publishing.get()
                afterEvaluate {
                    from(components["release"])
                }
                pom {
                    name = "Monitor"
                    description = "An Http inspector for OkHttp & Retrofit"
                    url = "https://github.com/leavesCZY/Monitor"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                            distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "leavesCZY"
                            name = "leavesCZY"
                            email = mavenCentralEmail
                        }
                    }
                    scm {
                        url = "https://github.com/leavesCZY/Monitor"
                        connection = "https://github.com/leavesCZY/Monitor"
                        developerConnection = "https://github.com/leavesCZY/Monitor"
                    }
                }
            }
        }
        repositories {
            maven {
                setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = mavenCentralUserName
                    password = mavenCentralPassword
                }
            }
        }
    }
    signing {
        sign(publishing.publications["release"])
    }
}