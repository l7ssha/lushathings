package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;

/**
 * Controller UI: only displays energy + current recipe/progress text.
 */
public class ReprocessorControllerScreen extends AbstractContainerScreen<ReprocessorControllerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_controller_gui.png");


    public ReprocessorControllerScreen(ReprocessorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 2 lines of text: current recipe + progress
        guiGraphics.drawString(this.font,
                Component.literal("Recipe: " + menu.blockEntity.getCurrentRecipeId()),
                8, 20, 0x404040, false);
        guiGraphics.drawString(this.font,
                Component.literal("Progress: " + menu.getProgress() + "/" + menu.getMaxProgress()),
                8, 32, 0x404040, false);

        guiGraphics.drawString(this.font,
                Component.literal("Status: " + statusText(menu.getStatus())),
                8, 44, 0x404040, false);

    }

    private static String statusText(int status) {
        return switch (status) {
            case ReprocessorControllerBlockEntity.STATUS_OK -> "OK";
            case ReprocessorControllerBlockEntity.STATUS_NO_INPUT_HATCH -> "No input hatch";
            case ReprocessorControllerBlockEntity.STATUS_NO_OUTPUT_HATCH -> "No output hatch";
            case ReprocessorControllerBlockEntity.STATUS_NO_RECIPE -> "No matching recipe";
            case ReprocessorControllerBlockEntity.STATUS_NO_ENERGY -> "Not enough energy";
            case ReprocessorControllerBlockEntity.STATUS_OUTPUT_FULL -> "Output full";
            default -> "Unknown";
        };
    }
}
