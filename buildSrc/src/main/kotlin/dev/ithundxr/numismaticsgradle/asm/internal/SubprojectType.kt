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

package dev.ithundxr.numismaticsgradle.asm.internal

import org.gradle.api.Project

enum class SubprojectType {
    COMMON, FABRIC, FORGE;

    companion object {
        fun getProjectType(project: Project): SubprojectType {
            return when (project.path) {
                ":common" -> COMMON
                ":fabric" -> FABRIC
                ":forge" -> FORGE
                else -> throw IllegalStateException("Invalid Project Type")
            }
        }
    }
}