import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import github.leavesczy.monitor.monitorVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class AndroidPublishConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(receiver = target) {
            apply(plugin = "com.vanniktech.maven.publish")
            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral()
                // JitPack runs publishToMavenLocal without GPG keys.
                // Signing is only needed for Maven Central releases.
                if (System.getenv("JITPACK") != "true") {
                    signAllPublications()
                }
                configure(platform = AndroidSingleVariantLibrary())
                coordinates(
                    groupId = "io.github.leavesczy",
                    version = monitorVersion()
                )
                pom {
                    name.set("Monitor")
                    description.set("An Http inspector for OkHttp & Retrofit")
                    inceptionYear.set("2020")
                    url.set("https://github.com/leavesczy/Monitor")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("leavesczy")
                            name.set("leavesczy")
                            url.set("https://github.com/leavesczy")
                        }
                    }
                    scm {
                        url.set("https://github.com/leavesczy/Monitor")
                        connection.set("scm:git:git://github.com/leavesczy/Monitor.git")
                        developerConnection.set("scm:git:ssh://git@github.com/leavesczy/Monitor.git")
                    }
                }
            }
        }
    }
}