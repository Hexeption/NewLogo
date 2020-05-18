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

/**
 * AnimatedTexture
 *
 * @author Hexeption admin@hexeption.co.uk
 * @since 18/05/2020 - 04:45 pm
 */
public class AnimatedTexture extends Texture {

    private final int[] textureIDs;
    private final long[] delay;
    private final long duration;

    private int completedFrames;
    private ImageData imageData;

    public AnimatedTexture(ImageData image) {
        super(image.getWidth(), image.getHeight());
        imageData = image;
        textureIDs = new int[image.getFrameCount()];
        delay = image.getDelay();
        duration = image.getDuration();
        for (int i = 0; i < textureIDs.length; i++) {
            textureIDs[i] = -1;
        }
    }

    @Override
    public void tick() {
        if (imageData != null) {
            long startTime = System.currentTimeMillis();
            int index = 0;
            while (completedFrames < textureIDs.length && index < textureIDs.length && System.currentTimeMillis() - startTime < 10) {
                while (textureIDs[index] != -1 && index < textureIDs.length - 1) {
                    index++;
                }
                if (textureIDs[index] == -1) {
                    textureIDs[index] = uploadFrame(index);
                }
            }
        }
    }

    @Override
    public int getTextureID() {
        long time = duration > 0 ? System.currentTimeMillis() % duration : 0;
        int index = 0;
        for (int i = 0; i < delay.length; i++) {
            if (delay[i] >= time) {
                index = i;
                break;
            }
        }
        return textureIDs[index];
    }

    private int uploadFrame(int index) {
        int id;
        id = imageData.uploadFrame(index);
        textureIDs[index] = id;
        if (++completedFrames >= imageData.getFrameCount()) {
            imageData = null;
        }
        return id;
    }

}
