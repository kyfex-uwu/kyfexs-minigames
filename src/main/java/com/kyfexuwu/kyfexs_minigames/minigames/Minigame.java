package com.kyfexuwu.kyfexs_minigames.minigames;

import com.kyfexuwu.kyfexs_minigames.KyfexsMinigames;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class Minigame {
    public final ServerPlayerEntity leader;
    public final List<ServerPlayerEntity> players;

    /**
     * Creates a minigame. {@code players} is copied, so the list can be modified without
     * changing this minigame's players. {@code this.players} is unmodifiable.
     * @param leader
     * @param players
     */
    public Minigame(ServerPlayerEntity leader, List<ServerPlayerEntity> players){
        this.leader=leader;
        this.players=List.copyOf(players);
    }
    public abstract void onTick();
    public boolean finished=false;
    public void finish(){
        this.finished=true;
        KyfexsMinigames.finished.add(this);
    }

    //--

    public record MinigameData(
            String name,
            ItemStack buttonStack,
            BiFunction<ServerPlayerEntity, List<ServerPlayerEntity>, ? extends Minigame> initFunc){}
    public static Minigame startMinigame(Class<? extends Minigame> minigame,
                                         ServerPlayerEntity leader, List<ServerPlayerEntity> players){
        return minigameData.get(minigame).initFunc.apply(leader, players);
    }
    public static final Map<Class<? extends Minigame>, MinigameData> minigameData = new HashMap<>();
}
