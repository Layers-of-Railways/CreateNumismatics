/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.numismaticsgradle.transformer

import dev.ithundxr.numismaticsgradle.asm.NumismaticsGradleASM
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

class PostCompileTransformer {
    fun transformJar(project: Project, jar: File) {
        var architecturyInjectableName = project.name
        if (project.rootProject != project)
            architecturyInjectableName = project.rootProject.name + "_" + architecturyInjectableName

        val contents = linkedMapOf<String, ByteArray>()
        JarFile(jar).use {
            it.entries().asIterator().forEach { entry ->
                if (!entry.isDirectory) {
                    contents[entry.name] = it.getInputStream(entry).readAllBytes()
                }
            }
        }

        jar.delete()

        JarOutputStream(jar.outputStream()).use { out ->
            out.setLevel(Deflater.BEST_COMPRESSION)
            contents.forEach { var (name, data) = it
                if (name.contains("architectury_inject_${architecturyInjectableName}_common"))
                    return@forEach

                if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                    data = (JsonOutput.toJson(JsonSlurper().parse(data)).toByteArray())
                } else if (name.endsWith(".class")) {
                    data = NumismaticsGradleASM().transformClass(project, data)
                }

                out.putNextEntry(JarEntry(name))
                out.write(data)
                out.closeEntry()
            }
            out.finish()
            out.close()
        }
    }

}