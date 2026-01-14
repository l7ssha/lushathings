package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.blockentity.ReprocessorInputBlockEntity;
import xyz.l7ssha.lushathings.blockentity.ReprocessorOutputBlockEntity;

public class ReprocessorHatchScreen extends AbstractContainerScreen<ReprocessorHatchMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_hatch_gui.png");
    private net.minecraft.client.gui.components.Button autoButton;

    public ReprocessorHatchScreen(ReprocessorHatchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        this.autoButton = this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                getAutoButtonText(),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                })
                .bounds(relX + 120, relY + 18, 50, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.autoButton != null) {
            this.autoButton.setMessage(getAutoButtonText());
        }
    }

    private Component getAutoButtonText() {
        boolean enabled = this.menu.isAutoIOEnabled();
        boolean isPull = (this.menu.blockEntity instanceof ReprocessorInputBlockEntity);
        Component typeComp = isPull ? Component.translatable("lushathings.screen.hatch.pull") : Component.translatable("lushathings.screen.hatch.push");
        Component stateComp = enabled ? Component.translatable("lushathings.screen.hatch.on") : Component.translatable("lushathings.screen.hatch.off");
        return Component.translatable("lushathings.screen.hatch.auto_btn", typeComp, stateComp);
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
}

