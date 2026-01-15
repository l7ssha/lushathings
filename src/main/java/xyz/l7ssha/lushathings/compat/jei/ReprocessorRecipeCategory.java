package xyz.l7ssha.lushathings.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

import java.util.Arrays;
import java.util.List;

public class ReprocessorRecipeCategory implements IRecipeCategory<ReprocessorRecipe> {
    public static final RecipeType<ReprocessorRecipe> RECIPE_TYPE = RecipeType.create(lushathings.MODID, "reprocessor", ReprocessorRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ReprocessorRecipeCategory(IGuiHelper helper) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(lushathings.MODID, "textures/gui/reprocessor_controller_gui.png");
        this.background = helper.createDrawable(texture, 5, 5, 171, 75);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(lushathings.REPROCESSOR_CONTROLLER_BLOCK.get()));
    }

    @Override
    public RecipeType<ReprocessorRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.lushathings.reprocessor_controller_block");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ReprocessorRecipe recipe, IFocusGroup focuses) {
        int x = 40;
        int y = 20;

        // Inputs
        for (int i = 0; i < recipe.inputs().size(); i++) {
            SizedIngredient input = recipe.inputs().get(i);
            List<ItemStack> stacks = Arrays.stream(input.ingredient().getItems())
                    .map(itemStack -> {
                        ItemStack copy = itemStack.copy();
                        copy.setCount(input.count());
                        return copy;
                    }).toList();

            builder.addSlot(RecipeIngredientRole.INPUT, x + (i * 18), y)
                    .addIngredients(VanillaTypes.ITEM_STACK, stacks);
        }

        // Outputs
        int outX = 100;

        builder.addSlot(RecipeIngredientRole.OUTPUT, outX, y)
                .addItemStack(recipe.output());

        if (!recipe.output2().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, outX + 18, y)
                    .addItemStack(recipe.output2());
        }
    }

    @Override
    public void draw(ReprocessorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw Energy Cost
        Component energyText = Component.literal(recipe.energyCost() + " FE");
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, energyText, 10, 60, 0xFF0000, false);

        // Draw Crafting Time
        Component timeText = Component.literal(recipe.craftingTime() + " ticks");
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, timeText, 100, 60, 0x404040, false);
    }
}
