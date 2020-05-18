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

package dev.hexeption.newlogo.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.resource.ResourceReloadMonitor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * MixinSplashScreen
 *
 * @author Hexeption admin@hexeption.co.uk
 * @since 18/05/2020 - 03:49 pm
 */
@Mixin(SplashScreen.class)
@Environment(EnvType.CLIENT)
public abstract class MixinSplashScreen extends Overlay {


    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private boolean reloading;
    @Shadow
    @Final
    private ResourceReloadMonitor reloadMonitor;
    @Shadow
    private long prepareCompleteTime;
    @Shadow
    private long applyCompleteTime;
    @Shadow
    @Final
    private static Identifier LOGO;
    @Shadow
    private float progress;

    @Shadow
    @Final
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Shadow
    protected abstract void renderProgressBar(int minX, int minY, int maxX, int maxY, float progress);

    /**
     * @author New Loading Screen
     */
    @Overwrite
    public void render(int mouseX, int mouseY, float delta) {
        int i = this.client.getWindow().getScaledWidth();
        int j = this.client.getWindow().getScaledHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloading && (this.reloadMonitor.isPrepareStageComplete() || this.client.currentScreen != null) && this.prepareCompleteTime == -1L) {
            this.prepareCompleteTime = l;
        }

        float f = this.applyCompleteTime > -1L ? (float) (l - this.applyCompleteTime) / 1000.0F : -1.0F;
        float g = this.prepareCompleteTime > -1L ? (float) (l - this.prepareCompleteTime) / 500.0F : -1.0F;
        float o;
        int m;
        if (f >= 1.0F) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(0, 0, delta);
            }

            m = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(0, 0, i, j, 15675965 | m << 24);
            o = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            if (this.client.currentScreen != null && g < 1.0F) {
                this.client.currentScreen.render(mouseX, mouseY, delta);
            }

            m = MathHelper.ceil(MathHelper.clamp((double) g, 0.15D, 1.0D) * 255.0D);
            fill(0, 0, i, j, 15675965 | m << 24);
            o = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            fill(0, 0, i, j, -1);
            o = 1.0F;
        }

//        m = (this.client.getWindow().getScaledWidth() - 256) / 2;
//        int q = (this.client.getWindow().getScaledHeight() - 256) / 2;
//        this.client.getTextureManager().bindTexture(LOGO);
//        RenderSystem.enableBlend();
//        Color color = new Color(15675965);
//        RenderSystem.color4f(color.getRed(), color.getGreen(), color.getBlue(), o);
//        this.blit(m, q, 0, 0, 256, 256);
        float r = this.reloadMonitor.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + r * 0.050000012F, 0.0F, 1.0F);
        if (f < 1.0F) {
            this.renderProgressBar(i / 2 - 150, j / 4 * 3, i / 2 + 150, j / 4 * 3 + 10, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F) {
            this.client.setOverlay((Overlay) null);
        }

        if (this.applyCompleteTime == -1L && this.reloadMonitor.isApplyStageComplete() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reloadMonitor.throwExceptions();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var15) {
                this.exceptionHandler.accept(Optional.of(var15));
            }

            this.applyCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight());
            }
        }

    }
}
