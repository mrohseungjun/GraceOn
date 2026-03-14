import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val extension = extensions.findByType(CommonExtension::class.java)
            extension?.apply {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}
