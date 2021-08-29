package fictioncraft.wintersteve25.ascensiontweaker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fictioncraft.wintersteve25.ascensiontweaker.AscensionTweaker;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOAConfigObject;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOALevelProvider;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOAMap;
import fictioncraft.wintersteve25.fclib.api.json.base.IJsonConfig;
import fictioncraft.wintersteve25.fclib.api.json.objects.SimpleObjectMap;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.*;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleBlockProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleEntityProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleFluidProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleItemProvider;
import fictioncraft.wintersteve25.fclib.api.json.utils.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainConfig implements IJsonConfig {

    public SimpleAOAMap configData;

    private final File configFile = JsonUtils.getConfigFile(this.UID().getNamespace(), false);
    private final File exampleFile = JsonUtils.getConfigFile(this.UID().getNamespace(), true);

    private static final SimpleAOAConfigObject ENTITY_CONFIG_OBJECT = new SimpleAOAConfigObject(new SimpleEntityProvider("minecraft:enderman", false), new SimpleAOALevelProvider("HUNTER", 10));
    private static final SimpleAOAConfigObject BLOCK_CONFIG_OBJECT = new SimpleAOAConfigObject(new SimpleBlockProvider("minecraft:diamond_ore", false, "", false), new SimpleAOALevelProvider("EXTRACTION", 4));
    private static final SimpleAOAConfigObject ITEM_CONFIG_OBJECT = new SimpleAOAConfigObject(new SimpleItemProvider("minecraft:bow", 1, "", false), new SimpleAOALevelProvider("HUNTER", 10));
    private static final SimpleAOAConfigObject FLUID_CONFIG_OBJECT = new SimpleAOAConfigObject(new SimpleFluidProvider("minecraft:lava", 1000, "", false), new SimpleAOALevelProvider("ENGINEERING", 2));
    private static final SimpleAOAConfigObject DIMENSION_CONFIG_OBJECT = new SimpleAOAConfigObject(new SimpleObjProvider("minecraft:nether", false, "Dimensions"), new SimpleAOALevelProvider("HUNTER", 5));

    public static final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    @Override
    public void write() {
        JsonUtils.createDirectory();
        if (!this.configFile.exists()) {
            PrintWriter writer = JsonUtils.createWriter(this.configFile, AscensionTweaker.LOGGER);

            Map<String, List<SimpleAOAConfigObject>> configObjectMap = new HashMap<>();
            List<SimpleAOAConfigObject> emptyList = new ArrayList<>();
            configObjectMap.putIfAbsent(AscensionTweaker.MOB_ATTACK, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.ITEM_USE, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_PLACEMENT, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_INTERACTION, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_HARVEST, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.ENTER_DIMENSION, emptyList);
            configObjectMap.putIfAbsent(AscensionTweaker.PICKUP_FLUID, emptyList);
            SimpleAOAMap map = new SimpleAOAMap(configObjectMap);

            try {
                gson.excluder().excludeField(SimpleObjectMap.class.getDeclaredField("configs"), false);
            } catch (NoSuchFieldException ignored) {}

            writer.print(gson.toJson(map));
            writer.close();
        }
    }

    @Override
    public void example() {
        JsonUtils.createDirectory();
        PrintWriter writer = JsonUtils.createWriter(this.exampleFile, AscensionTweaker.LOGGER);

        Map<String, List<SimpleAOAConfigObject>> configObjectMap = new HashMap<>();

        List<SimpleAOAConfigObject> entityList = new ArrayList<>();
        entityList.add(ENTITY_CONFIG_OBJECT);
        List<SimpleAOAConfigObject> blockList = new ArrayList<>();
        blockList.add(BLOCK_CONFIG_OBJECT);
        List<SimpleAOAConfigObject> itemList = new ArrayList<>();
        itemList.add(ITEM_CONFIG_OBJECT);
        List<SimpleAOAConfigObject> fluidList = new ArrayList<>();
        fluidList.add(FLUID_CONFIG_OBJECT);
        List<SimpleAOAConfigObject> dimensionList = new ArrayList<>();
        dimensionList.add(DIMENSION_CONFIG_OBJECT);

        configObjectMap.putIfAbsent(AscensionTweaker.MOB_ATTACK, entityList);
        configObjectMap.putIfAbsent(AscensionTweaker.ITEM_USE, itemList);
        configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_PLACEMENT, blockList);
        configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_INTERACTION, blockList);
        configObjectMap.putIfAbsent(AscensionTweaker.BLOCK_HARVEST, blockList);
        configObjectMap.putIfAbsent(AscensionTweaker.ENTER_DIMENSION, dimensionList);
        configObjectMap.putIfAbsent(AscensionTweaker.PICKUP_FLUID, fluidList);

        try {
            gson.excluder().excludeField(SimpleObjectMap.class.getDeclaredField("configs"), false);
        } catch (NoSuchFieldException ignore) {}

        SimpleAOAMap map = new SimpleAOAMap(configObjectMap);

        writer.print(gson.toJson(map));
        writer.close();
    }

    @Override
    public void read() {
        if (!this.configFile.exists()) {
            AscensionTweaker.LOGGER.warn("{} Config json not found! Creating a new one..", this.UID().getNamespace());
            this.write();
        } else {
            try {
                AscensionTweaker.LOGGER.info("Attempting to read {}", this.configFile.getName());
                Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
                SimpleAOAMap c = gson.fromJson(new FileReader(this.configFile), SimpleAOAMap.class);
                if (c != null && c.getConfig() != null && !c.getConfig().isEmpty()) {
                    this.configData = c;
                }
            } catch (FileNotFoundException e) {
                AscensionTweaker.LOGGER.warn("{} Config json configFile not found! Creating a new one..", this.UID().getNamespace());
                e.printStackTrace();
                this.write();
            }
        }
    }

    @Override
    public ResourceLocation UID() {
        return new ResourceLocation(AscensionTweaker.MODID, "main");
    }

    @Override
    public SimpleObjectMap finishedConfig() {
        return configData;
    }
}
