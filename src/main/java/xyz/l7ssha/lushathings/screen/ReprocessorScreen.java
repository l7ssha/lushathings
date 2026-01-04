package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.screen.renderer.EnergyDisplayTooltipArea;
import xyz.l7ssha.lushathings.util.MouseUtil;

import java.util.Optional;

public class ReprocessorScreen extends AbstractContainerScreen<ReprocessorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_gui.png");
    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/arrow.png");

    private EnergyDisplayTooltipArea energyInfoArea;

    public ReprocessorScreen(ReprocessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 166,
                ((height - imageHeight) / 2 ) + 18, menu.blockEntity.getEnergyStorage(null), 2, 48);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isProcessing()) {
            guiGraphics.blit(ARROW_TEXTURE, x + 73, y + 35, 0, 0, menu.getScaledProgressArrow(), 16, 24, 16);
        }

        energyInfoArea.render(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 166, 16, 4, 50)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
                    Optional.empty(), pMouseX - x, pMouseY - y);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public static boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }
}
