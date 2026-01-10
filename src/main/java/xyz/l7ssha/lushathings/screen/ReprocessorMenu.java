//package xyz.l7ssha.lushathings.screen;
//
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.entity.player.Inventory;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.*;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.neoforged.neoforge.items.SlotItemHandler;
//import xyz.l7ssha.lushathings.lushathings;
//import xyz.l7ssha.lushathings.screen.util.LushaAbstractContainerMenu;
//import xyz.l7ssha.lushathings.util.SideConfigType;
//
//public class ReprocessorMenu extends LushaAbstractContainerMenu {
//    public final ReprocessorBlockEntity blockEntity;
//    private final Level level;
//    private final ContainerData data;
//
//    public ReprocessorMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
//        this(containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(16));
//    }
//
//    public ReprocessorMenu(int containerId, Inventory inventory, BlockEntity blockEntity, ContainerData data) {
//        super(lushathings.REPROCESSOR_MENU.get(), containerId);
//
//        this.blockEntity = (ReprocessorBlockEntity) blockEntity;
//        this.level = inventory.player.level();
//        this.data = data;
//
//        addPlayerInventory(inventory);
//        addPlayerHotbar(inventory);
//
//        this.addSlot(new SlotItemHandler(this.blockEntity.itemHandler, 0, 54, 21));
//        this.addSlot(new SlotItemHandler(this.blockEntity.itemHandler, 1, 54, 48));
//        this.addSlot(new SlotItemHandler(this.blockEntity.itemHandler, 2, 104, 26));
//        this.addSlot(new SlotItemHandler(this.blockEntity.itemHandler, 3, 104, 43));
//
//        addDataSlots(this.data);
//    }
//
//    public boolean isAutoPull() {
//        return this.data.get(14) == 1;
//    }
//
//    public boolean isAutoPush() {
//        return this.data.get(15) == 1;
//    }
//
//    public boolean isProcessing() {
//        return data.get(0) > 0;
//    }
//
//    public int getScaledProgressArrow() {
//        int progress = this.data.get(0);
//        int maxProgress = this.data.get(1);
//
//        int arrowPixelSize = 24;
//
//        if (maxProgress == 0 || progress == 0) {
//            return 0;
//        }
//
//        return progress * arrowPixelSize / maxProgress;
//    }
//
//    public int getSideConfig(SideConfigType type, int directionOrdinal) {
//        int baseIndex = type == SideConfigType.ITEM ? 2 : 8;
//        return this.data.get(baseIndex + directionOrdinal);
//    }
//
//    @Override
//    public boolean stillValid(Player player) {
//        return stillValid(ContainerLevelAccess.create(player.level(), blockEntity.getBlockPos()), player, lushathings.REPROCESSOR_BLOCK.get());
//    }
//
//    private void addPlayerInventory(Inventory playerInventory) {
//        for (int i = 0; i < 3; ++i) {
//            for (int l = 0; l < 9; ++l) {
//                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
//            }
//        }
//    }
//
//    private void addPlayerHotbar(Inventory playerInventory) {
//        for (int i = 0; i < 9; ++i) {
//            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
//        }
//    }
//}
