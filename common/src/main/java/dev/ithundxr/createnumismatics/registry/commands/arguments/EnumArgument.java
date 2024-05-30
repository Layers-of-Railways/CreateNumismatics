/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package dev.ithundxr.createnumismatics.registry.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
        (found, constants) -> Component.translatable("commands.numismatics.arguments.enum.invalid", constants, found));
    private final Class<T> enumClass;
    
    public static <T extends Enum<T>> EnumArgument<T> enumArgument(Class<T> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    protected EnumArgument(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw INVALID_ENUM.createWithContext(reader, name, Arrays.toString(Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Stream.of(enumClass.getEnumConstants()).map(Enum::name), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Stream.of(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }

    public static class Info<T extends Enum<T>> implements ArgumentTypeInfo<EnumArgument<T>, Info<T>.Template>
    {
        @Override
        public void serializeToNetwork(EnumArgument.Info.Template template, FriendlyByteBuf buffer)
        {
            buffer.writeUtf(template.enumClass.getName());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf buffer)
        {
            try
            {
                String name = buffer.readUtf();
                return new Template((Class<T>) Class.forName(name));
            }
            catch (ClassNotFoundException e)
            {
                return null;
            }
        }

        @Override
        public void serializeToJson(Template template, JsonObject json)
        {
            json.addProperty("enum", template.enumClass.getName());
        }

        @Override
        public @NotNull Template unpack(EnumArgument<T> argument)
        {
            return new Template(argument.enumClass);
        }

        public class Template implements ArgumentTypeInfo.Template<EnumArgument<T>>
        {
            final Class<T> enumClass;

            Template(Class<T> enumClass)
            {
                this.enumClass = enumClass;
            }

            @Override
            public @NotNull EnumArgument<T> instantiate(@NotNull CommandBuildContext pStructure)
            {
                return new EnumArgument<>(this.enumClass);
            }

            @Override
            public @NotNull ArgumentTypeInfo<EnumArgument<T>, ?> type()
            {
                return Info.this;
            }
        }
    }
}
