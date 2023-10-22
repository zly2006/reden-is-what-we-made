package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.EndStage;
import com.github.zly2006.reden.debugger.stages.ServerRootStage;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tickWorlds(BooleanSupplier shouldKeepTicking) {
        TickStage stage = getRedenServerData().getTickStageTree().peekLeaf();

        if (stage instanceof ServerRootStage) {
            /**
             * Called by vanilla,
             * {@link ServerRootStage#tick} is already called, don't call again.
             * Leave injecting points for other mods.
             */

            this.profiler.push("commandFunctions");
            this.getCommandFunctionManager().tick();
            this.profiler.swap("levels");
        } else if (stage instanceof WorldRootStage rootStage) {
            System.out.println("World stage " + rootStage.getWorld().getRegistryKey().getValue());
            /**
             * Called by {@link WorldRootStage#tick}, so dont need to call it
             * leave injecting points for other mods
             */
            ServerWorld serverWorld = rootStage.getWorld();

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
        } else if (stage instanceof EndStage) {
            System.out.println("End stage");

            this.profiler.swap("connection");
            this.getNetworkIo().tick();
            this.profiler.swap("players");
            this.playerManager.updatePlayerLatency();
            if (SharedConstants.isDevelopment) {
                TestManager.INSTANCE.tick();
            }

            this.profiler.swap("server gui refresh");

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < this.serverGuiTickables.size(); ++i) {
                this.serverGuiTickables.get(i).run();
            }

            this.profiler.pop();
        }
    }
}
