package me.jaackson.etched.common.item;

import me.jaackson.etched.EtchedRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BoomboxItem extends Item {
    public BoomboxItem(Properties properties) {
        super(properties);
    }

    public static boolean isPlaying(ItemStack stack) {
        if (stack.getItem() != EtchedRegistry.BOOMBOX.get())
            return false;

        // TODO: put actual playing check here
        return stack.getOrCreateTag().getBoolean("Playing");
    }
}
