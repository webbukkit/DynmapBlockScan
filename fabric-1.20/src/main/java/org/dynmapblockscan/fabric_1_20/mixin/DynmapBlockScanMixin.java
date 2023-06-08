package org.dynmapblockscan.fabric_1_20.mixin;

import net.minecraft.client.gui.screen.TitleScreen;

import org.dynmapblockscan.fabric_1_20.DynmapBlockScanMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class DynmapBlockScanMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        DynmapBlockScanMod.logger.info("This line is printed by an example mod mixin!");
    }
}