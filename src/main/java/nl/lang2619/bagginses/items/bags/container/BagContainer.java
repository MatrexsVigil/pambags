package nl.lang2619.bagginses.items.bags.container;

import net.minecraft.inventory.ClickType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import nl.lang2619.bagginses.config.ModConfig;
import nl.lang2619.bagginses.helpers.NBTHelper;
import nl.lang2619.bagginses.helpers.Names;
import nl.lang2619.bagginses.inventory.InventoryBag;
import nl.lang2619.bagginses.items.bags.Bag;
import nl.lang2619.bagginses.references.BagTypes;
import nl.lang2619.bagginses.references.BlockList;
import nl.lang2619.bagginses.references.StackUtils;

import java.util.UUID;

/**
 * Created by Tim on 8-2-2015.
 */
public class BagContainer extends ContainerBagginses {
    int bagStartX = 0;
    int bagStartY = 0;

    private final EntityPlayer entityPlayer;
    public final InventoryBag inventoryBag;
    private int blockedSlot;

    private int bagInventoryRows;
    private int bagInventoryColumns;

    boolean foid = false;
    String color;

    public BagContainer(EntityPlayer entityPlayer, InventoryBag inventoryBag) {

        this.entityPlayer = entityPlayer;
        this.inventoryBag = inventoryBag;

        Bag item = (Bag) inventoryBag.parentItemStack.getItem();

        BagTypes type = item.getType();
        color = item.getColor();

        bagInventoryRows = type.getRows();
        bagInventoryColumns = type.getColumns();
        bagStartX = type.getBagStartX();
        bagStartY = type.getBagStartY();

        int i = 0;

        //Inventory
        for (int bagRowIndex = 0; bagRowIndex < bagInventoryRows; ++bagRowIndex) {
            for (int bagColumnIndex = 0; bagColumnIndex < bagInventoryColumns; ++bagColumnIndex) {
                this.addSlotToContainer(new SlotBag(this, inventoryBag, entityPlayer, bagColumnIndex + bagRowIndex * bagInventoryColumns, bagStartX + bagColumnIndex * 18, bagStartY + bagRowIndex * 18));
                i++;
            }
        }

        int added = 0;

        //PlayerInventory
        for (int inventoryRowIndex = 0; inventoryRowIndex < PLAYER_INVENTORY_ROWS; ++inventoryRowIndex) {
            for (int inventoryColumnIndex = 0; inventoryColumnIndex < PLAYER_INVENTORY_COLUMNS; ++inventoryColumnIndex) {
                this.addSlotToContainer(new Slot(entityPlayer.inventory, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 30 + (18 * bagInventoryRows) + inventoryRowIndex * 18 + added));
            }
        }

        //Hotbar
        for (int actionBarSlotIndex = 0; actionBarSlotIndex < PLAYER_INVENTORY_COLUMNS; ++actionBarSlotIndex) {
            this.addSlotToContainer(new Slot(entityPlayer.inventory, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 30 + (18 * bagInventoryRows) + 58 + added));
        }
        blockedSlot = entityPlayer.inventory.currentItem + 27 + i;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.world.isRemote) {
            InventoryPlayer invPlayer = player.inventory;
            for (ItemStack itemStack : invPlayer.mainInventory) {
                if (itemStack != ItemStack.EMPTY) {
                    if (NBTHelper.hasTag(itemStack, Names.NBT.BAG_OPEN)) {
                        NBTHelper.removeTag(itemStack, Names.NBT.BAG_OPEN);
                    }
                }
            }
            saveInventory(player);
        }
    }


    public boolean isItemStackParent(ItemStack itemStack) {
        if (NBTHelper.hasUUID(itemStack)) {
            UUID stackUUID = new UUID(itemStack.getTagCompound().getLong(Names.NBT.UUID_MOST_SIG), itemStack.getTagCompound().getLong(Names.NBT.UUID_LEAST_SIG));
            return inventoryBag.matchesUUID(stackUUID);
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int slotIndex) {
        if (!foid) {
            ItemStack newItemStack = null;
            Slot slot = inventorySlots.get(slotIndex);

            if (slot != null && slot.getHasStack()) {
                ItemStack itemStack = slot.getStack();
                newItemStack = itemStack.copy();

                if (slotIndex < bagInventoryRows * bagInventoryColumns) {
                    if (!this.mergeItemStack(itemStack, bagInventoryRows * bagInventoryColumns, inventorySlots.size(), false)) {
                        return null;
                    }
                } else if (itemStack.getItem() instanceof Bag) {
                    if (slotIndex < (bagInventoryRows * bagInventoryColumns) + (PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS)) {
                        if (!this.mergeItemStack(itemStack, (bagInventoryRows * bagInventoryColumns) + (PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS), inventorySlots.size(), false)) {
                            return null;
                        }
                    } else if (!this.mergeItemStack(itemStack, bagInventoryRows * bagInventoryColumns, (bagInventoryRows * bagInventoryColumns) + (PLAYER_INVENTORY_ROWS * PLAYER_INVENTORY_COLUMNS), false)) {
                        return null;
                    }
                } else if (!this.mergeItemStack(itemStack, 0, bagInventoryRows * bagInventoryColumns, false)) {
                    return null;
                }
                slot.onSlotChanged();
            }
            return newItemStack;
        } else {
            return transferVoid(p, slotIndex);
        }
    }

    private ItemStack transferVoid(EntityPlayer player, int slotIndex) {
        ItemStack originalStack = null;
        Slot slot = inventorySlots.get(slotIndex);
        int numSlots = inventorySlots.size();
        if ((slot != null) && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            originalStack = stackInSlot.copy();
            if (slotIndex >= numSlots - 9 * 4 && tryShiftItem(stackInSlot, numSlots)) {
                // NOOP
            } else if (slotIndex >= numSlots - 9 * 4 && slotIndex < numSlots - 9) {
                if (!shiftItemStack(stackInSlot, numSlots - 9, numSlots))
                    return null;
            } else if (slotIndex >= numSlots - 9 && slotIndex < numSlots) {
                if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots - 9))
                    return null;
            } else if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots))
                return null;
            slot.onSlotChange(stackInSlot, originalStack);
            slot.onSlotChanged();
            if (stackInSlot.getCount() == originalStack.getCount())
                return ItemStack.EMPTY;
            slot.onTake(player, stackInSlot);
        }
        return originalStack;
    }

    private boolean tryShiftItem(ItemStack stackToShift, int numSlots) {
        for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++) {
            Slot slot = inventorySlots.get(machineIndex);
            if (!slot.isItemValid(stackToShift))
                continue;
            if (shiftItemStack(stackToShift, machineIndex, machineIndex + 1))
                return true;
        }
        return false;
    }

    protected boolean shiftItemStack(ItemStack stackToShift, int start, int end) {
        boolean changed = false;
        if (stackToShift.isStackable())
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot != ItemStack.EMPTY && StackUtils.isIdenticalItem(stackInSlot, stackToShift)) {
                    int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    if (resultingStackSize <= max) {
                        stackToShift = ItemStack.EMPTY;
                        stackInSlot.setCount(resultingStackSize);
                        slot.onSlotChanged();
                        changed = true;
                    } else if (stackInSlot.getCount() < max) {
                        stackToShift.shrink(max - stackInSlot.getCount());
                        stackInSlot.setCount(max);
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
            }
        if (stackToShift.getCount() > 0)
            for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++) {
                Slot slot = inventorySlots.get(slotIndex);
                ItemStack stackInSlot = slot.getStack();
                if (stackInSlot == ItemStack.EMPTY) {
                    int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
                    stackInSlot = stackToShift.copy();
                    stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
                    stackToShift.shrink(stackInSlot.getCount());
                    slot.putStack(stackInSlot);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        return changed;
    }

    public void saveInventory(EntityPlayer player) {
        inventoryBag.onGuiSaved(player);
    }

    public int getNumColumns() {
        return bagInventoryRows;
    }

    @Override
    public ItemStack slotClick(int slotID, int clickedButton, ClickType clickType, EntityPlayer player) {
        if (slotID == this.blockedSlot)
            return null;
        return super.slotClick(slotID, clickedButton, clickType, player);
    }

    private class SlotBag extends Slot {
        private final EntityPlayer entityPlayer;
        private BagContainer containerBag;

        public SlotBag(BagContainer containerBag, IInventory inventory, EntityPlayer entityPlayer, int slotIndex, int x, int y) {
            super(inventory, slotIndex, x, y);
            this.entityPlayer = entityPlayer;
            this.containerBag = containerBag;
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();

            if (!foid) {
                if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                    containerBag.saveInventory(entityPlayer);
                }
            }
        }

        /**
         * Check if the stack is a valid item for this slot. Always true beside for the armor slots.
         */
        @Override
        public boolean isItemValid(ItemStack itemStack) {
            if (itemStack == null) {
                return true;
            } else {
                itemStack = itemStack.copy();
                itemStack.setCount(1);
                if (!(itemStack.getItem() instanceof Bag)) {
                    if (ModConfig.whitelist) {
                        //Whitelist
                        return BlockList.contains(itemStack.getItem(), itemStack.getItemDamage(), color);
                    }else{
                        //Blacklist
                        return !BlockList.contains(itemStack.getItem(), itemStack.getItemDamage(), color);
                    }
                }
                return false;
            }
        }
    }
}
