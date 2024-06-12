package com.kyfexuwu.kyfexs_minigames;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerEvent {
    public static final String serverEventCommand = "kyfexsminigames:serverevent";
    private static Map<UUID, Map<Integer, Runnable>> playerMessages = new HashMap<>();
    public static void send(String message, ServerPlayerEntity player, Runnable onClick){
        if(!playerMessages.containsKey(player.getUuid())) playerMessages.put(player.getUuid(), new HashMap<>());

        int messageID;
        do{
            messageID = (int) (Math.random()*Integer.MAX_VALUE);
        }while(playerMessages.get(player.getUuid()).containsKey(messageID));
        playerMessages.get(player.getUuid()).put(messageID, onClick);

        var toSend = new LiteralText(message).setStyle(Style.EMPTY
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/"+serverEventCommand+" "+messageID)));
        player.sendMessage(toSend, false);
    }
    public static int processEvent(ServerPlayerEntity player, int id){
        if(!playerMessages.containsKey(player.getUuid())) return 0;

        var toRun = playerMessages.get(player.getUuid()).get(id);
        if(toRun==null) return 0;

        toRun.run();
        playerMessages.get(player.getUuid()).remove(id);
        return 1;
    }
}
