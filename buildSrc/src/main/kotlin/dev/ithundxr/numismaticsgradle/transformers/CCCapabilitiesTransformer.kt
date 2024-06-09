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

package dev.ithundxr.numismaticsgradle.transformers

import dev.ithundxr.lotus.gradle.api.asm.util.IClassTransformer
import dev.ithundxr.lotus.gradle.api.asm.util.SubprojectType
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class CCCapabilitiesTransformer : IClassTransformer {
    @Suppress("LocalVariableName")
    override fun transform(project: SubprojectType, node: ClassNode) {
        if (node.invisibleAnnotations != null && project == SubprojectType.FORGE) {
            // Cache the field, so we don't CME the list during the remove
            val annotationNodes = node.invisibleAnnotations.toList()
            for (annotationNode in annotationNodes) {
                if (annotationNode.desc.equals("Ldev/ithundxr/createnumismatics/annotation/asm/CCForgeImpl;")) {
                    // Remove @CCForgeImpl
                    node.invisibleAnnotations.remove(annotationNode)

                    // Add the interface that's needed
                    node.interfaces.add("net/minecraftforge/common/capabilities/ICapabilityProvider")

                    // getCapability method
                    run {
                        val mv = node.visitMethod(
                            Opcodes.ACC_PUBLIC,
                            "getCapability",
                            "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
                            null,
                            null
                        )
                        mv.visitCode()

                        val L1 = Label()

                        // L0
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitVarInsn(Opcodes.ALOAD, 1)
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "isPeripheralCap",
                            "(Lnet/minecraftforge/common/capabilities/Capability;)Z",
                            false
                        )
                        mv.visitJumpInsn(Opcodes.IFEQ, L1)

                        // L2
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "getPeripheralCapability",
                            "()Lnet/minecraftforge/common/util/LazyOptional;",
                            false
                        )
                        mv.visitInsn(Opcodes.ARETURN)

                        // L1
                        mv.visitLabel(L1)
                        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitVarInsn(Opcodes.ALOAD, 1)
                        mv.visitVarInsn(Opcodes.ALOAD, 2)
                        mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL,
                            node.name,
                            "getCapability",
                            "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
                            false
                        )
                        mv.visitInsn(Opcodes.ARETURN)

                        mv.visitEnd()
                    }

                    // invalidateCaps method
                    run {
                        val mv = node.visitMethod(Opcodes.ACC_PUBLIC, "invalidateCaps", "()V", null, null)
                        mv.visitCode()

                        // L0
                        val L0 = Label()
                        mv.visitLabel(L0)
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, node.name, "invalidateCaps", "()V", false)

                        // L1
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "removePeripheral",
                            "()V",
                            false
                        )

                        // L2
                        mv.visitLabel(Label())
                        mv.visitInsn(Opcodes.RETURN)

                        mv.visitEnd()
                    }
                }
            }
        }
    }
}