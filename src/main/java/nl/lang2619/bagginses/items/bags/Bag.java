package nl.lang2619.bagginses.items.bags;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nl.lang2619.bagginses.Bagginses;
import nl.lang2619.bagginses.config.ModConfig;
import nl.lang2619.bagginses.helpers.ChatUtils;
import nl.lang2619.bagginses.helpers.ItemHelper;
import nl.lang2619.bagginses.helpers.NBTHelper;
import nl.lang2619.bagginses.helpers.Names;
import nl.lang2619.bagginses.items.ModItems;
import nl.lang2619.bagginses.proxy.GuiInfo;
import nl.lang2619.bagginses.references.BagMode;
import nl.lang2619.bagginses.references.BagTypes;

import java.util.List;

/**
 * Created by Tim on 4/12/2015.
 */
public class Bag extends Item {
    String color;
    BagTypes type;


    public Bag(String color, BagTypes type) {
        super();
        maxStackSize = 1;
        setCreativeTab(Bagginses.BagTab);
        this.color = color;
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public BagTypes getType() {
        return type;
    }

    public String getUnlocalizedName(ItemStack itemStack) {

        return "backpack_" + color;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        if (stack.getTagCompound() != null
                && stack.getTagCompound().hasKey("soulbound")
                && stack.getTagCompound().getBoolean("soulbound")) {
            return true;
        }
        return stack.isItemEnchanted();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (player.isSneaking()) {
            changeMode(itemStack, world, player, hand);
            return new ActionResult(EnumActionResult.SUCCESS, itemStack);
        }

        if (!world.isRemote) {
            if (!ItemHelper.hasOwnerUUID(itemStack)) {
                ItemHelper.setOwner(itemStack, player);
            }
            NBTHelper.setUUID(itemStack);
            NBTHelper.setBoolean(itemStack, Names.NBT.BAG_OPEN, true);

            if (type == BagTypes.TIER1)
            {
                player.openGui(Bagginses.instance, GuiInfo.GUI_BACKPACK, world, 0, 0, 0);
            }
        }
        return new ActionResult(EnumActionResult.SUCCESS, itemStack);
    }

    private void changeMode(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!ModConfig.bagPickUp) {
            return;
        }
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tags = stack.getTagCompound();
        BagMode mode = BagMode.getMode(stack);

        mode = mode.next();

        tags.setString("bagMode", mode.getName());

        stack.setTagCompound(tags);

        if (world.isRemote)
            ChatUtils.sendNoSpamMessages(14, new TextComponentString(ChatFormatting.AQUA + "Mode: " + mode.getName()));
    }
}
