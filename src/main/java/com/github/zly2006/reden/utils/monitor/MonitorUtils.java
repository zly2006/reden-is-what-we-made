package com.github.zly2006.reden.utils.monitor;

import com.github.zly2006.reden.Reden;
import org.jetbrains.annotations.Nullable;

public class MonitorUtils {
    private MonitorUtils() {
    }

    static public boolean profileStarted = false;
    static public int profileStartDelay = 10;
    static public int profileInterval = 20;

    @Nullable
    static private String thisFunction;
    static private boolean runProfilerThisTick;
    static private long startNano;
    static private long totalNano;
    static private long serverStartNano;
    static private long serverTotalNano;

    public static void serverStart() {
        profileStarted = false;
        serverStartNano = System.nanoTime();
    }

    public static void serverEnd() {
        if (profileStarted) {
            end();
            Reden.LOGGER.error("{} was not ended", thisFunction);
        }
        profileStarted = false;
        serverTotalNano += System.nanoTime() - serverStartNano;
    }

    public static boolean start(String thisFunction) {
        if (profileStarted) {
            return false;
        }
        MonitorUtils.thisFunction = thisFunction;
        startNano = System.nanoTime();
        profileStarted = true;
        return true;
    }

    public static void end() {
        if (!profileStarted) {
            throw new IllegalStateException("Profile not started");
        }
        profileStarted = false;
        long time = System.nanoTime() - startNano;
        totalNano += time;
        onFunctionExit(time, thisFunction);
        thisFunction = null;
    }

    private static void onFunctionExit(long time, String name) {

    }
}
