/*
 * Copyright 2013. Muhammad Ashraf
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
package org.github.mansur.scalastyle

import org.gradle.api.GradleException
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

    ScalaStyleTask() {
        super()
        setDescription("Scalastyle examines your Scala code and indicates potential problems with it.")
    }

    /**
     * Whether or not this task will ignore failures and continue running the build.
     */
    boolean ignoreFailures

    @TaskAction
    def scalastyle() {
        println("in scalastyle task for " + name)
        extractAndValidateProperties()
        try {
            def configuration = ScalastyleConfiguration.readFromXml(configLocation)
            List<FileSpec> files = Directory.getFilesAsJava(scala.Option.apply(null), getSource().files.asList())
            def messages = new ScalastyleChecker().checkFilesAsJava(configuration, files)
            def outputResult = new TextOutput(verbose, quiet).output(messages)

            getLogger().debug("Saving to outputFile={}", project.file(outputFile).getCanonicalPath());
            XmlOutput.save(outputFile, outputEncoding, messages)

            def violations = outputResult.errors() + ((failOnWarning) ? outputResult.warnings() : 0)

            processViolations(violations)
        } catch (Exception e) {
            throw new GradleException("Scala check error", e)
        }
    }

    private void processViolations(int violations) {
        if (violations > 0) {
            if (ignoreFailures) {
                throw new GradleException("You have $violations Scalastyle violation(s).")
            } else {
                project.getLogger().warn("Scalastyle:check violations detected but failOnViolation set to " + failOnViolation)
            }
        } else {
            project.getLogger().debug("Scalastyle:check no violations found")
        }
    }

    private void extractAndValidateProperties() {

        if (!new File(configLocation).exists()) {
            throw new Exception("configLocation " + configLocation + " does not exist")
        }

        if (buildDirectory == null) {
            buildDirectory = project.buildDir
        }

        if (outputFile == null) {
            outputFile = buildDirectory.absolutePath + "/scala_style_result.xml"
        }
    }
}
