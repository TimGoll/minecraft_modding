package de.timgoll.facading.client.gui;

import de.timgoll.facading.container.ContainerFacadingbench;
import de.timgoll.facading.titleentities.TileBlockFacadingbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_FACADING_CONTAINER_ID = 0;

    private static Object OPENGUI;
    private static Object OPENCONTAINER;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        switch (ID) {
            case GUI_FACADING_CONTAINER_ID:
                OPENCONTAINER = new ContainerFacadingbench(player.inventory, (TileBlockFacadingbench) te);
                return OPENCONTAINER;
            default: return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

        switch (ID) {
            case GUI_FACADING_CONTAINER_ID:
                OPENGUI = new GuiFacadingbenchContainer(player.inventory, (TileBlockFacadingbench) te);
                return OPENGUI;
            default: return null;
        }
    }

    public static Object getOpenGui() {
        return OPENGUI;
    }

    public static Object getOpenContainer() {
        return OPENCONTAINER;
    }
}
