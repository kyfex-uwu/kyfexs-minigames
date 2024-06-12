package com.kyfexuwu.kyfexs_minigames.minigames;

import com.kyfexuwu.kyfexs_minigames.minigames.deathswap.Deathswap;
import com.kyfexuwu.server_guis.ServerGUIs;
import net.minecraft.text.Text;

public class Minigames {
    public static void register(){
        Minigame.minigameData.put(Deathswap.class, new Minigame.MinigameData(
                "Death Swap",
                ServerGUIs.getPlayerHead(1816280691,1581731231,-1466764401,1439218843,
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU3YjE5M2Y3ZDM3MjQ5ZGQwY2E0OTMwYmRlMDcyM2Y4MmEzY2I1MWRmOWJlNDM4ZWY1MzQ1MWRhYzA5YTkzMCJ9fX0=")
                        .setCustomName(Text.of("Death Swap")),
                Deathswap::new));
    }
}
