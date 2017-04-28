package nl.lang2619.bagginses.event;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nl.lang2619.bagginses.helpers.BagFinder;
import nl.lang2619.bagginses.inventory.InventoryBag;
import nl.lang2619.bagginses.items.bags.Bag;
import nl.lang2619.bagginses.items.bags.container.BagContainer;
import nl.lang2619.bagginses.references.BagMode;
import nl.lang2619.bagginses.references.BagTypes;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Stolen with love from Modular Utilities
 * https://github.com/BrassGoggledCoders/ModularUtilities
 */
public class ItemDropEvent {

    //TODO add bags in hotbar too

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if(event.getSource().getDamageType() == "player") {
            EntityPlayer player = (EntityPlayer) event.getSource().getEntity();
            ItemStack bagStack = BagFinder.getBag(player);
            if (bagStack != null) {
                Bag bag = (Bag) bagStack.getItem();
                //Only want pickup
                if (BagMode.getMode(bagStack) != BagMode.PICKUP)
                    return;
                Iterator<EntityItem> drops = event.getDrops().iterator();
                ArrayList<EntityItem> toRemove = new ArrayList<EntityItem>();
                
                event.getDrops().removeAll(toRemove);
            }
        }
    }

    @SubscribeEvent
    public void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
        if(event.getHarvester() != null) {
            ItemStack bagStack = BagFinder.getBag(event.getHarvester());
            if (bagStack != null) {
                Bag bag = (Bag) bagStack.getItem();
                //Only want pickup
                if (BagMode.getMode(bagStack) != BagMode.PICKUP)
                    return;
                Iterator<ItemStack> drops = event.getDrops().iterator();
                ArrayList<ItemStack> toRemove = new ArrayList<ItemStack>();
                event.getDrops().removeAll(toRemove);
            }
        }
    }

    public boolean doStacksMatch(ItemStack stackA, ItemStack stackB) {
        if (stackA == null
                || stackB == null)
            return false;
        if (stackA.getItem() != stackB.getItem())
            return false;
        if (stackA.getItemDamage() != stackB.getItemDamage())
            return false;
        if (stackA.getTagCompound() != stackB.getTagCompound())
            return false;

        return true;
    }
}
