package com.mruilab.evoplayer.beauty;

public class RenderBean {
    private byte[] buffer;
    private int width, height;

    public RenderBean(byte[] buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
