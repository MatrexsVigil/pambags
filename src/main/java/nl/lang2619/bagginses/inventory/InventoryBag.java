package nl.lang2619.bagginses.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import nl.lang2619.bagginses.helpers.INBTTaggable;
import nl.lang2619.bagginses.helpers.NBTHelper;
import nl.lang2619.bagginses.helpers.Names;
import nl.lang2619.bagginses.items.bags.Bag;
import nl.lang2619.bagginses.references.BagTypes;

import java.util.UUID;

/**
 * Created by Tim on 4/12/2015.
 */
public class InventoryBag implements IInventory, INBTTaggable {

    public ItemStack parentItemStack;
    public NonNullList<ItemStack> inventory;
    public NonNullList<ItemStack> itemSlots = NonNullList.<ItemStack>withSize(64, ItemStack.EMPTY);
    protected String customName;
    public NBTTagCompound tag;
    public static boolean instantAdd = false;
    private boolean added = false;
    public ItemStack item;
    public int stackSize;

    public InventoryBag(ItemStack itemStack) {
        Bag item = (Bag) itemStack.getItem();
        int size = item.getType().getSize();
        inventory.clear();
        parentItemStack = itemStack;
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        tag = itemStack.getTagCompound();
        this.item = itemStack;
        readFromNBT(itemStack.getTagCompound());
    }

    public void onGuiSaved(EntityPlayer entityPlayer) {
        parentItemStack = findParentItemStack(entityPlayer);

        if (parentItemStack != ItemStack.EMPTY) {
            save();
        }
    }

    public ItemStack findParentItemStack(EntityPlayer entityPlayer) {
        if (NBTHelper.hasUUID(parentItemStack)) {
            UUID parentItemStackUUID = new UUID(parentItemStack.getTagCompound().getLong(Names.NBT.UUID_MOST_SIG), parentItemStack.getTagCompound().getLong(Names.NBT.UUID_LEAST_SIG));
            for (int i = 0; i < entityPlayer.inventory.getSizeInventory(); i++) {
                ItemStack itemStack = entityPlayer.inventory.getStackInSlot(i);

                if (NBTHelper.hasUUID(itemStack)) {
                    if (itemStack.getTagCompound().getLong(Names.NBT.UUID_MOST_SIG) == parentItemStackUUID.getMostSignificantBits() && itemStack.getTagCompound().getLong(Names.NBT.UUID_LEAST_SIG) == parentItemStackUUID.getLeastSignificantBits()) {
                        return itemStack;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean matchesUUID(UUID uuid) {
        return NBTHelper.hasUUID(parentItemStack) && parentItemStack.getTagCompound().getLong(Names.NBT.UUID_LEAST_SIG) == uuid.getLeastSignificantBits() && parentItemStack.getTagCompound().getLong(Names.NBT.UUID_MOST_SIG) == uuid.getMostSignificantBits();
    }

    public void save() {
        NBTTagCompound nbtTagCompound = parentItemStack.getTagCompound();

        if (nbtTagCompound == null) {
            nbtTagCompound = new NBTTagCompound();

            UUID uuid = UUID.randomUUID();
            nbtTagCompound.setLong(Names.NBT.UUID_MOST_SIG, uuid.getMostSignificantBits());
            nbtTagCompound.setLong(Names.NBT.UUID_LEAST_SIG, uuid.getLeastSignificantBits());
        }

        writeToNBT(nbtTagCompound);
        parentItemStack.setTagCompound(nbtTagCompound);
    }

    @Override
    public int getSizeInventory() {
    	return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
            return inventory[slotIndex];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.itemSlots, index);
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
    	inventory[slotIndex] = itemStack;
        if ((itemStack != ItemStack.EMPTY) && (itemStack.getCount() > getInventoryStackLimit())) {
            itemStack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    public ItemStack addItem(ItemStack stack)
    {
        ItemStack itemstack = stack.copy();

        for (int i = 0; i < this.itemSlots.size(); ++i)
        {
            ItemStack itemstack1 = this.getStackInSlot(i);

            if (itemstack1.isEmpty())
            {
                this.setInventorySlotContents(i, itemstack);
                this.markDirty();
                return ItemStack.EMPTY;
            }

            if (ItemStack.areItemsEqual(itemstack1, itemstack))
            {
                int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
                int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

                if (k > 0)
                {
                    itemstack1.grow(k);
                    itemstack.shrink(k);

                    if (itemstack.isEmpty())
                    {
                        this.markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (itemstack.getCount() != stack.getCount())
        {
            this.markDirty();
        }

        return itemstack;
    }

    public void processInv() {
        if (itemSlots[0] != ItemStack.EMPTY) {
            itemSlots[0] = ItemStack.EMPTY;
        }
        /*for (int i = 1; i < 64; ++i) {
            itemSlots[i] = null;
        }*/
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        writeToNBT(tag);
        setNBT(item);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    public void setNBT(ItemStack item) {
        item.setTagCompound(tag);
    }

    @Override
    public void openInventory(EntityPlayer entityPlayer) {
        readFromNBT(tag);
    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer) {
        writeToNBT(tag);
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
    	return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound) {
    	if (nbtTagCompound != null && nbtTagCompound.hasKey(Names.NBT.ITEMS)) {
            // Read in the ItemStacks in the inventory from NBT
            if (nbtTagCompound.hasKey(Names.NBT.ITEMS)) {
                NBTTagList tagList = nbtTagCompound.getTagList(Names.NBT.ITEMS, 10);
                inventory.size();
                for (int i = 0; i < tagList.tagCount(); ++i) {
                    NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
                    byte slotIndex = tagCompound.getByte("Slot");
                    if (slotIndex >= 0 && slotIndex < inventory.length) {
                        inventory[slotIndex] = new ItemStack(tagCompound);
                    }
                }
            }

            // Read in any custom name for the inventory
            if (nbtTagCompound.hasKey("display") && nbtTagCompound.getTag("display").getClass().equals(NBTTagCompound.class)) {
                if (nbtTagCompound.getCompoundTag("display").hasKey("Name")) {
                    customName = nbtTagCompound.getCompoundTag("display").getString("Name");
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound) {
    	// Write the ItemStacks in the inventory to NBT
        NBTTagList tagList = new NBTTagList();
        for (int currentIndex = 0; currentIndex < inventory.length; ++currentIndex) {
            if (inventory[currentIndex] != ItemStack.EMPTY) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) currentIndex);
                inventory[currentIndex].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }
        nbtTagCompound.setTag(Names.NBT.ITEMS, tagList);
    }

    @Override
    public String getTagLabel() {
        return "InventoryBag";
    }

    @Override
    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomName();
        } else {
            return Names.Containers.BAGS;
        }
    }

    public boolean hasCustomName() {
        return customName != null && customName.length() > 0;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    public String getCustomName() {
        return customName;
    }
}
