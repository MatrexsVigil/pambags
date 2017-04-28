package nl.lang2619.bagginses.items.bags.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import nl.lang2619.bagginses.helpers.NBTHelper;
import nl.lang2619.bagginses.helpers.Names;
import nl.lang2619.bagginses.inventory.InventoryBag;
import nl.lang2619.bagginses.items.bags.Bag;
import nl.lang2619.bagginses.items.bags.container.BagContainer;
import nl.lang2619.bagginses.references.BagTypes;
import nl.lang2619.bagginses.references.TextureInfo;
import org.lwjgl.opengl.GL11;

/**
 * Created by Tim on 8-2-2015.
 */
public class BagGui extends GuiContainer {

    private final ItemStack parentItemStack;
    private final InventoryBag inventoryBag;

    public BagGui(EntityPlayer entityPlayer, InventoryBag inventoryBag) {
        super(new BagContainer(entityPlayer, inventoryBag));
        this.parentItemStack = inventoryBag.parentItemStack;
        this.inventoryBag = inventoryBag;
        if (((Bag) parentItemStack.getItem()).getType() == BagTypes.TIER1) {
            this.ySize = 166 + 18;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {

        Bag item = (Bag) this.parentItemStack.getItem();
        if (item.getType() == BagTypes.TIER1) {
            mc.renderEngine.bindTexture(new ResourceLocation(TextureInfo.GUI_BACKPACK));
        }
        GL11.glColor4f(1, 1, 1, 1);

        int var5 = (width - xSize) / 2;
        int var6 = (height - ySize) / 2;
        drawTexturedModalRect(var5, var6, 0, 0, xSize, ySize);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int i, int j) {
        String s = "";
        Bag item = (Bag) this.parentItemStack.getItem();
        if (item.getType() == BagTypes.TIER1) {
            s = "Bag";

        }

        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (mc.player != null)
        {
            for (ItemStack itemStack : mc.player.inventory.mainInventory)
            {
                if (itemStack != ItemStack.EMPTY)
                {
                    if (NBTHelper.hasTag(itemStack, Names.NBT.BAG_OPEN))
                    {
                        NBTHelper.removeTag(itemStack, Names.NBT.BAG_OPEN);
                    }
                }
            }
        }
    }
}
