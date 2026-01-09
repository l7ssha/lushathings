package xyz.l7ssha.lushathings.datagen.builder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import xyz.l7ssha.lushathings.recipe.ReprocessorRecipe;
import xyz.l7ssha.lushathings.recipe.util.SizedIngredient;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReprocessorRecipeBuilder implements RecipeBuilder {
    private final NonNullList<SizedIngredient> ingredients = NonNullList.create();
    private final ItemStack result;
    private final ItemStack result2;
    private final int craftingTime;
    private final int energyCost;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public ReprocessorRecipeBuilder(ItemStack result, ItemStack result2, int craftingTime, int energyCost) {
        this.result = result;
        this.result2 = result2;
        this.craftingTime = craftingTime;
        this.energyCost = energyCost;
    }

    // Static helper to make provider code cleaner
    public static ReprocessorRecipeBuilder create(ItemStack result, int craftingTime, int energyCost) {
        return new ReprocessorRecipeBuilder(result, ItemStack.EMPTY, craftingTime, energyCost);
    }

    public static ReprocessorRecipeBuilder create(ItemStack result, ItemStack result2, int craftingTime, int energyCost) {
        return new ReprocessorRecipeBuilder(result, result2, craftingTime, energyCost);
    }

    public ReprocessorRecipeBuilder addInput(Ingredient ingredient, int count) {
        this.ingredients.add(new SizedIngredient(ingredient, count));
        return this;
    }

    @Override
    public ReprocessorRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public ReprocessorRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        Advancement.Builder advancementBuilder = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        ReprocessorRecipe recipe = new ReprocessorRecipe(this.ingredients, this.result, this.result2, this.craftingTime, this.energyCost);
        output.accept(id, recipe, advancementBuilder.build(id.withPrefix("recipes/")));
    }
}
