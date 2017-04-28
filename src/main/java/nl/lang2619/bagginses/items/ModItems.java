package nl.lang2619.bagginses.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import nl.lang2619.bagginses.config.ModConfig;
import nl.lang2619.bagginses.helpers.Bags;
import nl.lang2619.bagginses.items.bags.Bag;
import nl.lang2619.bagginses.references.BagTypes;
import nl.lang2619.bagginses.references.Defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tim on 8/24/2014.
 */
public class ModItems {
	private static Item seed;
	private static Item sapling;
    private static Item crop;

    //public static ArrayList<Bags> bags = new ArrayList<Bags>();
    public static Map<String, Bags> bags = new HashMap<String, Bags>();

    public static ArrayList<String> bagColors = new ArrayList<String>() {{
        add("seed");
        add("sapling");
        add("crop");
    }};

    public static Map<String, Integer> colorNumbers = new HashMap<String, Integer>() {{
        put("seed", 0);
        put("sapling", 1);
        put("crop", 2);
    }};

    public static void init() {
        fillTiers();
        
        RecipeSorter.register(Defaults.MODID + ":bag", BagRecipe.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");

        //Tier 1 bags
        for (Bags bag : bags.values()) {
            bag.setTier1(new Bag(bag.getColor(), BagTypes.TIER1));
            registerItem(bag.getTier1(), bag.getColor());
            String color = bag.getColor();
            if (bag.isRegistered())
                GameRegistry.addShapedRecipe(new ItemStack(bag.getTier1()), "sws", "wcw", "sws", 's', Items.STRING, 'w', new ItemStack(Blocks.WOOL, 1, colorNumbers.get(color)), 'c', Blocks.CHEST);
        }

    }

    static void registerItem(Item item, String name) {
        GameRegistry.register(item, new ResourceLocation(Defaults.MODID + ":" + name));
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (Bags bag : bags.values()) {

            ModelLoader.registerItemVariants(bag.getTier1());
            ModelLoader.setCustomModelResourceLocation(bag.getTier1(), 0, new ModelResourceLocation(Defaults.MODID + ":" + bag.getColor(), "inventory"));
        }
    }

    private static void newBag(Item t1, String color) {
        newBag(t1, color);
    }

    private static void newBag(Item t1, String color, boolean register) {
        bags.put(color, new Bags(t1, color, register));
    }

    static void fillTiers() {
        if (ModConfig.whitelist) {
            if (!ModConfig.seed.isEmpty()) {
                newBag(seed, "seed");
            } else {
                newBag(seed, "seed", false);
            }

            if (!ModConfig.sapling.isEmpty()) {
                newBag(sapling, "sapling");
            } else {
                newBag(sapling, "sapling", false);
            }

            if (!ModConfig.crop.isEmpty()) {
                newBag(crop, "crop");
            } else {
                newBag(crop, "crop", false);
            }

            

        } else {
            newBag(seed, "seed");
            newBag(sapling, "sapling");
            newBag(crop, "crop");
            
        }
    }

}
