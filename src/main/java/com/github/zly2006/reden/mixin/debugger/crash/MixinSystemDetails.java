package com.github.zly2006.reden.mixin.debugger.crash;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.util.SystemDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(value = SystemDetails.class, priority = 1001/* Make it apply later */)
public abstract class MixinSystemDetails {
    @Shadow
    public abstract void addSection(String string, Supplier<String> supplier);

    @Inject(at = @At("RETURN"), method = "<init>")
    private void fillSystemDetails(CallbackInfo info) {
        addSection("Reden Debugger", () -> {
            StringBuilder sb = new StringBuilder();
            if (RedenCarpetSettings.redenDebuggerEnabled) {
                sb.append("Enabled (");
                if (RedenCarpetSettings.redenDebuggerBlockUpdates) {
                    sb.append("Block Updates, ");
                }
                if (RedenCarpetSettings.redenDebuggerItemShadow) {
                    sb.append("Item Shadows, ");
                }
                sb.append(")");
                if (UtilsKt.server != null) {
                    ServerData serverData = data(UtilsKt.server);
                    if (serverData.realTicks > 1) {
                        sb.append(" Incredible! Reden Debugger worked for more than %d full tick!!"
                                .formatted(serverData.realTicks - 1));
                    }
                }
            } else {
                sb.append("Disabled");
            }
            return sb.toString();
        });
    }
}
