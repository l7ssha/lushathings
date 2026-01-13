package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.screen.renderer.EnergyDisplayTooltipArea;
import xyz.l7ssha.lushathings.util.MouseUtil;

import java.util.Optional;

public class ReprocessorEnergyInputScreen extends AbstractContainerScreen<ReprocessorEnergyInputMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_energy_input_gui.png");

    private EnergyDisplayTooltipArea energyInfoArea;

    public ReprocessorEnergyInputScreen(ReprocessorEnergyInputMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 164,
                ((height - imageHeight) / 2 ) + 18, menu.getEnergyStorage(), 4, 48);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        energyInfoArea.render(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        if (isMouseAboveArea(mouseX, mouseY, x, y, 166, 16, 4, 50)) {
            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(), Optional.empty(), mouseX - x, mouseY - y);
        }
    }

    public static boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
    }
}

