package com.kyfexuwu.kyfexs_minigames;

import com.kyfexuwu.kyfexs_minigames.minigames.Minigame;
import com.kyfexuwu.kyfexs_minigames.minigames.Minigames;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class KyfexsMinigames implements DedicatedServerModInitializer {
    public static List<Minigame> currMinigames = new ArrayList<>();
    public static List<Minigame> finished = new ArrayList<>();
    public static final String namespace = "kyfexs_minigames";
    @Override
    public void onInitializeServer() {
        Minigames.register();
        CustomItems.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                dispatcher.register(CommandManager.literal("minigame").executes(context -> {
                    var maybeSourceEntity = context.getSource().getEntity();
                    if(!(maybeSourceEntity instanceof ServerPlayerEntity sourceEntity)) return 0;

                    Screens.minigameSelector.buildAndOpen(sourceEntity, null);
                    return 1;
                }))
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                dispatcher.register(CommandManager.literal(ServerEvent.serverEventCommand)
                        .then(CommandManager.argument("eventId", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            if(!(context.getSource().getEntity() instanceof ServerPlayerEntity)) return 0;

                            try{
                                int id = context.getArgument("eventId", Integer.class);
                                return ServerEvent.processEvent(context.getSource().getPlayer(), id);
                            }catch (Exception e){
                                return 0;
                            }
                        })))
        );

        ServerTickEvents.END_SERVER_TICK.register(world -> {
            for(var minigame : currMinigames) if(!minigame.finished) minigame.onTick();
            for(var minigame : finished) currMinigames.remove(minigame);
        });
    }
}
