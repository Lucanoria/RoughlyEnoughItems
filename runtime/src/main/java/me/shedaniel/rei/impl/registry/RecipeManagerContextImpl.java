/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.api.registry.RecipeManagerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManagerContextImpl implements RecipeManagerContext {
    private static final Comparator<Recipe<?>> RECIPE_COMPARATOR = Comparator.comparing((Recipe<?> o) -> o.getId().getNamespace()).thenComparing(o -> o.getId().getPath());
    private List<Recipe<?>> sortedRecipes = null;
    
    @Override
    public List<Recipe<?>> getAllSortedRecipes() {
        if (sortedRecipes == null) {
            this.sortedRecipes = getRecipeManager().getRecipes().parallelStream().sorted(RECIPE_COMPARATOR).collect(Collectors.toList());
        }
        
        return Collections.unmodifiableList(sortedRecipes);
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        return Minecraft.getInstance().getConnection().getRecipeManager();
    }
    
    @Override
    public void startReload() {
        this.sortedRecipes = null;
    }
}