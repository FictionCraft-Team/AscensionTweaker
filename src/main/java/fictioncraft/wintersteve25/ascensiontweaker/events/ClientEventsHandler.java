package fictioncraft.wintersteve25.ascensiontweaker.events;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import fictioncraft.wintersteve25.ascensiontweaker.AscensionTweaker;
import fictioncraft.wintersteve25.ascensiontweaker.config.SkillSerializer;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOAConfigObject;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOALevelProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.SimpleObjProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleItemProvider;
import fictioncraft.wintersteve25.fclib.api.json.utils.JsonSerializer.ItemStackSerializer;
import fictioncraft.wintersteve25.fclib.common.helper.MiscHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.tslat.aoa3.util.constant.Skills;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import static fictioncraft.wintersteve25.ascensiontweaker.events.ServerEventsHandler.configMap;
import static fictioncraft.wintersteve25.ascensiontweaker.events.ServerEventsHandler.itemUse;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = AscensionTweaker.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventsHandler {
    @SubscribeEvent
    public static void toolTipEvent(ItemTooltipEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;
        itemUse = configMap.getConfig().get(AscensionTweaker.ITEM_USE);

        ItemStack itemStack = event.getItemStack();
        PlayerEntity player = event.getPlayer();

        if (itemStack.isEmpty()) return;
        if (player == null) return;

        if (MiscHelper.isListValid(itemUse)) {
            for (SimpleAOAConfigObject cfg : itemUse) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleItemProvider) {
                    SimpleItemProvider itemProvider = (SimpleItemProvider) objProvider;
                    if (ItemStackSerializer.doesItemStackMatch(itemStack, itemProvider)) {
                        event.getToolTip().add(new TranslationTextComponent("ascensiontweaker.tooltips.itemRequirementNotMet", levelProvider.getLevel(), levelProvider.getName()));
                    }
                }
            }
        }
    }
}