/*******************************************************************************
 * New Logo
 * Copyright (C) 2020  Hexeption (Keir Davis)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package dev.hexeption.newlogo.animatedTexture;

import dev.hexeption.newlogo.util.GifDecoder;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * ImageData
 *
 * @author Hexeption admin@hexeption.co.uk
 * @since 18/05/2020 - 04:47 pm
 */
public class ImageData {

    private static class Frame {

        private final ByteBuffer buffer;
        private final boolean hasAlpha;

        public Frame(ByteBuffer buffer, boolean hasAlpha) {
            this.buffer = buffer;
            this.hasAlpha = hasAlpha;
        }
    }

    private final int width;
    private final int height;
    private final Frame[] frames;
    private final long[] delay;
    private final long duration;

    public ImageData(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        frames = new Frame[]{loadFrom(image)};
        delay = new long[]{0};
        duration = 0;
    }

    public ImageData(GifDecoder decoder) {
        Dimension frameSize = decoder.getFrameSize();
        width = (int) frameSize.getWidth();
        height = (int) frameSize.getHeight();
        frames = new Frame[decoder.getFrameCount()];
        delay = new long[decoder.getFrameCount()];
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            frames[i] = loadFrom(decoder.getFrame(i));
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
    }

    private Frame loadFrom(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        boolean hasAlpha = false;
        if (image.getColorModel().hasAlpha()) {
            hasAlpha = Arrays.stream(pixels).anyMatch(pixel -> (pixel >> 24 & 0xFF) < 0xFF);
        }
        int bytesPerPixel = hasAlpha ? 4 : 3;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            if (hasAlpha) {
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();
        return new Frame(buffer, hasAlpha);
    }

    public int uploadFrame(int index) {
        if (index >= 0 && index < frames.length) {
            Frame frame = frames[index];
            if (frame != null) {
                frames[index] = null;
                return uploadFrame(frame.buffer, frame.hasAlpha, width, height);
            }
        }
        return -1;
    }

    private static int uploadFrame(ByteBuffer buffer, boolean hasAlpha, int width, int height) {
        int textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (!hasAlpha) {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        }

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        return textureID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Frame[] getFrames() {
        return frames;
    }

    public long[] getDelay() {
        return delay;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isAnimated() {
        return frames.length > 1;
    }

    public int getFrameCount() {
        return frames.length;
    }
}
