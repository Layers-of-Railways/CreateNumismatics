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

package dev.ithundxr.numismaticsgradle.asm

import dev.ithundxr.numismaticsgradle.asm.internal.SubprojectType
import dev.ithundxr.numismaticsgradle.asm.transformers.CCCapabilitiesASM
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter

class NumismaticsGradleASM {
    fun transformClass(projectPath: String, bytes: ByteArray): ByteArray {
        // Get project type
        val project = SubprojectType.getProjectType(projectPath)

        var node = ClassNode()
        ClassReader(bytes).accept(node, 0)

        // Transformers
        node = CCCapabilitiesASM().transform(project, node)

        // Verify the bytecode is valid
        val byteArray = ClassWriter(0).also { node.accept(it) }.toByteArray()
        ClassReader(byteArray).accept(CheckClassAdapter(null), 0)
        return byteArray
    }
}