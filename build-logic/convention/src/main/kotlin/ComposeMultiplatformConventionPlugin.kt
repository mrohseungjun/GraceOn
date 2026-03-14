import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
                val kotlin = extensions.getByType<KotlinMultiplatformExtension>()

                kotlin.sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("compose-runtime").get())
                    implementation(libs.findLibrary("compose-foundation").get())
                    implementation(libs.findLibrary("compose-material3").get())
                    implementation(libs.findLibrary("compose-ui").get())
                    implementation(libs.findLibrary("compose-components-resources").get())
                    implementation(libs.findLibrary("compose-components-ui-tooling-preview").get())
                    implementation(libs.findLibrary("compose-material-icons-extended").get())
                }

                dependencies {
                    add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
                }
            }
        }
    }
}
