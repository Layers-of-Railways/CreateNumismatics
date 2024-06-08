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

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.9.23"
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("numismaticsPlugin") {
            id = "dev.ithundxr.numismatics.gradle"
            implementationClass = "dev.ithundxr.numismaticsgradle.NumismaticsGradlePlugin"
        }
    }
}

dependencies {
    implementation("org.ow2.asm:asm:${"asm_version"()}")
    //implementation("org.ow2.asm:asm-analysis:${"asm_version"()}")
    //implementation("org.ow2.asm:asm-commons:${"asm_version"()}")
    implementation("org.ow2.asm:asm-tree:${"asm_version"()}")
    implementation("org.ow2.asm:asm-util:${"asm_version"()}")
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
