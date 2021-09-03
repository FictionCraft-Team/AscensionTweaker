package fictioncraft.wintersteve25.ascensiontweaker;

import fictioncraft.wintersteve25.ascensiontweaker.config.MainConfig;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOALevelProvider;
import fictioncraft.wintersteve25.ascensiontweaker.events.ServerEventsHandler;
import fictioncraft.wintersteve25.fclib.api.json.objects.ProviderType;
import fictioncraft.wintersteve25.fclib.api.json.utils.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.tslat.aoa3.util.constant.Skills;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

@Mod(AscensionTweaker.MODID)
public class AscensionTweaker {

    public static final String MODID = "ascensiontweaker";
    public static final Logger LOGGER = LogManager.getLogger("AscensionTweaker");

    public static final ProviderType AOA_LEVEL = new ProviderType("AOALevel", null, null, (player, stack, outputTag, tagIndex) -> {
        if (!player.getEntityWorld().isRemote()) {
            for (Skills skill : Skills.values()) {
                String name = JsonUtils.jsonStringFromObject(skill);
                player.sendMessage(new TranslationTextComponent(name), player.getUniqueID());
                LOGGER.info(name);
            }
        }
        return 1;
    }, SimpleAOALevelProvider.class);

    public static final String MOB_ATTACK = "mob_kill";
    public static final String ITEM_USE = "item_use";
    public static final String BLOCK_PLACEMENT = "block_placement";
    public static final String BLOCK_INTERACTION = "block_interaction";
    public static final String BLOCK_HARVEST = "block_harvest";
    public static final String ENTER_DIMENSION = "enter_dimension";
    public static final String PICKUP_FLUID = "pickup_fluid";

    public static final MainConfig CONFIG = new MainConfig();

    public AscensionTweaker() {
        CONFIG.registerConfig();

        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::mobHurtEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::blockPlaceEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::blockInteractionEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::blockHarvestEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::enterDimensionEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::fillBucketEvent);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::onJsonReload);
        MinecraftForge.EVENT_BUS.addListener(ServerEventsHandler::playerTick);
    }
}
