package xyz.l7ssha.lushathings.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorInputOutputScreen extends AbstractContainerScreen<ReprocessorInputOutputMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_input_output_gui.png");
    private net.minecraft.client.gui.components.Button autoPullButton;
    private net.minecraft.client.gui.components.Button autoPushButton;

    public ReprocessorInputOutputScreen(ReprocessorInputOutputMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;

        this.autoPullButton = this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                getAutoPullText(),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                })
                .bounds(relX + 63, relY + 18, 50, 20)
                .build());

        this.autoPushButton = this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                getAutoPushText(),
                button -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
                    }
                })
                .bounds(relX + 63, relY + 40, 50, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.autoPullButton != null) {
            this.autoPullButton.setMessage(getAutoPullText());
        }
        if (this.autoPushButton != null) {
            this.autoPushButton.setMessage(getAutoPushText());
        }
    }

    private Component getAutoPullText() {
        boolean enabled = this.menu.isAutoPullEnabled();
        Component typeComp = Component.translatable("lushathings.screen.hatch.pull");
        Component stateComp = enabled ? Component.translatable("lushathings.screen.hatch.on") : Component.translatable("lushathings.screen.hatch.off");
        return Component.translatable("lushathings.screen.hatch.auto_btn", typeComp, stateComp);
    }

    private Component getAutoPushText() {
        boolean enabled = this.menu.isAutoPushEnabled();
        Component typeComp = Component.translatable("lushathings.screen.hatch.push");
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

        // Draw slot backgrounds if needed.
        // We reuse reprocessor_hatch_gui.png which has a 3x3 grid at 62, 17 (size 54x54)
        // We want to draw it at 8, 17 and 116, 17.
        guiGraphics.blit(TEXTURE, x + 8, y + 17, 62, 17, 54, 54);
        guiGraphics.blit(TEXTURE, x + 116, y + 17, 62, 17, 54, 54);
    }
}
