package com.kyfexuwu.kyfexs_minigames;

import com.kyfexuwu.kyfexs_minigames.minigames.deathswap.items.InfiniteRocket;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CustomItems {
    public static final Item INF_ROCKET = new InfiniteRocket(new Item.Settings().maxCount(1));
    public static void register(){
        Registry.register(Registry.ITEM, new Identifier(KyfexsMinigames.namespace, "infinite_rocket"), INF_ROCKET);
    }
}
