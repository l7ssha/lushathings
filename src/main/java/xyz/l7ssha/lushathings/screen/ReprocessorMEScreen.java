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

public class ReprocessorMEScreen extends AbstractContainerScreen<ReprocessorMEMenu> {
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(
          lushathings.MODID, "textures/gui/reprocessor_me_hatch_gui.png");

  private Button patternsButton;
  private Button powerButton;

  public ReprocessorMEScreen(ReprocessorMEMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    this.imageWidth = 176;
    this.imageHeight = 166;
  }

  @Override
  protected void init() {
    super.init();

    int relX = (this.width - this.imageWidth) / 2;
    int relY = (this.height - this.imageHeight) / 2;

    this.patternsButton =
        this.addRenderableWidget(
            Button.builder(
                    getPatternsButtonText(),
                    button -> {
                      if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId, 0);
                      }
                    })
                .bounds(relX + 10, relY + 20, 156, 20)
                .build());

    this.powerButton =
        this.addRenderableWidget(
            Button.builder(
                    getPowerButtonText(),
                    button -> {
                      if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId, 1);
                      }
                    })
                .bounds(relX + 10, relY + 50, 156, 20)
                .build());
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    if (this.patternsButton != null) {
      this.patternsButton.setMessage(getPatternsButtonText());
    }

    if (this.powerButton != null) {
      this.powerButton.setMessage(getPowerButtonText());
    }
  }

  private Component getPatternsButtonText() {
    Component stateComp =
        this.menu.isProvidingBuiltinPatterns()
            ? Component.translatable("lushathings.screen.me.on")
            : Component.translatable("lushathings.screen.me.off");
    return Component.translatable("lushathings.screen.me.patterns_btn", stateComp);
  }

  private Component getPowerButtonText() {
    Component stateComp =
        this.menu.isAllowingNetworkPower()
            ? Component.translatable("lushathings.screen.me.on")
            : Component.translatable("lushathings.screen.me.off");
    return Component.translatable("lushathings.screen.me.power_btn", stateComp);
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
