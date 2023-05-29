/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.api.common.util;

import com.google.common.collect.ImmutableList;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.hooks.TagHooks;
import me.shedaniel.architectury.mixin.FluidTagsAccessor;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class EntryIngredients {
    private EntryIngredients() {
    }
    
    public static EntryIngredient of(ItemLike stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(ItemLike stack, int amount) {
        return EntryIngredient.of(EntryStacks.of(stack, amount));
    }
    
    public static EntryIngredient of(ItemStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(Fluid fluid) {
        return EntryIngredient.of(EntryStacks.of(fluid));
    }
    
    public static EntryIngredient of(Fluid fluid, Fraction amount) {
        return EntryIngredient.of(EntryStacks.of(fluid, amount));
    }
    
    public static EntryIngredient of(FluidStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static <T> EntryIngredient of(EntryType<T> type, Collection<T> values) {
        return of(type.getDefinition(), values);
    }
    
    public static <T> EntryIngredient of(EntryDefinition<T> definition, Collection<T> values) {
        if (values.size() == 0) return EntryIngredient.empty();
        if (values.size() == 1) return EntryIngredient.of(EntryStack.of(definition, values.iterator().next()));
        EntryIngredient.Builder result = EntryIngredient.builder(values.size());
        for (T value : values) {
            result.add(EntryStack.of(definition, value));
        }
        return result.build();
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks) {
        return ofItems(stacks, 1);
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks, int amount) {
        if (stacks.size() == 0) return EntryIngredient.empty();
        if (stacks.size() == 1) return EntryIngredient.of(EntryStacks.of(stacks.iterator().next(), amount));
        EntryIngredient.Builder result = EntryIngredient.builder(stacks.size());
        for (ItemLike stack : stacks) {
            result.add(EntryStacks.of(stack, amount));
        }
        return result.build();
    }
    
    public static EntryIngredient ofItemStacks(Collection<ItemStack> stacks) {
        return of(VanillaEntryTypes.ITEM, stacks);
    }
    
    public static EntryIngredient ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return EntryIngredient.empty();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return EntryIngredient.empty();
        if (matchingStacks.length == 1) return EntryIngredient.of(EntryStacks.of(matchingStacks[0]));
        EntryIngredient.Builder result = EntryIngredient.builder(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty()) {
                result.add(EntryStacks.of(matchingStack));
            }
        }
        return result.build();
    }
    
    public static List<EntryIngredient> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<EntryIngredient> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <S, T> EntryIngredient ofTag(TagCollection<S> tagCollection, ResourceLocation tagKey, Function<S, EntryStack<T>> mapper) {
        net.minecraft.tags.Tag.Named<S> holders = TagHooks.getOptional(tagKey, () -> tagCollection);
        if (holders == null) return EntryIngredient.empty();
        List<S> values = holders.getValues();
        EntryIngredient.Builder result = EntryIngredient.builder(values.size());
        for (S holder : values) {
            EntryStack<T> stack = mapper.apply(holder);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result.build();
    }
    
    public static <S, T> List<EntryIngredient> ofTags(TagCollection<S> tagCollection, Iterable<ResourceLocation> tagKeys, Function<S, EntryStack<T>> mapper) {
        if (tagKeys instanceof Collection && ((Collection<ResourceLocation>) tagKeys).isEmpty())
            return Collections.emptyList();
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (ResourceLocation tagKey : tagKeys) {
            ingredients.add(ofTag(tagCollection, tagKey, mapper));
        }
        return ingredients.build();
    }
    
    public static EntryIngredient ofItemTag(ResourceLocation tagKey) {
        return ofTag(ItemTags.getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static EntryIngredient ofItemTag(net.minecraft.tags.Tag.Named<? extends ItemLike> tagKey) {
        return ofItemTag(tagKey.getName());
    }
    
    public static EntryIngredient ofBlockTag(ResourceLocation tagKey) {
        return ofTag(BlockTags.getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static EntryIngredient ofBlockTag(net.minecraft.tags.Tag.Named<? extends Block> tagKey) {
        return ofBlockTag(tagKey.getName());
    }
    
    public static EntryIngredient ofFluidTag(ResourceLocation tagKey) {
        return ofTag(FluidTagsAccessor.getHelper().getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static EntryIngredient ofFluidTag(net.minecraft.tags.Tag.Named<? extends Fluid> tagKey) {
        return ofFluidTag(tagKey.getName());
    }
    
    public static List<EntryIngredient> ofItemTags(Iterable<ResourceLocation> tagKey) {
        return ofTags(ItemTags.getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static List<EntryIngredient> ofBlockTags(Iterable<ResourceLocation> tagKey) {
        return ofTags(BlockTags.getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static List<EntryIngredient> ofFluidTags(Iterable<ResourceLocation> tagKey) {
        return ofTags(FluidTagsAccessor.getHelper().getAllTags(), tagKey, EntryStacks::of);
    }
    
    public static <T> boolean testFuzzy(EntryIngredient ingredient, EntryStack<T> stack) {
        for (EntryStack<?> ingredientStack : ingredient) {
            if (EntryStacks.equalsFuzzy(ingredientStack, stack)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static ListTag save(List<EntryIngredient> ingredients) {
        ListTag listTag = new ListTag();
        for (EntryIngredient ingredient : ingredients) {
            listTag.add(ingredient.saveIngredient());
        }
        return listTag;
    }
    
    public static List<EntryIngredient> read(ListTag listTag) {
        if (listTag.isEmpty()) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (Tag tag : listTag) {
            ingredients.add(EntryIngredient.read((ListTag) tag));
        }
        return ingredients.build();
    }
}
