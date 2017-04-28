package nl.lang2619.bagginses.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Created by Tim on 8/24/2014.
 */
public class ModConfig {

    public static String black,
            seed,
            sapling,
            crop;
    public static boolean whitelist;
    public static boolean bagPickUp, hotbarBagPickUp;
    public static final String CATEGORY_WHITELIST = "whitelist";
    public static final String CATEGORY_MISC = "miscellaneous config options";

    public static void init(File file) {
        Configuration config = new Configuration(file);
        config.load();
        config.setCategoryComment(CATEGORY_WHITELIST, "");
        whitelist = config.get("whitelist", CATEGORY_WHITELIST, true, "If you want to blacklist items instead of whitelist, change this config to false").getBoolean();

        config.setCategoryComment(CATEGORY_WHITELIST, "Input here all your whitelists per bag. \nIf empty, bag won't be added to the world.\nUse modid:* to whitelist the whole mod.\nFor example:\nminecraft:wool/2 will add Magenta wool to the whitelist. \nminecraft:wool will add every wool type. \nminecraft:wool/0+1+2 will add damage value 0,1 and 2.\nAdd multiple items by using a comma between items.");
        seed = config.get(CATEGORY_WHITELIST, "Whitelist Items/Blocks for seed bag", "").getString();
        sapling = config.get(CATEGORY_WHITELIST, "Whitelist Items/Blocks for sapling bag", "").getString();
        crop = config.get(CATEGORY_WHITELIST, "Whitelist Items/Blocks for crop bag", "").getString();


        config.setCategoryComment(CATEGORY_MISC, "Miscellaneous config options related to the mod");
        bagPickUp = config.get(CATEGORY_MISC, "Should bags be able to pick up items when dropped", true).getBoolean();
        hotbarBagPickUp = config.get(CATEGORY_MISC, "Should bag pickup be enabled for bags in your hotbar?", true).getBoolean();
        if (config.hasChanged())
            config.save();
    }
}
