//package xyz.l7ssha.lushathings.screen;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.Tooltip;
//import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//import net.minecraft.client.renderer.GameRenderer;
//import net.minecraft.core.Direction;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Inventory;
//import net.neoforged.neoforge.network.PacketDistributor;
//import xyz.l7ssha.lushathings.lushathings;
//import xyz.l7ssha.lushathings.network.packet.CycleSideConfigPayload;
//import xyz.l7ssha.lushathings.network.packet.ToggleAutoIOPayload;
//import xyz.l7ssha.lushathings.screen.renderer.EnergyDisplayTooltipArea;
//import xyz.l7ssha.lushathings.util.MouseUtil;
//import xyz.l7ssha.lushathings.util.SideConfigType;
//
//import java.util.Optional;
//
//public class ReprocessorScreen extends AbstractContainerScreen<ReprocessorMenu> {
//    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_gui.png");
//    private static final ResourceLocation ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/arrow.png");
//
//    private EnergyDisplayTooltipArea energyInfoArea;
//
//    public ReprocessorScreen(ReprocessorMenu menu, Inventory playerInventory, Component title) {
//        super(menu, playerInventory, title);
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//
//        this.energyInfoArea = new EnergyDisplayTooltipArea(((width - imageWidth) / 2) + 164,
//                ((height - imageHeight) / 2 ) + 18, menu.blockEntity.getEnergyStorage(null), 4, 48);
//
//        // Add buttons for each side
//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;
//
//        // Item config buttons (Left side)
//        addSideButton(Direction.UP, x - 20, y + 20, SideConfigType.ITEM);
//        addSideButton(Direction.DOWN, x - 20, y + 40, SideConfigType.ITEM);
//        addSideButton(Direction.NORTH, x - 20, y + 60, SideConfigType.ITEM);
//        addSideButton(Direction.SOUTH, x - 20, y + 80, SideConfigType.ITEM);
//        addSideButton(Direction.WEST, x - 20, y + 100, SideConfigType.ITEM);
//        addSideButton(Direction.EAST, x - 20, y + 120, SideConfigType.ITEM);
//
//        // Energy config buttons (Right side)
//        int rightX = x + imageWidth;
//        addSideButton(Direction.UP, rightX, y + 20, SideConfigType.ENERGY);
//        addSideButton(Direction.DOWN, rightX, y + 40, SideConfigType.ENERGY);
//        addSideButton(Direction.NORTH, rightX, y + 60, SideConfigType.ENERGY);
//        addSideButton(Direction.SOUTH, rightX, y + 80, SideConfigType.ENERGY);
//        addSideButton(Direction.WEST, rightX, y + 100, SideConfigType.ENERGY);
//        addSideButton(Direction.EAST, rightX, y + 120, SideConfigType.ENERGY);
//
//        // Auto IO Buttons
//        this.addRenderableWidget(Button.builder(Component.literal("I"), (button) -> {
//            PacketDistributor.sendToServer(new ToggleAutoIOPayload(menu.blockEntity.getBlockPos(), true));
//        }).bounds(x - 20, y + 145, 20, 20).tooltip(Tooltip.create(Component.literal("Auto Pull"))).build());
//
//        this.addRenderableWidget(Button.builder(Component.literal("O"), (button) -> {
//            PacketDistributor.sendToServer(new ToggleAutoIOPayload(menu.blockEntity.getBlockPos(), false));
//        }).bounds(rightX, y + 145, 20, 20).tooltip(Tooltip.create(Component.literal("Auto Push"))).build());
//    }
//
//    private void addSideButton(Direction direction, int x, int y, SideConfigType type) {
//        this.addRenderableWidget(Button.builder(Component.literal(direction.getName().substring(0, 1).toUpperCase()), (button) -> {
//            PacketDistributor.sendToServer(new CycleSideConfigPayload(menu.blockEntity.getBlockPos(), direction, type));
//        }).bounds(x, y, 20, 20).build());
//    }
//
//    @Override
//    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
//
//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;
//
//        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
//
//        if (menu.isProcessing()) {
//            guiGraphics.blit(ARROW_TEXTURE, x + 73, y + 35, 0, 0, menu.getScaledProgressArrow(), 16, 24, 16);
//        }
//
//        energyInfoArea.render(guiGraphics);
//    }
//
//    @Override
//    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;
//
//        if(isMouseAboveArea(pMouseX, pMouseY, x, y, 166, 16, 4, 50)) {
//            guiGraphics.renderTooltip(this.font, energyInfoArea.getTooltips(),
//                    Optional.empty(), pMouseX - x, pMouseY - y);
//        }
//    }
//
//    @Override
//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
//        super.render(guiGraphics, mouseX, mouseY, partialTick);
//        this.renderTooltip(guiGraphics, mouseX, mouseY);
//
//        int x = (width - imageWidth) / 2;
//        int y = (height - imageHeight) / 2;
//        int btnX = x - 20;
//        int btnY = y + 20;
//
//        renderConfigState(guiGraphics, Direction.UP, btnX, btnY, SideConfigType.ITEM);
//        renderConfigState(guiGraphics, Direction.DOWN, btnX, btnY + 20, SideConfigType.ITEM);
//        renderConfigState(guiGraphics, Direction.NORTH, btnX, btnY + 40, SideConfigType.ITEM);
//        renderConfigState(guiGraphics, Direction.SOUTH, btnX, btnY + 60, SideConfigType.ITEM);
//        renderConfigState(guiGraphics, Direction.WEST, btnX, btnY + 80, SideConfigType.ITEM);
//        renderConfigState(guiGraphics, Direction.EAST, btnX, btnY + 100, SideConfigType.ITEM);
//
//        int rightX = x + imageWidth;
//        renderConfigState(guiGraphics, Direction.UP, rightX, btnY, SideConfigType.ENERGY);
//        renderConfigState(guiGraphics, Direction.DOWN, rightX, btnY + 20, SideConfigType.ENERGY);
//        renderConfigState(guiGraphics, Direction.NORTH, rightX, btnY + 40, SideConfigType.ENERGY);
//        renderConfigState(guiGraphics, Direction.SOUTH, rightX, btnY + 60, SideConfigType.ENERGY);
//        renderConfigState(guiGraphics, Direction.WEST, rightX, btnY + 80, SideConfigType.ENERGY);
//        renderConfigState(guiGraphics, Direction.EAST, rightX, btnY + 100, SideConfigType.ENERGY);
//
//        // Render Auto IO State
//        renderAutoIOState(guiGraphics, x - 20, y + 145, menu.isAutoPull());
//        renderAutoIOState(guiGraphics, rightX, y + 145, menu.isAutoPush());
//    }
//
//    private void renderConfigState(GuiGraphics guiGraphics, Direction direction, int x, int y, SideConfigType type) {
//        int config = menu.getSideConfig(type, direction.ordinal());
//        int color = switch (config) {
//            case 1 -> 0xFF0000FF; // Input - Blue
//            case 2 -> 0xFFFF0000; // Output - Red
//            case 3 -> 0xFF00FF00; // Both - Green
//            default -> 0xFF888888; // None - Gray
//        };
//
//        guiGraphics.fill(x, y, x + 20, y + 1, color); // Top
//        guiGraphics.fill(x, y + 19, x + 20, y + 20, color); // Bottom
//        guiGraphics.fill(x, y, x + 1, y + 20, color); // Left
//        guiGraphics.fill(x + 19, y, x + 20, y + 20, color); // Right
//    }
//
//    private void renderAutoIOState(GuiGraphics guiGraphics, int x, int y, boolean enabled) {
//        int color = enabled ? 0xFF00FF00 : 0xFFFF0000; // Green if enabled, Red if disabled
//
//        guiGraphics.fill(x, y, x + 20, y + 1, color); // Top
//        guiGraphics.fill(x, y + 19, x + 20, y + 20, color); // Bottom
//        guiGraphics.fill(x, y, x + 1, y + 20, color); // Left
//        guiGraphics.fill(x + 19, y, x + 20, y + 20, color); // Right
//    }
//
//    public static boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
//        return MouseUtil.isMouseOver(pMouseX, pMouseY, x + offsetX, y + offsetY, width, height);
//    }
//}
