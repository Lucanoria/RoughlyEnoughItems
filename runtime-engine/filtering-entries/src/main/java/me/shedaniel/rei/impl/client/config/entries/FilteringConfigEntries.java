package me.shedaniel.rei.impl.client.config.entries;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonNull;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerInternal;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRuleInternal;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public class FilteringConfigEntries implements ConfigManagerInternal.SystemSetup {
    @Override
    public void setup(GuiRegistry registry) {
        registry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
                    List<EntryStack<?>> value = CollectionUtils.map(Utils.<List<EntryStackProvider<?>>>getUnsafely(field, config, new ArrayList<>()), EntryStackProvider::provide);
                    List<EntryStack<?>> defaultValue = CollectionUtils.map(Utils.<List<EntryStackProvider<?>>>getUnsafely(field, defaults), EntryStackProvider::provide);
                    Consumer<List<EntryStack<?>>> saveConsumer = (newValue) -> {
                        setUnsafely(field, config, CollectionUtils.map(newValue, EntryStackProvider::ofStack));
                    };
                    try {
                        Field filteringRules = config.getClass().getDeclaredField("filteringRules");
                        return REIRuntime.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null ?
                                Collections.singletonList(new NoFilteringEntry(220, value, defaultValue, saveConsumer))
                                :
                                Collections.singletonList(new FilteringEntry(220, value, Utils.getUnsafely(filteringRules, config), defaultValue, saveConsumer, list -> setUnsafely(filteringRules, config, Lists.newArrayList(list))));
                    } catch (NoSuchFieldException e) {
                        throw new RuntimeException(e);
                    }
                }
                , (field) -> field.getType() == List.class && field.getName().equals("filteredStacks"));
    }
    
    @Override
    public void setup(Jankson.Builder builder) {
        // FilteringRule
        builder.registerSerializer(FilteringRule.class, (value, marshaller) -> {
            try {
                return marshaller.serialize(FilteringRuleType.save((FilteringRuleInternal) value, new CompoundTag()));
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read((CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        builder.registerDeserializer(String.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read(TagParser.parseTag(value));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}