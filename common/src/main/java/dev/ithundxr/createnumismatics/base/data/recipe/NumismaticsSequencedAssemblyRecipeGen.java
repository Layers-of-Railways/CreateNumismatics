package dev.ithundxr.createnumismatics.base.data.recipe;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class NumismaticsSequencedAssemblyRecipeGen extends NumismaticsRecipeProvider {
  public NumismaticsSequencedAssemblyRecipeGen(PackOutput pOutput) {
    super(pOutput);
  }

  protected GeneratedRecipe create(String name, UnaryOperator<SequencedAssemblyRecipeBuilder> transform) {
    GeneratedRecipe generatedRecipe =
        c -> transform.apply(new SequencedAssemblyRecipeBuilder(Numismatics.asResource(name)))
            .build(c);
    all.add(generatedRecipe);
    return generatedRecipe;
  }

  @Override
  public @NotNull String getName() {
    return "Numismatics' Sequenced Assembly Recipes";
  }
}
