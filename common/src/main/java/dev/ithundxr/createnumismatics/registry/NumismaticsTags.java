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

package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class NumismaticsTags {
  public enum NameSpace {

    MOD(Numismatics.MOD_ID, false, true), FORGE("forge")

    ;

    public final String id;
    public final boolean optionalDefault;
    public final boolean alwaysDatagenDefault;

    NameSpace(String id) {
      this(id, true, false);
    }

    NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
      this.id = id;
      this.optionalDefault = optionalDefault;
      this.alwaysDatagenDefault = alwaysDatagenDefault;
    }

  }



  public enum AllBlockTags {
//    SEMAPHORE_POLES(NameSpace.MOD, NameSpace.MOD.optionalDefault,false),
//    TRACK_CASING_BLACKLIST(NameSpace.MOD, NameSpace.MOD.optionalDefault,false),
//    CONDUCTOR_SPY_USABLE(NameSpace.MOD, NameSpace.MOD.optionalDefault,false) // so other mods / datapacks can make more blocks usable for conductor spies
    ;

    public final TagKey<Block> tag;


    AllBlockTags() {
      this(NameSpace.MOD);
    }

    AllBlockTags(NameSpace namespace) {
      this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
    }

    AllBlockTags(NameSpace namespace, String path) {
      this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
    }

    AllBlockTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
      this(namespace, null, optional, alwaysDatagen);
    }

    AllBlockTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
      ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
      tag = optionalTag(BuiltInRegistries.BLOCK, id);
    }

    @SuppressWarnings("deprecation")
    public boolean matches(Block block) {
      return block.builtInRegistryHolder()
          .is(tag);
    }

    public boolean matches(ItemStack stack) {
      return stack != null && stack.getItem() instanceof BlockItem blockItem && matches(blockItem.getBlock());
    }

    public boolean matches(BlockState state) {
      return state.is(tag);
    }

    public static void register() {
    }
  }

  public enum AllItemTags {
    COINS,
    CARDS,
    ID_CARDS
    ;

    public final TagKey<Item> tag;
    public final boolean alwaysDatagen;

    AllItemTags() {
      this(NameSpace.MOD);
    }

    AllItemTags(NameSpace namespace) {
      this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
    }

    AllItemTags(NameSpace namespace, String path) {
      this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
    }

    AllItemTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
      this(namespace, null, optional, alwaysDatagen);
    }

    AllItemTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
      ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
      tag = optionalTag(BuiltInRegistries.ITEM, id);
      this.alwaysDatagen = alwaysDatagen;
    }

    @SuppressWarnings("deprecation")
    public boolean matches(Item item) {
      return item.builtInRegistryHolder()
          .is(tag);
    }

    public boolean matches(ItemStack stack) {
      return stack.is(tag);
    }

    public static void register() {
    }
  }

  public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
    return TagKey.create(registry.key(), id);
  }

  // load all classes
  public static void register() {
    AllBlockTags.register();
    AllItemTags.register();
  }
}
