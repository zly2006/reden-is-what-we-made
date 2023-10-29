package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.EndStage;
import com.github.zly2006.reden.debugger.stages.GlobalNetworkStage;
import com.github.zly2006.reden.debugger.stages.ServerRootStage;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(value = MinecraftServer.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServer implements ServerData.ServerDataAccess {
    @Shadow private Profiler profiler;

    @Shadow public abstract void sendTimeUpdatePackets();

    @Shadow public abstract void tick(BooleanSupplier shouldKeepTicking);

    @Shadow private int ticks;

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow @Final private CommandFunctionManager commandFunctionManager;

    @Shadow public abstract CommandFunctionManager getCommandFunctionManager();

    @Shadow protected abstract void sendTimeUpdatePackets(ServerWorld world);

    @Shadow @Nullable public abstract ServerNetworkIo getNetworkIo();

    @Shadow private PlayerManager playerManager;

    @Shadow @Final private List<Runnable> serverGuiTickables;

    @Shadow @Final private ServerNetworkIo networkIo;

    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public void tickWorlds(BooleanSupplier shouldKeepTicking) {
        // Note: this variable just keeps local variable name same as vanilla
        Iterator<?> iterator = this.getWorlds().iterator();
        ServerWorld serverWorld;

        // Reden start
        TickStage stage = getRedenServerData().getTickStageTree().peekLeaf();

        if (stage instanceof ServerRootStage) {
            /**
             * Called by {@link ServerRootStage#tick}, so don't need to call it.
             * Leave injecting points for other mods.
             */

            this.profiler.push("commandFunctions");
            this.getCommandFunctionManager().tick();
            this.profiler.swap("levels");
        } else if (stage instanceof WorldRootStage rootStage) {
            /**
             * Called by {@link WorldRootStage#tick}, so don't need to call it
             * leave injecting points for other mods
             */
            serverWorld = rootStage.getWorld();

            // Vanilla start
            this.profiler.push(() -> serverWorld + " " + serverWorld.getRegistryKey().getValue());
            if (this.ticks % 20 == 0) {
                this.profiler.push("timeSync");
                this.sendTimeUpdatePackets(serverWorld);
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try {
                serverWorld.tick(shouldKeepTicking);
            } catch (Throwable var6) {
                CrashReport crashReport = CrashReport.create(var6, "Exception ticking world");
                serverWorld.addDetailsToCrashReport(crashReport);
                throw new CrashException(crashReport);
            }

            this.profiler.pop();
            this.profiler.pop();
            // Vanilla end
        } else if (stage instanceof GlobalNetworkStage) {
            this.profiler.swap("connection");
            //noinspection DataFlowIssue
            this.getNetworkIo().tick();
        } else if (stage instanceof EndStage) {
            this.profiler.swap("players");
            this.playerManager.updatePlayerLatency();
            // from vanilla, we don't need this
            //
            // if (SharedConstants.isDevelopment) {
            //     TestManager.INSTANCE.tick();
            // }

            this.profiler.swap("server gui refresh");

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < this.serverGuiTickables.size(); ++i) {
                this.serverGuiTickables.get(i).run();
            }

            this.profiler.pop();
        }
    }
}
