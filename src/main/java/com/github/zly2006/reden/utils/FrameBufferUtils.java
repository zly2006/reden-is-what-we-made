package com.github.zly2006.reden.utils;

import org.lwjgl.opengl.GL30;

public class FrameBufferUtils {
    public static void debug() {
        System.out.println("Framebuffer: (Read)" + GL30.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING) + " (Draw)" + GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING));
        System.out.println("FBO Status: " + (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE ? "OK" : GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)));
        new Exception("Stack trace").printStackTrace();
    }
}
