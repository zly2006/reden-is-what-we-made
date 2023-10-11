package com.github.zly2006.reden.transformers;

import com.github.zly2006.reden.Reden;
import kotlinx.coroutines.YieldKt;

public class Coroutines {
    public static void foo() {
        //noinspection unchecked,rawtypes
        YieldKt.yield(Reden.rootContinuation);
    }
}
