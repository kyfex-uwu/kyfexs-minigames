package com.kyfexuwu.kyfexs_minigames.minigames.manhunt;

import com.kyfexuwu.kyfexs_minigames.minigames.Minigame;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class Manhunt extends Minigame {
    public Manhunt(ServerPlayerEntity leader, List<ServerPlayerEntity> players) {
        super(leader, players);
    }

    @Override
    public void onTick() {

    }
}
