package nl.lang2619.bagginses.helpers;

import net.minecraft.item.Item;

/**
 * Created by alex on 22/06/16.
 */
public class Bags {

    private Item tier1;
    private Item tier2;
    private Item tier3;
    private String color;
    private boolean registered;
    private String desc;
    private String serverDesc;

    public Bags(Item tier1, String color, boolean registered) {
        this.tier1 = tier1;
        this.color = color;
        this.registered = registered;
    }

    public Item getTier1() {
        return tier1;
    }

    public void setTier1(Item tier1) {
        this.tier1 = tier1;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isRegistered() {
        return registered;
    }

}
