package de.timgoll.facading.container;

import de.timgoll.facading.init.ModRegistry;
import de.timgoll.facading.misc.CustomRecipeRegistry;
import de.timgoll.facading.titleentities.TileBlockMachinePress;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class ContainerMachinePress extends ContainerMachineGeneric {

    public ContainerMachinePress(InventoryPlayer inventoryPlayer, TileBlockMachinePress tileBlockMachinePress) {
        super(inventoryPlayer, tileBlockMachinePress);
    }

    @Override
    ArrayList<Item> getAllowedItemsInput() {
        ArrayList<Item> allowedItems = new ArrayList<>();

        for (ArrayList<ArrayList<ItemStack>> inputStackList : CustomRecipeRegistry.getInputList("press")) {
            for (ItemStack inputStack : inputStackList.get(0))
                allowedItems.add( inputStack.getItem() );
        }

        return allowedItems;
    }

    @Override
    ArrayList<Item> getAllowedItemsTools() {
        ArrayList<Item> allowedItems = new ArrayList<>();

        allowedItems.add(ModRegistry.ITEM_IRONPRESS);
        allowedItems.add(ModRegistry.ITEM_DIAMONDPRESS);
        allowedItems.add(ModRegistry.ITEM_NETHERPRESS);

        return allowedItems;
    }
}
