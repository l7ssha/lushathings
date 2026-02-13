package xyz.l7ssha.lushathings.blockentity;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.blocks.ReprocessorControllerBlock;
import xyz.l7ssha.lushathings.blocks.ReprocessorMultiblock;
import xyz.l7ssha.lushathings.lushathings;

public class ReprocessorCCAdapterBlockEntity extends BlockEntity {
  private final ReprocessorPeripheral peripheral = new ReprocessorPeripheral();

  public ReprocessorCCAdapterBlockEntity(BlockPos pos, BlockState blockState) {
    super(lushathings.REPROCESSOR_CC_ADAPTER_BLOCK_ENTITY.get(), pos, blockState);
  }

  public IPeripheral getPeripheral() {
    return peripheral;
  }

  @Nullable
  private ReprocessorControllerBlockEntity findController() {
    if (level == null) return null;

    for (BlockPos checkPos :
        BlockPos.betweenClosed(worldPosition.offset(-2, -2, -2), worldPosition.offset(2, 2, 2))) {
      BlockState checkState = level.getBlockState(checkPos);

      if (checkState.getBlock() instanceof ReprocessorControllerBlock) {
        if (checkState.getValue(ReprocessorMultiblock.MUTLIBLOCK_FORMED)) {
          if (level.getBlockEntity(checkPos)
              instanceof ReprocessorControllerBlockEntity controller) {
            return controller;
          }
        }
      }
    }

    return null;
  }

  public class ReprocessorPeripheral implements IPeripheral {
    @Override
    public String getType() {
      return "reprocessor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
      return other == this;
    }

    /**
     * Check if the adapter is part of a formed multiblock.
     *
     * @return true if multiblock is formed
     */
    @LuaFunction
    public final boolean isFormed() {
      return getBlockState().getValue(ReprocessorMultiblock.MUTLIBLOCK_FORMED);
    }

    /**
     * Get the current processing progress (0-100).
     *
     * @return progress percentage or -1 if not formed
     */
    @LuaFunction
    public final int getProgress() {
      var controller = findController();
      if (controller == null) return -1;

      var data = controller.data;
      int progress = data.get(0);
      int maxProgress = data.get(1);

      if (maxProgress <= 0) return 0;
      return (int) ((progress / (float) maxProgress) * 100);
    }

    /**
     * Get the raw progress value in ticks.
     *
     * @return progress in ticks or -1 if not formed
     */
    @LuaFunction
    public final int getProgressTicks() {
      var controller = findController();
      if (controller == null) return -1;

      return controller.data.get(0);
    }

    /**
     * Get the maximum progress value in ticks for the current recipe.
     *
     * @return max progress in ticks or -1 if not formed
     */
    @LuaFunction
    public final int getMaxProgressTicks() {
      var controller = findController();
      if (controller == null) return -1;

      return controller.data.get(1);
    }

    /**
     * Get the current status of the reprocessor.
     *
     * @return status string: "ok", "no_input_hatch", "no_output_hatch", "no_recipe", "no_energy",
     *     "output_full", or "not_formed"
     */
    @LuaFunction
    public final String getStatus() {
      var controller = findController();
      if (controller == null) return "not_formed";

      int status = controller.data.get(2);
      return switch (status) {
        case ReprocessorControllerBlockEntity.STATUS_OK -> "ok";
        case ReprocessorControllerBlockEntity.STATUS_NO_INPUT_HATCH -> "no_input_hatch";
        case ReprocessorControllerBlockEntity.STATUS_NO_OUTPUT_HATCH -> "no_output_hatch";
        case ReprocessorControllerBlockEntity.STATUS_NO_RECIPE -> "no_recipe";
        case ReprocessorControllerBlockEntity.STATUS_NO_ENERGY -> "no_energy";
        case ReprocessorControllerBlockEntity.STATUS_OUTPUT_FULL -> "output_full";
        default -> "unknown";
      };
    }

    /**
     * Get the status code of the reprocessor (numeric value).
     *
     * @return status code: 0=ok, 1=no_input_hatch, 2=no_output_hatch, 3=no_recipe, 4=no_energy,
     *     5=output_full, -1=not_formed
     */
    @LuaFunction
    public final int getStatusCode() {
      var controller = findController();
      if (controller == null) return -1;

      return controller.data.get(2);
    }

    /**
     * Get the energy cost per tick for the current recipe.
     *
     * @return energy cost in FE/tick or -1 if not formed
     */
    @LuaFunction
    public final int getEnergyCostPerTick() {
      var controller = findController();
      if (controller == null) return -1;

      return controller.data.get(3);
    }

    /**
     * Get the name of the current recipe's output item.
     *
     * @return output item name or empty string if no recipe/not formed
     */
    @LuaFunction
    public final String getCurrentRecipeOutput() {
      var controller = findController();
      if (controller == null) return "";

      return controller.getCurrentRecipeOutputName();
    }

    /**
     * Check if the reprocessor is actively processing a recipe.
     *
     * @return true if processing
     */
    @LuaFunction
    public final boolean isProcessing() {
      var controller = findController();
      if (controller == null) return false;

      return controller.data.get(2) == ReprocessorControllerBlockEntity.STATUS_OK
          && controller.data.get(0) > 0;
    }

    /**
     * Get all reprocessor information as a table.
     *
     * @return table with all reprocessor data
     */
    @LuaFunction
    public final Map<String, Object> getInfo() {
      Map<String, Object> info = new HashMap<>();

      info.put("formed", isFormed());

      var controller = findController();
      if (controller == null) {
        info.put("progress", -1);
        info.put("progressTicks", -1);
        info.put("maxProgressTicks", -1);
        info.put("status", "not_formed");
        info.put("statusCode", -1);
        info.put("energyCostPerTick", -1);
        info.put("currentRecipeOutput", "");
        info.put("processing", false);
      } else {
        int progress = controller.data.get(0);
        int maxProgress = controller.data.get(1);
        int statusCode = controller.data.get(2);

        info.put("progress", maxProgress > 0 ? (int) ((progress / (float) maxProgress) * 100) : 0);
        info.put("progressTicks", progress);
        info.put("maxProgressTicks", maxProgress);
        info.put("status", getStatus());
        info.put("statusCode", statusCode);
        info.put("energyCostPerTick", controller.data.get(3));
        info.put("currentRecipeOutput", controller.getCurrentRecipeOutputName());
        info.put(
            "processing", statusCode == ReprocessorControllerBlockEntity.STATUS_OK && progress > 0);
      }

      return info;
    }
  }
}
