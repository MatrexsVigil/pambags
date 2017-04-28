package nl.lang2619.bagginses.references;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import nl.lang2619.bagginses.config.ModConfig;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tim on 8-2-2015.
 * addFromString() taken from chylex with permission
 * Source : https://github.com/chylex/Hardcore-Ender-Expansion
 */
public class BlockList {

    public static IdentityHashMap<Item, short[]> seedList = new IdentityHashMap<Item, short[]>();
    public static IdentityHashMap<Item, short[]> saplingList = new IdentityHashMap<Item, short[]>();
    public static IdentityHashMap<Item, short[]> cropList = new IdentityHashMap<Item, short[]>();


    public static boolean contains(Item item, int damage, String color) {
        short[] listedDamages = getList(color).get(item);
        return (listedDamages != null && listedDamages.length > 0 && (listedDamages[0] == -1 || ArrayUtils.contains(listedDamages, (short) damage)));
    }

    public static IdentityHashMap<Item, short[]> getList(String color) {
        if (color.equals("seed")) {
            return seedList;
        }
        if (color.equals("sapling")) {
            return saplingList;
        }
        if (color.equals("crop")) {
            return cropList;
        }
        
        return null;
    }

    public static void addFromString(String list, String color) {
        if (list.isEmpty()) return;

        int added = 0;
        String[] split = list.split(",");

        for (String entry : split) {
            entry = entry.trim();
            if (entry.length() == 0) continue;

            String itemName = entry;
            short[] dmgs = new short[]{-1};

            // PARSE DAMAGE VALUES
            if (entry.contains("/")) {
                String[] sep = entry.split("/");
                if (sep.length != 2) {
                    Log.warn("Invalid entry in whitelist $1 : $0", entry, color);
                    continue;
                }
                itemName = sep[0];
                try {
                    if (sep[1].contains("+")) {
                        String[] dmgVals = sep[1].split("\\+");
                        dmgs = new short[dmgVals.length];
                        for (int a = 0; a < dmgVals.length; a++) dmgs[a] = Short.parseShort(dmgVals[a]);
                    } else dmgs = new short[]{Short.parseShort(sep[1])};
                } catch (NumberFormatException e) {
                    Log.warn("Invalid entry in whitelist $1, wrong damage values: $0", entry, color);
                    continue;
                }
            }

            // PARSE ENTRY ID AND NAME
            String[] itemId = itemName.split(":");
            if (itemId.length > 2) {
                Log.warn("Invalid entry in whitelist $1, wrong item identifier: $0", entry, color);
                continue;
            } else if (itemId.length == 1) {
                itemId = new String[]{"minecraft", itemId[0]};
            }
            if (itemId[0].equals("ore")) {
                String identifier = itemId[1];
                NonNullList<ItemStack> items = OreDictionary.getOres(identifier);
                for (ItemStack is : items) {
                    if (GameData.getItemRegistry().getRaw(Item.getIdFromItem(is.getItem())) != null) {
                        Item item = GameData.getItemRegistry().getRaw(Item.getIdFromItem(is.getItem()));
                        getList(color).put(item, dmgs);
                        ++added;
                    } else {
                        Block block = GameData.getBlockRegistry().getRaw(Block.getIdFromBlock(Block.getBlockFromItem(is.getItem())));
                        getList(color).put(Item.getItemFromBlock(block), dmgs);
                        ++added;
                    }
                }
            }
            if (itemId[1].equals("*")) { // BLACKLIST ALL BLOCKS AND ITEMS FROM MOD
                String identifier = itemId[0] + ":";
                // SEARCH ALL BLOCKS WITH SPECIFIED ID
                added = SpecifiedBlock(color, added, dmgs, itemId, identifier);
                // SEARCH ALL ITEMS WITH SPECIFIED ID
                added = SpecifiedItem(color, added, dmgs, itemId, identifier);
            } else { // BLACKLIST SPECIFIED ENTRY
                added = SpecifiedEntry(color, added, entry, dmgs, itemId);
            }
        }
        //WHITELIST ALL MODS WITH THEIR BLOCKS/ITEMS
        if (list.equals("*:*")) {
            added = addAllMods(color, added);
        }

        if (added > 0) {
            if (ModConfig.whitelist) {
                Log.info("Added $0 items into whitelist $1", added, color);
            } else {
                Log.info("Added $0 items into blacklist $1", added, color);
            }
        }
    }

    private static int SpecifiedEntry(String color, int added, String entry, short[] dmgs, String[] itemId) {
        Item item = GameData.getItemRegistry().getValue(new ResourceLocation(itemId[0], itemId[1]));
        if (item == null) {
            Block block = GameData.getBlockRegistry().getValue(new ResourceLocation(itemId[0], itemId[1]));
            if (block == null) {
                if (itemId[0].equals("minecraft") || Loader.isModLoaded(itemId[0]))
                    Log.warn("Invalid entry in whitelist $1, item not found: $0", entry, color);
                return added;
            } else item = Item.getItemFromBlock(block);
        }
        getList(color).put(item, dmgs);
        ++added;
        return added;
    }

    private static int SpecifiedItem(String color, int added, short[] dmgs, String[] itemId, String identifier) {
        for (ResourceLocation key : GameData.getItemRegistry().getKeys()) {
            if (key.toString().startsWith(identifier)) {
                int id = GameData.getItemRegistry().getId(key);
                Item item = Item.getItemById(id);
                if (item == null) {
                    if (itemId[0].equals("minecraft") || Loader.isModLoaded(itemId[0]))
                        Log.warn("Stumbled upon invalid entry in item registry while parsing whitelist $1, object not found: $0", key, color);
                    continue;
                }
                getList(color).put(item, dmgs);
                ++added;
            }
        }
        return added;
    }

    private static int SpecifiedBlock(String color, int added, short[] dmgs, String[] itemId, String identifier) {
        for (ResourceLocation key : GameData.getBlockRegistry().getKeys()) {
            if (key.toString().startsWith(identifier)) {
                int id = GameData.getBlockRegistry().getId(key);
                Block block = Block.getBlockById(id);
                if (block == null) {
                    if (itemId[0].equals("minecraft") || Loader.isModLoaded(itemId[0]))
                        Log.warn("Stumbled upon invalid entry in block registry while parsing whitelist $1, object not found: $0", key, color);
                    continue;
                }
                getList(color).put(Item.getItemFromBlock(block), dmgs);
                ++added;
            }
        }
        return added;
    }

    private static int addAllMods(String color, int added) {
        short[] dmgs = new short[]{-1};
        Set<Map.Entry<String, ModContainer>> mapSet = Loader.instance().getIndexedModList().entrySet();
        for (Map.Entry<String, ModContainer> mapEntry : mapSet) {
            String keyValue = mapEntry.getKey();
            String identifier = (keyValue + ":").toLowerCase();
            for (ResourceLocation key : GameData.getBlockRegistry().getKeys()) {
                if (key.toString().startsWith(identifier)) {
                    int id = GameData.getBlockRegistry().getId(key);
                    Block block = Block.getBlockById(id);
                    if (block == null) {
                        if (keyValue.equals("minecraft") || Loader.isModLoaded(keyValue))
                            Log.warn("Stumbled upon invalid entry in block registry while parsing whitelist $1, object not found: $0", key, color);
                        continue;
                    }
                    getList(color).put(Item.getItemFromBlock(block), dmgs);
                    ++added;
                }
            }
            for (ResourceLocation key : GameData.getItemRegistry().getKeys()) {
                if (key.toString().startsWith(identifier)) {
                    int id = GameData.getItemRegistry().getId(key);
                    Item item = Item.getItemById(id);

                    if (item == null) {
                        if (keyValue.equals("minecraft") || Loader.isModLoaded(keyValue))
                            Log.warn("Stumbled upon invalid entry in item registry while parsing whitelist $1, object not found: $0", key, color);
                        continue;
                    }
                    getList(color).put(item, dmgs);
                    ++added;
                }
            }
        }
        String identifier = "minecraft:";
        for (ResourceLocation key : GameData.getBlockRegistry().getKeys()) {
            if (key.toString().startsWith(identifier)) {
                int id = GameData.getBlockRegistry().getId(key);
                Block block = Block.getBlockById(id);
                getList(color).put(Item.getItemFromBlock(block), dmgs);
                ++added;
            }
        }
        for (ResourceLocation key : GameData.getItemRegistry().getKeys()) {
            if (key.toString().startsWith(identifier)) {
                int id = GameData.getItemRegistry().getId(key);
                Item item = Item.getItemById(id);
                getList(color).put(item, dmgs);
                ++added;
            }
        }
        return added;
    }
}