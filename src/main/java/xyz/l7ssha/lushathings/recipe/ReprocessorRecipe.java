package xyz.l7ssha.lushathings.recipe;

import com.mojang.serialization.Codec;
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

public record ReprocessorRecipe(NonNullList<SizedIngredient> inputs, ItemStack output, ItemStack output2, int craftingTime, int energyCost) implements Recipe<ReprocessorRecipeInput> {
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

        if (inputs.size() == 1) {
            ItemStack slot1 = reprocessorRecipeInput.getItem(0);
            ItemStack slot2 = reprocessorRecipeInput.getItem(1);

            return inputs.getFirst().test(slot1) && slot2.isEmpty();
        } else if (inputs.size() == 2) {
             ItemStack slot1 = reprocessorRecipeInput.getItem(0);
             ItemStack slot2 = reprocessorRecipeInput.getItem(1);

             return inputs.getFirst().test(slot1) && inputs.get(1).test(slot2);
        }

        return false;
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
        return output != null ? output : ItemStack.EMPTY;
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
                ItemStack.CODEC.fieldOf("result").forGetter(ReprocessorRecipe::output),
                ItemStack.CODEC.optionalFieldOf("result_aux", ItemStack.EMPTY).forGetter(ReprocessorRecipe::output2),
                Codec.INT.fieldOf("crafting_time").forGetter(ReprocessorRecipe::craftingTime),
                Codec.INT.fieldOf("energy_cost").forGetter(ReprocessorRecipe::energyCost)
        ).apply(inst, (ingredients, result, result2, craftingTime, energyCost) -> {
            NonNullList<SizedIngredient> inputs = NonNullList.create();
            inputs.addAll(ingredients);
            return new ReprocessorRecipe(inputs, result, result2, craftingTime, energyCost);
        }));

        public static final StreamCodec<RegistryFriendlyByteBuf, ReprocessorRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
                ReprocessorRecipe::inputs,
                ItemStack.STREAM_CODEC,
                ReprocessorRecipe::output,
                ItemStack.OPTIONAL_STREAM_CODEC,
                ReprocessorRecipe::output2,
                ByteBufCodecs.INT,
                ReprocessorRecipe::craftingTime,
                ByteBufCodecs.INT,
                ReprocessorRecipe::energyCost,
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
