package com.github.zly2006.reden.transformers;

public class Helper {
    /**
     * com/github/zly2006/reden/transformers/Helper
     * <br>
     * unsupportedOperation
     * <br>
     * ()V
     */
    public static void unsupportedOperation() {
        throw new UnsupportedOperationException();
    }

    public static void transformerHint(String description) {
        throw new UnsupportedOperationException("Transformer hint cannot be called at runtime! Do you forget to run the transformer?");
    }
}
