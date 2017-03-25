/*
 * Copyright (C) 2017 OSTracker
 *
 * This file is a part of OSTracker.
 *
 * OSTracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSTracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSTracker. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ostracker.cache.dumpers;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ostracker.OSTracker;
import com.ostracker.cache.loaders.ItemFileLoader;
import net.runelite.cache.definitions.ItemDefinition;
import net.runelite.cache.definitions.loaders.ItemLoader;
import net.runelite.cache.fs.File;
import net.runelite.cache.io.InputStream;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class ItemDefinitionSerializer {

    private static Gson GSON;

    static {
        Map<String, String> fieldNameMappings = new HashMap<>();
        fieldNameMappings.put("notedID", "noteId");
        fieldNameMappings.put("cost", "storePrice");
        fieldNameMappings.put("members", "isMembers");
        fieldNameMappings.put("stackable", "isStackable");
        fieldNameMappings.put("team", "teamId");
        fieldNameMappings.put("options", "groundOptions");
        fieldNameMappings.put("interfaceOptions", "widgetOptions");
        fieldNameMappings.put("inventoryModel", "inventoryModelId");

        Set<String> fieldsToSerialize = new HashSet<>();
        fieldsToSerialize.addAll(fieldNameMappings.keySet());
        fieldsToSerialize.add("name");
        fieldsToSerialize.add("isTradeable");
        fieldsToSerialize.add("boughtId");
        fieldsToSerialize.add("boughtTemplateId");
        fieldsToSerialize.add("placeholderId");
        fieldsToSerialize.add("placeholderTemplateId");

        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingStrategy(f -> {
                    String oldName = f.getName();
                    String newName = fieldNameMappings.get(oldName);
                    if (newName != null) {
                        return newName;
                    }
                    return oldName;
                })
                .addSerializationExclusionStrategy(new ExclusionStrategy() {

                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return !fieldsToSerialize.contains(f.getName());
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
    }

    private ItemLoader itemLoader = new ItemLoader();

    private List<net.runelite.cache.fs.File> itemFiles;

    public ItemDefinitionSerializer(ItemFileLoader itemFileLoader) {
        this.itemFiles = itemFileLoader.getItemFiles();
    }

    public void dump(int itemId) {
        File f = itemFiles.get(itemId);
        if (f == null) {
            throw new NullPointerException("A file for item " + itemId + " could not be found in the cache");
        }

        java.io.File file = new java.io.File(OSTracker.ITEM_DUMP_ROOT,
                itemId + "/def.json");

        if (!file.exists()) {
            file.getParentFile().mkdirs();

            ItemDefinition definition = itemLoader.load(itemId, f.getContents());

            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(definition, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}