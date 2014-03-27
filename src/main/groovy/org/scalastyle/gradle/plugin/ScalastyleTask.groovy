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

import org.gradle.api.GradleException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

import org.scalastyle.Directory
import org.scalastyle.FileSpec
import org.scalastyle.ScalastyleChecker
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.TextOutput
import org.scalastyle.XmlOutput

/**
 * Runs Scalastyle against some source files.
 */
class ScalaStyleTask extends SourceTask implements VerificationTask {
    String outputFile
    String outputEncoding = "UTF-8"

    @InputFile
    File configFile

    /**
     * Whether or not this task will ignore failures and continue running the build.
     */
    boolean ignoreFailures

    ScalaStyleTask() {
        super()
        setDescription("Scalastyle examines your Scala code and indicates potential problems with it.")
    }

    @TaskAction
    def scalastyle() {
        extractAndValidateProperties()
        def outputResult
        try {
            def configuration = ScalastyleConfiguration.readFromXml(getConfigFile().absolutePath)
            List<FileSpec> files = Directory.getFilesAsJava(scala.Option.apply(null), getSource().files.asList())
            def messages = new ScalastyleChecker().checkFilesAsJava(configuration, files)
            outputResult = new TextOutput(false, false).output(messages)

            //XmlOutput.save(outputFile, outputEncoding, messages)
        } catch (Exception e) {
            throw new GradleException("Scala check error", e)
        }

        if (outputResult.errors() > 0 && !getIgnoreFailures()) {
            throw new GradleException("Scalastyle rule violations were found.")
        }
    }

    private void extractAndValidateProperties() {

        if (!getConfigFile().exists()) {
            throw new GradleException("configFile $configFile does not exist")
        }

        /*
        if (buildDirectory == null) {
            buildDirectory = project.buildDir
        }

        if (outputFile == null) {
            outputFile = buildDirectory.absolutePath + "/scala_style_result.xml"
        }
        */
    }
}
