/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.scalastyle.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.api.tasks.ScalaRuntime
import org.gradle.api.tasks.SourceSet

/**
 *
 */
class ScalastylePlugin implements Plugin<Project> {

    static final String DEFAULT_CONFIG_FILE = "config/scalastyle/scalastyle_config.xml";
    private static final String DEFAULT_VERSION = "0.4.0"

    private ScalastyleExtension extension
    private Project project

    Set<SourceSet> testSourceSets
    Set<SourceSet> mainSourceSets

    ScalaRuntime scalaRuntime

    String toolName = "ScalaStyle"

    void apply(Project project) {

        this.project = project

        createConfigurations()
        extension = createExtension()
        configureExtensionRule()
        configureTaskRule()
        configureSourceSetRule()
        configureCheckTask()
    }

    protected String getTaskBaseName() {
        return toolName.toLowerCase()
    }

    protected String getConfigurationName() {
        return toolName.toLowerCase()
    }

    protected String getReportName() {
        return toolName.toLowerCase()
    }

    protected Class<ScalastyleTask> getTaskType() {
        return ScalastyleTask
    }

    protected Class<ScalaBasePlugin> getBasePlugin() {
        return ScalaBasePlugin
    }

    protected ScalastyleExtension createExtension() {
        extension = project.extensions.create(toolName.toLowerCase(), ScalastyleExtension)

        extension.with {
            toolVersion = DEFAULT_VERSION
            configFile = project.file(DEFAULT_CONFIG_FILE)
        }

        return extension
    }

    private void configureExtensionRule() {
        extension.conventionMapping.with {
            sourceSets = { [] }
            reportsDir = { project.extensions.getByType(ReportingExtension).file(reportName) }
        }

        project.plugins.withType(ScalaBasePlugin.class) {
            extension.conventionMapping.sourceSets = { project.sourceSets }
        }
    }

    private void configureTaskRule() {
        project.tasks.withType(ScalastyleTask.class) { ScalastyleTask task ->
            def prunedName = (task.name - taskBaseName ?: task.name)
            prunedName = prunedName[0].toLowerCase() + prunedName.substring(1)
            configureTaskDefaults(task, prunedName)
        }
    }

    protected void configureTaskDefaults(ScalastyleTask task, String baseName) {
        def conf = project.configurations['scalastyle']
        conf.incoming.beforeResolve {
            if (conf.dependencies.empty) {
                conf.dependencies.add(project.dependencies.create("org.scalastyle:scalastyle_2.10:$extension.toolVersion"))
            }
        }

        task.conventionMapping.with {
            configFile = { extension.configFile }
            ignoreFailures = { extension.ignoreFailures }
        }

        /*
        task.reports.xml.conventionMapping.with {
            enabled = { true }
            destination = { new File(extension.reportsDir, "${baseName}.xml") }
        }
        */
    }

    protected void createConfigurations() {
        project.configurations.create(toolName.toLowerCase()).with {
            visible = false
            transitive = true
            description = "The ${toolName} libraries to be used for this project."
        }
    }

    private void configureSourceSetRule() {
        project.plugins.withType(basePlugin) {
            project.sourceSets.all { SourceSet sourceSet ->
                ScalastyleTask task = project.tasks.create(sourceSet.getTaskName(taskBaseName, null), taskType)
                configureForSourceSet(sourceSet, task)
            }
        }
    }

    protected void configureForSourceSet(SourceSet sourceSet, ScalastyleTask task) {
        task.with {
            description = "Run $toolName analysis for ${sourceSet.name} classes"
        }
        task.setSource(sourceSet.allScala)
    }

    private void configureCheckTask() {
        project.plugins.withType(basePlugin) {
           project.tasks['check'].dependsOn { extension.sourceSets.collect { it.getTaskName(taskBaseName, null) }}
        }
    }
}
