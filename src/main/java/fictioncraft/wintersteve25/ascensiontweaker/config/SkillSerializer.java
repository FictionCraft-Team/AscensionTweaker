package fictioncraft.wintersteve25.ascensiontweaker.config;

import fictioncraft.wintersteve25.ascensiontweaker.config.object.SimpleAOALevelProvider;
import fictioncraft.wintersteve25.fclib.api.json.ErrorUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.tslat.aoa3.util.constant.Skills;
import net.tslat.aoa3.util.player.PlayerDataManager;
import net.tslat.aoa3.util.player.PlayerUtil;

public class SkillSerializer {
    public static Skills getSkillFromJson(SimpleAOALevelProvider jsonIn) {
        return Skills.valueOf(jsonIn.getName());
    }

    public static boolean areConditionsMet(PlayerEntity player, SimpleAOALevelProvider jsonIn, ResourceLocation configUID) {
        if (!player.getEntityWorld().isRemote()) {
            PlayerDataManager playerDataManager = PlayerUtil.getAdventPlayer((ServerPlayerEntity)player);
            Skills skills = getSkillFromJson(jsonIn);

            if (skills == null) {
                ErrorUtils.sendError(new TranslationTextComponent("ascensiontweaker.error.skillNotFound", jsonIn.getName(), configUID), player);
                return false;
            }

            int level = playerDataManager.stats().getLevel(skills);

            return level >= jsonIn.getLevel();
        }

        return false;
    }
}
