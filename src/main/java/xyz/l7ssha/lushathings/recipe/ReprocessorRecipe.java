package xyz.l7ssha.lushathings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

public record ReprocessorRecipe(NonNullList<SizedIngredient> inputs, ItemStack output) implements Recipe<ReprocessorRecipeInput> {
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient input : inputs) {
            list.add(input.ingredient());
        }

        return list;
    }

    @Override
    public boolean matches(ReprocessorRecipeInput reprocessorRecipeInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        if (reprocessorRecipeInput.size() < inputs.size()) {
            return false;
        }

        for (int i = 0; i < inputs.size(); i++) {
            SizedIngredient required = inputs.get(i);
            ItemStack provided = reprocessorRecipeInput.getItem(i);

            if (!required.test(provided)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(ReprocessorRecipeInput reprocessorRecipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return lushathings.REPROCESSOR_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return lushathings.REPROCESSOR_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ReprocessorRecipe> {
        public static final MapCodec<ReprocessorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(ReprocessorRecipe::inputs),
                ItemStack.CODEC.fieldOf("result").forGetter(ReprocessorRecipe::output)
        ).apply(inst, (ingredients, result) -> {
            NonNullList<SizedIngredient> inputs = NonNullList.create();
            inputs.addAll(ingredients);
            return new ReprocessorRecipe(inputs, result);
        }));

        public static final StreamCodec<RegistryFriendlyByteBuf, ReprocessorRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
                ReprocessorRecipe::inputs,
                ItemStack.STREAM_CODEC,
                ReprocessorRecipe::output,
                ReprocessorRecipe::new
        );

        @Override
        public MapCodec<ReprocessorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReprocessorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
