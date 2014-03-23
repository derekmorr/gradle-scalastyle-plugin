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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskInstantiationException


/**
 * @author Muhammad Ashraf
 * @since 5/11/13
 */
class ScalaStylePlugin implements Plugin<Project> {

    Set<SourceSet> testSourceSets
    Set<SourceSet> mainSourceSets

    void apply(Project project) {

        // the scala plugin is required
        if (!project.plugins.hasPlugin("scala")) {
            throw new TaskInstantiationException("Scala plugin has to be applied before ScalaStyle plugin")
        }

        mainSourceSets = [project.sourceSets.main] as Set<SourceSet>
        testSourceSets = [project.sourceSets.test] as Set<SourceSet>

        def allScalaSourceDirs = mainSourceSets*.allScala.srcDirs.flatten() // as Set<SourceDirectorySet>;

        for (File f: allScalaSourceDirs) {
            println(f.absolutePath);
        }

        def scalaDirSets = mainSourceSets*.allScala.flatten()
        for (SourceDirectorySet dirSet : scalaDirSets) {
            println(dirSet.name);
            for (File f : dirSet.files) {
                println("    " + f.absolutePath);
            }
        }

        /*
        for (Object o : project.plugins.toArray()) {
            println o.toString()
        }
        */

        project.configurations.create("scalaStyle")
                .setVisible(false)
                .setTransitive(true)
                .setDescription('Scala Style libraries to be used for this project.')

        project.task(type: ScalaStyleTask, 'scalaStyle')
        project.tasks.scalaStyle.outputs.upToDateWhen { false }



    }
}
