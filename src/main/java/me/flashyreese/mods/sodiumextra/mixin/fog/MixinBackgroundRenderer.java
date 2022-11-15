package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public abstract class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At(value = "TAIL"))
    private static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, CallbackInfo ci) {
        Entity entity = camera.getFocusedEntity();
        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.putIfAbsent(entity.world.getDimension().getEffects(), 0) : SodiumExtraClientMod.options().renderSettings.fogDistance;
        if (fogDistance == 0 || (entity instanceof LivingEntity && ((LivingEntity)entity).hasStatusEffect(StatusEffects.BLINDNESS))) {
            return;
        }
        if (camera.getSubmersionType() == CameraSubmersionType.NONE && (thickFog || fogType == BackgroundRenderer.FogType.FOG_TERRAIN)) {
            float fogStart = (float) SodiumExtraClientMod.options().renderSettings.fogStart / 100;
            if (fogDistance == 33) {
                RenderSystem.setShaderFogStart(Short.MAX_VALUE - 1 * fogStart);
                RenderSystem.setShaderFogEnd(Short.MAX_VALUE);
            } else {
                RenderSystem.setShaderFogStart(fogDistance * 16 * fogStart);
                RenderSystem.setShaderFogEnd((fogDistance + 1) * 16);
            }
        }
    }

    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
    private static void redirectSetShaderFogStart(float shaderFogStart) {
        float fogStart = (float) SodiumExtraClientMod.options().renderSettings.fogStart / 100;
        RenderSystem.setShaderFogStart(shaderFogStart * fogStart);
    }
}
