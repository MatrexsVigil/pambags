package nl.lang2619.bagginses.event;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.lang2619.bagginses.Bagginses;

import java.util.ListIterator;

/**
 * Created by alex on 23/05/16.
 */
public class SoulBoundEventHandler {

    private boolean isSoulBound(ItemStack stack) {
        try {
            if (stack != null
                    && stack.getTagCompound() != null
                    && stack.getTagCompound().hasKey("soulbound")
                    && stack.getTagCompound().getBoolean("soulbound")) {
                return true;
            }
        } catch (Exception e){

        }
        return false;
    }

    /*
     * Code taken with love from EnderIO by SleepyTrousers
     * https://github.com/SleepyTrousers/EnderIO/blob/master/src/main/java/crazypants/enderio/enchantment/EnchantmentSoulBound.java
     */

    /*
   * This is called the moment the player dies and drops his stuff.
   *
   * We go early, so we can get our items before other mods put them into some
   * grave. Also remove them from the list so they won't get duped. If the
   * inventory overflows, e.g. because everything there and the armor is
   * soulbound, let the remainder be dropped/graved.
   */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDropsEvent evt) {
        if (evt.getEntityPlayer() == null || evt.getEntityPlayer() instanceof FakePlayer || evt.isCanceled()) {
            return;
        }
        if(evt.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")) {
            return;
        }

        ListIterator<EntityItem> iter = evt.getDrops().listIterator();
        while (iter.hasNext()) {
            EntityItem ei = iter.next();
            ItemStack item = ei.getEntityItem();
            if(isSoulBound(item)) {
                if (addToPlayerInventory(evt.getEntityPlayer(), item)) {
                    iter.remove();
                }
            }
        }

    }



    /*
     * This is called when the user presses the "respawn" button. The getOriginal()
     * inventory would be empty, but onPlayerDeath() above placed items in it.
     *
     * Note: Without other death-modifying mods, the content of the old inventory
     * would always fit into the new one (both being empty but for soulbound items
     * in the old one) and the old one would be discarded just after this method.
     * But better play it safe and assume that an overflow is possible and that
     * another mod may move stuff out of the old inventory, too.
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone evt) {
        if (!evt.isWasDeath() || evt.isCanceled()) {
            return;
        }
        if(evt.getOriginal() == null || evt.getEntityPlayer() == null || evt.getEntityPlayer() instanceof FakePlayer) {
            return;
        }
        if(evt.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")) {
            return;
        }
        for (int i = 0; i < evt.getOriginal().inventory.mainInventory.size(); i++) {
            ItemStack item = evt.getOriginal().inventory.mainInventory.get(i);
            if(isSoulBound(item)) {
                if (addToPlayerInventory(evt.getEntityPlayer(), item)) {
                    evt.getOriginal().inventory.mainInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < evt.getOriginal().inventory.armorInventory.size(); i++) {
            ItemStack item = evt.getOriginal().inventory.armorInventory.get(i);
            if(isSoulBound(item)) {
                if (addToPlayerInventory(evt.getEntityPlayer(), item)) {
                    evt.getOriginal().inventory.armorInventory.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private boolean addToPlayerInventory(EntityPlayer entityPlayer, ItemStack item) {
        if(item == null || entityPlayer == null) {
            return false;
        }
        if(item.getItem() instanceof ItemArmor) {
            ItemArmor arm = (ItemArmor) item.getItem();
            int index = 3 - arm.armorType.getIndex();
            if(entityPlayer.inventory.armorInventory.get(index) == ItemStack.EMPTY) {
                entityPlayer.inventory.armorInventory.set(index, item);
                return true;
            }
        }

        InventoryPlayer inv = entityPlayer.inventory;
        for (int i = 0; i < inv.mainInventory.size(); i++) {
            if(inv.mainInventory.get(i) == ItemStack.EMPTY) {
                inv.mainInventory.set(i, item.copy());
                return true;
            }
        }

        return false;
    }
}
