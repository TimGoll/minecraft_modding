package de.timgoll.facading.titleentities;

import de.timgoll.facading.blocks.BlockMachineBase;
import de.timgoll.facading.items.ItemUpgradeBase;
import de.timgoll.facading.misc.CustomRecipeRegistry;
import de.timgoll.facading.misc.EnumHandler;
import de.timgoll.facading.network.PacketHandler;
import de.timgoll.facading.network.packets.PackedGuiFinishedProduction;
import de.timgoll.facading.network.packets.PacketGuiIsPowered;
import de.timgoll.facading.network.packets.PacketGuiStartedDisassembly;
import de.timgoll.facading.network.packets.PacketGuiStartedProduction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileBlockMachineBase extends TileEntity implements ITickable {

    //NBT data
    ItemStackHandler inventory;
    boolean isPowered               = false;
    boolean isProducing             = false;
    boolean isDisassembling         = false;
    int outputBlocks_indexProducing = 0;
    int outputBlocks_amount         = 0;
    int elapsedTicksProducing       = 0;
    int elapsedTicksDisassembling   = 0;
    int tickMultiplier              = 0;
    //NBT data end


    int disassembleTicks            = 40; //TODO confing value

    ArrayList<Integer> outputSlots      = new ArrayList<>();
    ArrayList<Integer> inputSlots       = new ArrayList<>();
    ArrayList<Integer> disassembleSlots = new ArrayList<>();
    ArrayList<Integer> upgradeSlot      = new ArrayList<>();

    //cache the recipes for every type
    ArrayList<ItemStack> outputStack                       = new ArrayList<>();
    ArrayList<ArrayList<ArrayList<ItemStack>>> inputStacks = new ArrayList<>();
    ArrayList<Integer> productionTime                      = new ArrayList<>();

    ArrayList<CustomRecipeRegistry.MachineRecipe> recipeList = new ArrayList<>();

    private int disassembleId = 0;
    boolean hasProduction = false;
    boolean hasDisassembly = false;

    TileBlockMachineBase(String machinetype) {
        outputStack    = CustomRecipeRegistry.getOutputList(machinetype);
        inputStacks    = CustomRecipeRegistry.getInputList(machinetype);
        productionTime = CustomRecipeRegistry.getProductionTimeList(machinetype);
    }


    public void setIsPowered(boolean powered) {
        this.isPowered = powered;

        sendMachineIsPoweredMessage();

        BlockMachineBase.setState(this.world, this.pos);
    }

    public boolean getIsPowered() {
        return this.isPowered;
    }

    public boolean getIsWorking() {
        return isProducing || isDisassembling;
    }

    public boolean getIsDisassembling() {
        return isDisassembling;
    }

    public boolean getIsProducing() {
        return isProducing;
    }

    public Enum getType() {
        if (getIsWorking() && isPowered)
            return EnumHandler.MachineStates.WORKING;
        if (isPowered)
            return EnumHandler.MachineStates.POWERED;

        return EnumHandler.MachineStates.DEFAULT;
    }

    public int getOutputBlocks_amount() {
        return outputBlocks_amount;
    }

    public void addProduction(int outputBlocks_index) {
        if (outputBlocks_amount == 0 || outputBlocks_indexProducing == outputBlocks_index) {
            outputBlocks_amount++;
            outputBlocks_amount = (outputBlocks_amount > 99) ? 99 : outputBlocks_amount; //limit to 99
            outputBlocks_indexProducing = outputBlocks_index;
        }
    }

    public int getOutputBlocks_indexProducing() {
        return outputBlocks_indexProducing;
    }

    public int getProductionTicks() {
        if (outputStack.size() == 0)
            return 0;

        if (tickMultiplier == 0)
            return productionTime.get( outputBlocks_indexProducing );

        return productionTime.get( outputBlocks_indexProducing ) / tickMultiplier;
    }

    public int getElapsedTicksProducing() {
        return this.elapsedTicksProducing;
    }

    public int getDisassembleTicks() {
        return disassembleTicks;
    }

    public int getElapsedDisassembleTicks() {
        return elapsedTicksDisassembling;
    }



























    @Override
    public void update() {
        if (world.isRemote)
            return;

        if (!isPowered)
            return;

        //PRODUCTION CODE
        if (hasProduction && ( isProducing || checkMachineStateForProduction() ) ) {
            if (elapsedTicksProducing == 0) { //new production started
                //get production speed multiplier
                tickMultiplier = 1;
                if ( !inventory.getStackInSlot( upgradeSlot.get(0) ).isEmpty() )
                    tickMultiplier = ( (ItemUpgradeBase) inventory.getStackInSlot( upgradeSlot.get(0) ).getItem() ).getMultiplier();

                isProducing = true;
                extractNeededInput(
                        inputStacks.get(outputBlocks_indexProducing),
                        inputSlots
                );
                sendStartedProductionMessage(
                        productionTime.get(outputBlocks_indexProducing) / tickMultiplier
                );
                BlockMachineBase.setState(this.world, this.pos);
            }

            elapsedTicksProducing++;

            if ( elapsedTicksProducing >= productionTime.get(outputBlocks_indexProducing)  / tickMultiplier ) { //production finished

                insertOutput_single(
                        outputStack.get(outputBlocks_indexProducing),
                        outputSlots
                );

                isProducing = false;
                elapsedTicksProducing = 0;
                outputBlocks_amount--;
                sendFinishedProductionMessage();
                BlockMachineBase.setState(this.world, this.pos);
            }
        }

        //DISASSEMBLY CODE
        if ( hasDisassembly && ( isDisassembling || checkMachineStateForDisassembly() ) ) {
            if (elapsedTicksDisassembling == 0) {
                isDisassembling = true;

                ArrayList<ItemStack> tmpInput = new ArrayList<>();
                tmpInput.add(outputStack.get(disassembleId));

                extractNeededInput_single(
                        tmpInput,
                        disassembleSlots
                );

                sendStartedDisassamblyMessage(
                        disassembleTicks
                );
                BlockMachineBase.setState(this.world, this.pos);
            }

            elapsedTicksDisassembling++;

            if (elapsedTicksDisassembling >= disassembleTicks) {
                //change the storage format from input (multiple item variants) to output (just one item variant)
                ArrayList<ItemStack> tmpStackList = new ArrayList<>();
                for ( ArrayList<ItemStack> output : inputStacks.get(disassembleId) )
                    tmpStackList.add(output.get(0));

                insertOutput(
                        tmpStackList,
                        inputSlots
                );

                isDisassembling = false;
                elapsedTicksDisassembling = 0;

                BlockMachineBase.setState(this.world, this.pos);
            }
        }
    }














    ////////////////////////////////////////////////////////////////////////////////////
    /// INVENTORY STUFF                                                              ///
    ////////////////////////////////////////////////////////////////////////////////////

    /**
     * this method checks on every update, if the machine has a production order
     * if this is the case, it checks, if the needed materials are given and there is space to output the materials
     * it also sets the machineState
     * @return the state
     */
    private boolean checkMachineStateForProduction() {
        if (outputBlocks_amount <= 0)
            return false;

        if (inputStacks.size() == 0)
            return false;

        boolean inputIsAvailable = inputIsAvailable(
                inputStacks.get(outputBlocks_indexProducing),
                inputSlots
        );

        if (!inputIsAvailable)
            return false;

        boolean outputCanBeInserted = outputCanBeInserted_single(
                outputStack.get(outputBlocks_indexProducing),
                outputSlots
        );

        if (!outputCanBeInserted)
            return false;

        return true;
    }

    private boolean checkMachineStateForDisassembly() {
        //init not runned
        if (outputStack.size() == 0)
            return false;

        //check if input is empty
        if (inventory.getStackInSlot(disassembleSlots.get(0)).getCount() < 0)
            return false;

        //get the ID of the inserted Block
        disassembleId = 0;
        for (ItemStack outputs : outputStack) {
            if (inventory.getStackInSlot(disassembleSlots.get(0)).getItem().equals( outputs.getItem() )) {
                break;
            }
            disassembleId++;
        }

        //item not found
        if (disassembleId >= outputStack.size())
            return false;

        //check if minimum amount of blocks is inserted
        if (inventory.getStackInSlot(disassembleSlots.get(0)).getCount() < outputStack.get(disassembleId).getCount() )
            return false;

        //check if output can be inserted
        //change the storage format from input (multiple item variants) to output (just one item variant)
        ArrayList<ItemStack> tmpStackList = new ArrayList<>();
        for ( ArrayList<ItemStack> output : inputStacks.get(disassembleId) )
            tmpStackList.add(output.get(0));
        boolean outputCanBeInserted = outputCanBeInserted(
                tmpStackList,
                inputSlots
        );

        if (!outputCanBeInserted)
            return false;

        return true;
    }

    /**
     * checks if the needed inputs for a recipe are available
     * @param inputStacks an ArrayList with multiple needed ItemStacks
     * @param slots an ArrayList with the slots in the ContainerInventory
     * @return the state
     */
    private boolean inputIsAvailable(ArrayList<ArrayList<ItemStack>> inputStacks, ArrayList<Integer> slots) {
        for (ArrayList<ItemStack> inputStack : inputStacks) {
            int amount = 0;
            int i;
            for (i = 0; i < slots.size(); i++) {
                if ( isInStackList(inputStack, inventory.getStackInSlot(slots.get(i)).getItem()) ) {
                    amount += inventory.getStackInSlot(slots.get(i)).getCount();
                    if (amount >= inputStack.get(0).getCount()) //compare to first entry, others are just alternative ones
                        break;
                }
            }
            //item not found:
            if (i == slots.size())
                return false;
        }
        return true;
    }

    /**
     * extracts the input from the slot
     * @param inputStacks an ArrayList of the Items to extract
     * @param slots an ArrayList with the slots in the ContainerInventory
     */
    private void extractNeededInput(ArrayList<ArrayList<ItemStack>> inputStacks, ArrayList<Integer> slots) {
        for (ArrayList<ItemStack> inputStack : inputStacks) {
            int remaining = inputStack.get(0).getCount();
            for (int slot : slots) {
                if ( isInStackList(inputStack, inventory.getStackInSlot(slot).getItem()) ) {
                    int amountInSlot = inventory.getStackInSlot(slot).getCount();
                    inventory.extractItem(slot, remaining, false);
                    remaining -= amountInSlot;

                    if (remaining <= 0)
                        break;
                }
            }
        }
    }

    private void extractNeededInput_single(ArrayList<ItemStack> inputStack, ArrayList<Integer> slots) {
        ArrayList<ArrayList<ItemStack>> inputStacks = new ArrayList<>();
        inputStacks.add(inputStack);

        extractNeededInput(inputStacks, slots);
    }

    /**
     * Checks if an slot is empty or stackable
     * @param outputStacks an ArrayList of the Items to add
     * @param slots an ArrayList with the slots in the ContainerInventory
     * @return the state
     */
    private boolean outputCanBeInserted(ArrayList<ItemStack> outputStacks, ArrayList<Integer> slots) {
        int emptySlots = 0;
        int usedEmptySlots = 0;

        //search for empty slots
        for (int slot : slots)
            if (inventory.getStackInSlot(slot).isEmpty())
                emptySlots++;

        //search for not empty ItemStacks to insert more items
        for (ItemStack outputStack : outputStacks) {
            int freeStackSlots = 0;
            int i;
            for (i = 0; i < slots.size(); i++) {
                if (inventory.getStackInSlot(slots.get(i)).getItem().equals(outputStack.getItem()))
                    freeStackSlots += outputStack.getMaxStackSize() - inventory.getStackInSlot(slots.get(i)).getCount();

                if (freeStackSlots >= outputStack.getCount())
                    break;

            }
            //ItemStack not found:
            if (i == slots.size()) {
                //check if empty slot is available
                if (emptySlots <= usedEmptySlots)
                    return false;

                usedEmptySlots++;
            }
        }

        return true;
    }

    private boolean outputCanBeInserted_single(ItemStack outputStack, ArrayList<Integer> slots) {
        ArrayList<ItemStack> outputStacks = new ArrayList<>();
        outputStacks.add(outputStack);

        return outputCanBeInserted(outputStacks, slots);
    }

    /**
     * inserts items in the first empty/stackable slots
     * @param outputStacks an ArrayList of the Items to add
     * @param slots an ArrayList with the slots in the ContainerInventory
     */
    private void insertOutput(ArrayList<ItemStack> outputStacks, ArrayList<Integer> slots) {
        //search for empty slots
        ArrayList<Integer> emptySlotIDs = new ArrayList<>();
        for (int slot : slots)
            if (inventory.getStackInSlot(slot).isEmpty())
                emptySlotIDs.add(slot);

        for (ItemStack _outputStack : outputStacks) {
            ItemStack outputStack = _outputStack.copy();
            //FIRST: Try to insert into existing stack
            for (int slot : slots) {
                if (inventory.getStackInSlot(slot).isEmpty())
                    continue;

                if (!inventory.getStackInSlot(slot).getItem().equals(outputStack.getItem()))
                    continue;

                int freespace = outputStack.getMaxStackSize() - inventory.getStackInSlot(slot).getCount();

                if (freespace < outputStack.getCount()) { //needs to be splitted
                    ItemStack splitted = outputStack.splitStack(outputStack.getCount() - freespace);
                    inventory.insertItem(slot, outputStack, false);
                    outputStack = splitted;
                } else {
                    inventory.insertItem(slot, outputStack, false);
                    outputStack.setCount(0);
                    break;
                }
            }
            //SECOND: If complete insertion wasn't possible, insert into empty slot
            if (outputStack.getCount() > 0) {
                inventory.insertItem(emptySlotIDs.get(0), outputStack, false);
                emptySlotIDs.remove(0);
            }
        }
    }

    private void insertOutput_single(ItemStack outputStack, ArrayList<Integer> slots) {
        ArrayList<ItemStack> outputStacks = new ArrayList<>();
        outputStacks.add(outputStack);

        insertOutput(outputStacks, slots);
    }

    boolean isInStackList(ArrayList<ItemStack> stackList, Item item) {
        for (ItemStack stack : stackList) {
            if (stack.getItem().equals(item))
                return true;
        }
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////
    /// NETWORKING STUFF                                                             ///
    ////////////////////////////////////////////////////////////////////////////////////

    public void cancelProduction() { //TODO: call on Blockbreak
        if (isProducing) {
            //change the storage format from input (multiple item variants) to output (just one item variant)
            ArrayList<ItemStack> tmpStackList = new ArrayList<>();
            for ( ArrayList<ItemStack> output : inputStacks.get(outputBlocks_indexProducing) )
                tmpStackList.add(output.get(0));

            insertOutput(
                    tmpStackList,
                    inputSlots
            );
        }
        isProducing           = false;
        outputBlocks_amount   = 0;
        elapsedTicksProducing = 0;
    }


    private void sendMachineIsPoweredMessage() {
        if (!this.world.isRemote) {
            BlockPos pos = this.getPos();

            PacketHandler.INSTANCE.sendToAllAround(
                    new PacketGuiIsPowered(
                            isPowered,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 6)
            );
        }
    }

    private void sendStartedProductionMessage(int productionTicks) {
        if (!this.world.isRemote) {
            BlockPos pos = this.getPos();

            PacketHandler.INSTANCE.sendToAllAround(
                    new PacketGuiStartedProduction(
                            productionTicks,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 6)
            );
        }
    }

    private void sendStartedDisassamblyMessage(int disassemblyTicks) {
        if (!this.world.isRemote) {

            BlockPos pos = this.getPos();

            PacketHandler.INSTANCE.sendToAllAround(
                    new PacketGuiStartedDisassembly(
                            disassemblyTicks,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 6)
            );
        }
    }

    private void sendFinishedProductionMessage() {
        if (!this.world.isRemote) {
            BlockPos pos = this.getPos();

            PacketHandler.INSTANCE.sendToAllAround(
                    new PackedGuiFinishedProduction(
                            outputBlocks_amount,
                            pos.getX(),
                            pos.getY(),
                            pos.getZ()
                    ),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 6)
            );
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////
    /// SYNCING AND NBT STUFF                                                        ///
    ////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        inventory.deserializeNBT(compound.getCompoundTag("inventory"));

        this.isPowered                   = compound.getBoolean("isPowered");
        this.isProducing                 = compound.getBoolean("isProducing");
        this.isDisassembling             = compound.getBoolean("isDisassembling");
        this.outputBlocks_indexProducing = compound.getInteger("outputBlocks_indexProducing");
        this.outputBlocks_amount         = compound.getInteger("outputBlocks_amount");
        this.elapsedTicksProducing       = compound.getInteger("elapsedTicksProducing");
        this.elapsedTicksDisassembling   = compound.getInteger("elapsedTicksDisassembling");
        this.tickMultiplier              = compound.getInteger("tickMultiplier");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());

        compound.setBoolean("isPowered", isPowered);
        compound.setBoolean("isProducing", isProducing);
        compound.setBoolean("isDisassembling", isDisassembling);
        compound.setInteger("outputBlocks_indexProducing", outputBlocks_indexProducing);
        compound.setInteger("outputBlocks_amount", outputBlocks_amount);
        compound.setInteger("elapsedTicksProducing", elapsedTicksProducing);
        compound.setInteger("elapsedTicksDisassembling", elapsedTicksDisassembling);
        compound.setInteger("tickMultiplier", tickMultiplier);

        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T) inventory : super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), -999, writeToNBT(new NBTTagCompound()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public final void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

}
