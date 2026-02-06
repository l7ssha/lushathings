package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.blockentity.ReprocessorControllerBlockEntity;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorControllerScreen extends AbstractContainerScreen<ReprocessorControllerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            lushathings.MODID,
            "textures/gui/reprocessor_controller_gui.png"
    );

    public ReprocessorControllerScreen(ReprocessorControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
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
        String recipeName = menu.blockEntity.getCurrentRecipeOutputName();
        Component recipeComp;
        if (recipeName.isEmpty()) {
            recipeComp = Component.translatable("lushathings.screen.reprocessor.recipe.none");
        } else {
            recipeComp = Component.literal(recipeName);
        }

        guiGraphics.drawString(this.font,
                Component.translatable("lushathings.screen.reprocessor.recipe", recipeComp),
                8, 20, 0x404040, false);
        guiGraphics.drawString(this.font,
                Component.translatable("lushathings.screen.reprocessor.progress", menu.getProgress(), menu.getMaxProgress()),
                8, 32, 0x404040, false);

        guiGraphics.drawString(this.font,
                Component.translatable("lushathings.screen.reprocessor.status", statusText(menu.getStatus())),
                8, 44, 0x404040, false);

        guiGraphics.drawString(this.font,
                Component.translatable("lushathings.screen.reprocessor.energy_usage", menu.getEnergyUsage()),
                8, 56, 0x404040, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private static Component statusText(int status) {
        return switch (status) {
            case ReprocessorControllerBlockEntity.STATUS_OK -> Component.translatable("lushathings.screen.reprocessor.status.ok");
            case ReprocessorControllerBlockEntity.STATUS_NO_INPUT_HATCH -> Component.translatable("lushathings.screen.reprocessor.status.no_input");
            case ReprocessorControllerBlockEntity.STATUS_NO_OUTPUT_HATCH -> Component.translatable("lushathings.screen.reprocessor.status.no_output");
            case ReprocessorControllerBlockEntity.STATUS_NO_RECIPE -> Component.translatable("lushathings.screen.reprocessor.status.no_recipe");
            case ReprocessorControllerBlockEntity.STATUS_NO_ENERGY -> Component.translatable("lushathings.screen.reprocessor.status.no_energy");
            case ReprocessorControllerBlockEntity.STATUS_OUTPUT_FULL -> Component.translatable("lushathings.screen.reprocessor.status.output_full");
            default -> Component.translatable("lushathings.screen.reprocessor.status.unknown");
        };
    }
}
