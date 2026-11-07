package com.skinall.mixin;

import com.skinall.SkinAllClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void skinall$override(CallbackInfoReturnable<SkinTextures> cir) {
        if (!SkinAllClient.enabled || !SkinAllClient.ready) return;

        PlayerListEntry entry = (PlayerListEntry) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!SkinAllClient.includeSelf && mc.player != null) {
            ClientPlayNetworkHandler handler = mc.getNetworkHandler();
            if (handler != null && entry == handler.getPlayerListEntry(mc.player.getUuid())) return;
        }

        SkinTextures original = cir.getReturnValue();
        AssetInfo.TextureAssetInfo body = new AssetInfo.TextureAssetInfo(SkinAllClient.SKIN_ID, SkinAllClient.SKIN_ID);
        cir.setReturnValue(new SkinTextures(
                body,
                original.cape(),
                original.elytra(),
                SkinAllClient.slim ? PlayerSkinType.SLIM : PlayerSkinType.WIDE,
                true
        ));
    }
}
