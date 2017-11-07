package de.timgoll.facadingIndustry.items;

import de.timgoll.facadingIndustry.FacadingIndustry;
import de.timgoll.facadingIndustry.client.IHasModel;
import de.timgoll.facadingIndustry.init.ModRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemSpade;
import net.minecraftforge.client.model.ModelLoader;

public class ItemToolDemeraldShovel extends ItemSpade implements IHasModel {

    public ItemToolDemeraldShovel(String name) {
        super(ModRegistry.TM_DEMERALD);

        this.setRegistryName(name);
        this.setUnlocalizedName(FacadingIndustry.MODID + "." + name);
        this.setCreativeTab(ModRegistry.TAB);

        ModRegistry.ITEMS.add(this);
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

}