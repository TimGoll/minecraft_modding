package de.timgoll.facading.misc;

import de.timgoll.facading.init.ModRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RecipeHandler {

    public void registerRecipes() {
        addHammer();
        addToolHolder();
        addWaterMill();

        addFlintWoodCutter();
        addIronToothSaw();
        addIronJapanSaw();
        addDiamondCicularSaw();

        addMachineframe();
        addFacadingbench();

    }

    /* ** RECIPES ** */
    private void addHammer() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("hammer_iron"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_HAMMER),
            "III",
            " SI",
            "W  ",
            'I', "ingotIron",
            'S', "stickWood",
            'W', "plankWood"
        );
        GameRegistry.addShapedRecipe(
            new ResourceLocation("hammer_copper"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_HAMMER),
            "III",
            " SI",
            "W  ",
            'I', "ingotCopper",
            'S', "stickWood",
            'W', "plankWood"
        );
    }

    private void addToolHolder() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("toolholder"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_TOOLHOLDER),
            " WW",
            "SWW",
            "S  ",
            'W', "plankWood",
            'S', "stickWood"
        );
    }

    private void addWaterMill() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("watermill_iron"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_WATERMILL),
            " W ",
            "WIW",
            " W ",
            'W', "plankWood",
            'I', "ingotIron"
        );

        GameRegistry.addShapedRecipe(
            new ResourceLocation("watermill_copper"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_WATERMILL),
            " W ",
            "WIW",
            " W ",
            'W', "plankWood",
            'I', "ingotCopper"
        );
    }



    private void addFlintWoodCutter() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("flintwoodcutter"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_FLINTWOODCUTTER),
            "WWW",
            "FFF",
            "   ",
            'W', "plankWood",
            'F', Items.FLINT
        );
    }

    private void addIronToothSaw() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("irontoothsaw_iron"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_IRONTOOTHSAW),
            "  S",
            "IIS",
            "III",
            'I', "ingotIron",
            'S', "stickWood"
        );

        GameRegistry.addShapedRecipe(
            new ResourceLocation("irontoothsaw_copper"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_IRONTOOTHSAW),
            "  S",
            "IIS",
            "III",
            'I', "ingotCopper",
            'S', "stickWood"
        );
    }

    private void addIronJapanSaw() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("ironjapansaw_iron"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_IRONJAPANSAW),
            "SSS",
            "II ",
            "   ",
            'I', "blockIron",
            'S', "stickWood"
        );

        GameRegistry.addShapedRecipe(
            new ResourceLocation("ironjapansaw_copper"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_IRONJAPANSAW),
            "SSS",
            "II ",
            "   ",
            'I', "blockCopper",
            'S', "stickWood"
        );
    }

    private void addDiamondCicularSaw() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("diamondcicularsaw_iron"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_DIAMONDCIRCULARSAW),
            " D ",
            "DID",
            " D ",
            'I', "ingotIron",
            'D', "gemDiamond"
        );

        GameRegistry.addShapedRecipe(
            new ResourceLocation("diamondcicularsaw_copper"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.ITEM_DIAMONDCIRCULARSAW),
            " D ",
            "DID",
            " D ",
            'I', "ingotCopper",
            'D', "gemDiamond"
        );
    }






    private void addMachineframe() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("machineframe"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.BLOCK_MACHINEFRAME),
            "RRR",
            "R R",
            "RRR",
            'R', ModRegistry.ITEM_REINFORCEMENTBUNDLE
        );
    }

    private void addFacadingbench() {
        GameRegistry.addShapedRecipe(
            new ResourceLocation("facadingbench"),
            new ResourceLocation("facading"),
            new ItemStack(ModRegistry.BLOCK_FACADINGBENCH),
            "WCW",
            "#MT",
            "SSS",
            'C', Blocks.CRAFTING_TABLE,
            'M', ModRegistry.BLOCK_MACHINEFRAME,
            '#', ModRegistry.ITEM_WATERMILL,
            'T', ModRegistry.ITEM_TOOLHOLDER,
            'W', "plankWood",
            'S', "stone"
        );
    }
}
