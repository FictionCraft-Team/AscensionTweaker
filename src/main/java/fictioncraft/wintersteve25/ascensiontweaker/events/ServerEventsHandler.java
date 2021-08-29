package fictioncraft.wintersteve25.ascensiontweaker.events;

import fictioncraft.wintersteve25.ascensiontweaker.AscensionTweaker;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOAConfigObject;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOALevelProvider;
import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOAMap;
import fictioncraft.wintersteve25.ascensiontweaker.config.SkillSerializer;
import fictioncraft.wintersteve25.fclib.api.events.JsonConfigEvent;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.SimpleObjProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleBlockProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleEntityProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleFluidProvider;
import fictioncraft.wintersteve25.fclib.api.json.objects.providers.templates.SimpleItemProvider;
import fictioncraft.wintersteve25.fclib.api.json.utils.JsonSerializer.*;
import fictioncraft.wintersteve25.fclib.common.helper.MiscHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.tslat.aoa3.util.constant.Skills;
import net.tslat.aoa3.util.player.PlayerUtil;

import java.util.List;

public class ServerEventsHandler {

    public static final ResourceLocation UID = AscensionTweaker.CONFIG.UID();
    public static SimpleAOAMap configMap = AscensionTweaker.CONFIG.configData;
    private static List<SimpleAOAConfigObject> mobAttack;
    public static List<SimpleAOAConfigObject> itemUse;
    private static List<SimpleAOAConfigObject> blockPlacement;
    private static List<SimpleAOAConfigObject> blockInteraction;
    private static List<SimpleAOAConfigObject> blockHarvest;
    private static List<SimpleAOAConfigObject> enterDim;
    private static List<SimpleAOAConfigObject> pickupFluid;

    public static void onJsonReload(JsonConfigEvent.Post event) {
        if (event.getStage() == JsonConfigEvent.JsonConfigLoadStages.READ) {
            configMap = AscensionTweaker.CONFIG.configData;
            mobAttack = configMap.getConfig().get(AscensionTweaker.MOB_ATTACK);
            itemUse = configMap.getConfig().get(AscensionTweaker.ITEM_USE);
            blockPlacement = configMap.getConfig().get(AscensionTweaker.BLOCK_PLACEMENT);
            blockInteraction = configMap.getConfig().get(AscensionTweaker.BLOCK_INTERACTION);
            blockHarvest = configMap.getConfig().get(AscensionTweaker.BLOCK_HARVEST);
            enterDim = configMap.getConfig().get(AscensionTweaker.ENTER_DIMENSION);
            pickupFluid = configMap.getConfig().get(AscensionTweaker.PICKUP_FLUID);
        }
    }

    public static void mobHurtEvent(AttackEntityEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        mobAttack = configMap.getConfig().get(AscensionTweaker.MOB_ATTACK);

        PlayerEntity player = event.getPlayer();
        if (basicChecks(mobAttack, player)) {
            Entity entity = event.getTarget();
            for (SimpleAOAConfigObject cfg : mobAttack) {
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();

                Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                int level = levelProvider.getLevel();

                if (cfg.getTarget() instanceof SimpleEntityProvider) {
                    SimpleEntityProvider entityProvider = (SimpleEntityProvider) cfg.getTarget();
                    if (EntitySerialization.doesEntitiesMatch(entity, entityProvider)) {
                        if (!SkillSerializer.areConditionsMet(player, cfg.getLevelProvider(), UID)) {
                            PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) player, skill, level);
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        itemUse = configMap.getConfig().get(AscensionTweaker.ITEM_USE);

        PlayerEntity player = event.player;
        if (basicChecks(itemUse, player)) {
            for (SimpleAOAConfigObject cfg : itemUse) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleItemProvider) {
                    SimpleItemProvider itemProvider = (SimpleItemProvider) objProvider;
                    ItemStack heldItem = player.getHeldItemMainhand();
                    if (ItemStackSerializer.doesItemStackMatch(heldItem, itemProvider)) {
                        if (!SkillSerializer.areConditionsMet(player, levelProvider, UID)) {
                            Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                            int level = levelProvider.getLevel();

                            player.dropItem(heldItem.copy(), true);
                            PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) player, skill, level);
                            heldItem.shrink(heldItem.getCount());
                        }
                    }
                }
            }
        }
    }

    public static void blockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        blockPlacement = configMap.getConfig().get(AscensionTweaker.BLOCK_PLACEMENT);

        Entity entity = event.getEntity();
        if (basicChecks(blockPlacement, entity)) {
            World world = entity.getEntityWorld();
            for (SimpleAOAConfigObject cfg : blockPlacement) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleBlockProvider) {
                    SimpleBlockProvider provider = (SimpleBlockProvider) objProvider;
                    if (BlockSerializer.doesBlockMatch(BlockSerializer.getPair(world, event.getPos()), provider)) {
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity player = (PlayerEntity) entity;
                            if (!SkillSerializer.areConditionsMet(player, levelProvider, UID)) {
                                Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                                int level = levelProvider.getLevel();
                                PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) player, skill, level);
                                event.setCanceled(true);
                            }
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public static void blockInteractionEvent(PlayerInteractEvent.RightClickBlock event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        blockInteraction = configMap.getConfig().get(AscensionTweaker.BLOCK_INTERACTION);

        Entity entity = event.getEntity();
        if (basicChecks(blockInteraction, entity)) {
            World world = entity.getEntityWorld();
            for (SimpleAOAConfigObject cfg : blockInteraction) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleBlockProvider) {
                    SimpleBlockProvider provider = (SimpleBlockProvider) objProvider;
                    if (BlockSerializer.doesBlockMatch(BlockSerializer.getPair(world, event.getPos()), provider)) {
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity player = (PlayerEntity) entity;
                            if (!SkillSerializer.areConditionsMet(player, levelProvider, UID)) {
                                Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                                int level = levelProvider.getLevel();
                                PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) player, skill, level);
                                event.setCanceled(true);
                            }
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public static void blockHarvestEvent(BlockEvent.BreakEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        blockHarvest = configMap.getConfig().get(AscensionTweaker.BLOCK_HARVEST);

        PlayerEntity player = event.getPlayer();
        if (basicChecks(blockHarvest, player)) {
            World world = player.getEntityWorld();
            for (SimpleAOAConfigObject cfg : blockHarvest) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleBlockProvider) {
                    SimpleBlockProvider provider = (SimpleBlockProvider) objProvider;
                    if (BlockSerializer.doesBlockMatch(BlockSerializer.getPair(world, event.getPos()), provider)) {
                        if (player instanceof FakePlayer) {
                            event.setCanceled(true);
                        }
                        if (!SkillSerializer.areConditionsMet(player, levelProvider, UID)) {
                            Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                            int level = levelProvider.getLevel();
                            PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) player, skill, level);
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public static void enterDimensionEvent(EntityTravelToDimensionEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        enterDim = configMap.getConfig().get(AscensionTweaker.ENTER_DIMENSION);

        Entity entity = event.getEntity();
        if (basicChecks(enterDim, entity)) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity) entity;
                for (SimpleAOAConfigObject cfg : enterDim) {
                    SimpleObjProvider objProvider = cfg.getTarget();
                    SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                    if (levelProvider == null) return;
                    RegistryKey<World> dimension = event.getDimension();
                    if (dimension == null) return;
                    String dimName = dimension.toString();
                    if (MiscHelper.isStringValid(dimName)) {
                        String processedDimName = dimName.substring(dimName.indexOf('/') + 2, dimName.length() - 1);
                        if (processedDimName.equals(objProvider.getName())) {
                            if (!SkillSerializer.areConditionsMet(playerEntity, levelProvider, UID)) {
                                Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                                PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) playerEntity, skill, levelProvider.getLevel());
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void fillBucketEvent(FillBucketEvent event) {
        if (configMap == null || !MiscHelper.isMapValid(configMap.getConfig())) return;

        pickupFluid = configMap.getConfig().get(AscensionTweaker.PICKUP_FLUID);

        Entity entity = event.getEntity();
        if (basicChecks(pickupFluid, entity)) {

            //if current bucket has fluid meaning this is an emptying operation, so we stop
            ItemStack emptyBucket = event.getEmptyBucket();
            if (!emptyBucket.isEmpty() && FluidUtil.getFluidContained(emptyBucket).isPresent()) return;

            RayTraceResult rayTraceResult = event.getTarget();
            if (rayTraceResult == null) return;
            if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK) return;
            BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
            BlockPos pos = blockRayTraceResult.getPos().offset(blockRayTraceResult.getFace());
            FluidState fluidState = event.getWorld().getFluidState(pos);
            if (fluidState.isEmpty()) return;
            Fluid fluid = fluidState.getFluid();
            if (!MiscHelper.isFluidValid(fluid)) return;
            for (SimpleAOAConfigObject cfg : pickupFluid) {
                SimpleObjProvider objProvider = cfg.getTarget();
                SimpleAOALevelProvider levelProvider = cfg.getLevelProvider();
                if (levelProvider == null) return;
                if (objProvider instanceof SimpleFluidProvider) {
                    SimpleFluidProvider provider = (SimpleFluidProvider) objProvider;
                    if (FluidSerializer.doesFluidMatch(new FluidStack(fluid, 1000), provider)) {
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity playerEntity = (PlayerEntity) entity;
                            if (!SkillSerializer.areConditionsMet(playerEntity, levelProvider, UID)) {
                                Skills skill = SkillSerializer.getSkillFromJson(levelProvider);
                                PlayerUtil.notifyPlayerOfInsufficientLevel((ServerPlayerEntity) playerEntity, skill, levelProvider.getLevel());
                                event.setCanceled(true);
                            }
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    private static boolean basicChecks(List<?> configList, Entity entity) {
        if (!MiscHelper.isListValid(configList)) return false;
        if (entity == null) return false;
        World world = entity.getEntityWorld();
        return !world.isRemote();
    }
}