package com.kyfexuwu.kyfexs_minigames;

import com.kyfexuwu.kyfexs_minigames.minigames.Minigame;
import com.kyfexuwu.server_guis.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class Screens {
    private static InvGUIItem minigameButton(Class<? extends Minigame> minigame){
        var icon = Minigame.minigameData.get(minigame).buttonStack();
        return new StaticInvGUIItem(icon, (slotIndex, button, actionType, player, thisInv, argument) -> {
            if(false){//if player in minigame
                player.closeHandledScreen();
                player.sendMessage(Text.of("Minigame in progress!"),
                        false);
                return;
            }

            playerSelector.buildAndOpen(player, new PlayerSelectorData(minigame, player));
        });
    }
    static final InvGUI.Template<Class<? extends Minigame>> minigameSelector = new InvGUI.Template<Class<? extends Minigame>>(
            ServerGUIs.ScreenType.GENERIC_9X5, Text.of("Choose Minigame"),
            InvGUIItem.decode("#########" +
                            "#       #" +
                            "#       #" +
                            "#       #" +
                            "#########",
                    new InvGUIItem.InvGUIEntry('#', ServerGUIs.IMMOVABLE),
                    new InvGUIItem.InvGUIEntry(' ', ServerGUIs.EMPTY)))
            .toBuild((player, template, argument) -> {
                var items = template.items.clone();

                int minigameIndex=0;
                var keys = Minigame.minigameData.keySet().stream().toList();
                for(int y=0;y<3;y++){
                    for(int x=0;x<7;x++){
                        items[(y+1)*9+x+1] = minigameButton(keys.get(minigameIndex));

                        minigameIndex++;
                        if(minigameIndex>=keys.size()) break;
                    }
                    if(minigameIndex>=keys.size()) break;
                }

                return new InvGUI<>(template.type,template.title,items);
            });
    private static class PlayerSelectorData{
        public final Class<? extends Minigame> minigame;
        public final ServerPlayerEntity leader;
        public int addedPage=0;
        public int allPage=0;
        public final List<ServerPlayerEntity> players = new ArrayList<>();
        public PlayerSelectorData(Class<? extends Minigame> minigame, ServerPlayerEntity leader){
            this.minigame=minigame;
            this.leader=leader;
            this.players.add(leader);
        }
    }
    static InvGUIItem removePlayerButton(Supplier<List<ServerPlayerEntity>> playerSupplier, Supplier<Integer> indexSupplier){
        return new RenderedInvGUIItem<>((serverPlayerEntity, invGUI, o) -> {
            var index = indexSupplier.get();
            var players = playerSupplier.get();
            if(index<0||index>=players.size()) return ItemStack.EMPTY;
            var player = players.get(index);

            return ServerGUIs.getPlayerHead(player.getName().getString())
                    .setCustomName(Text.of("Remove " + player.getName().getString()));
        }, (slotIndex, button, actionType, player, thisInv, argument) -> {
            var index = indexSupplier.get();
            var players = playerSupplier.get();
            if(index<0||index>=players.size()) return;
            var clickedPlayer = players.get(index);
            if(clickedPlayer.equals(player)) return;

            var cArg = (PlayerSelectorData) argument;
            cArg.players.remove(clickedPlayer);
            Utils._try(()->thisInv.getHandler().refresh());
        });
    }
    static InvGUIItem addPlayerButton(Supplier<List<ServerPlayerEntity>> playerSupplier, Supplier<Integer> indexSupplier){
        return new RenderedInvGUIItem<>((serverPlayerEntity, invGUI, o) -> {
            var index = indexSupplier.get();
            var players = playerSupplier.get();
            if(index<0||index>=players.size()) return ItemStack.EMPTY;
            var player = players.get(index);

            return ServerGUIs.getPlayerHead(player.getName().getString())
                    .setCustomName(Text.of("Invite " + player.getName().getString()));
        }, (slotIndex, button, actionType, player, thisInv, argument) -> {
            var index = indexSupplier.get();
            var players = playerSupplier.get();
            if(index<0||index>=players.size()) return;
            var clickedPlayer = players.get(index);

            var cArg = (PlayerSelectorData) argument;
            if(!cArg.players.contains(clickedPlayer))
                ServerEvent.send(player.getName().getString()+" would like you to join their game of "+
                        Minigame.minigameData.get(cArg.minigame).name(), clickedPlayer, ()->{
                    if(cArg.players.contains(clickedPlayer)) return;

                    clickedPlayer.sendMessage(Text.of("Joined "+player.getName().getString()+"'s " +
                            "game of "+Minigame.minigameData.get(cArg.minigame).name()), false);
                    cArg.players.add(clickedPlayer);
                    Utils._try(()->thisInv.getHandler().refresh());
                });
        });
    }
    static final InvGUI.Template<PlayerSelectorData> playerSelector = new InvGUI.Template<PlayerSelectorData>(
            ServerGUIs.ScreenType.GENERIC_9X6, Text.of("Invite Players"),
            InvGUIItem.decode("######sc#" +
                            "l       r" +
                            "#########" +
                            "L       R" +
                            "#       #" +
                            "#########",
                    new InvGUIItem.InvGUIEntry('#', ServerGUIs.IMMOVABLE),
                    new InvGUIItem.InvGUIEntry(' ', ServerGUIs.EMPTY),
                    new InvGUIItem.InvGUIEntry('r', new StaticInvGUIItem(
                            Utils.RIGHT_ARROW.copy().setCustomName(Text.of("Next")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                var cArg=((PlayerSelectorData)argument);

                                cArg.addedPage=Math.min(cArg.addedPage+1, cArg.players.size()/7);
                                thisInv.getHandler().refresh();
                            })),
                    new InvGUIItem.InvGUIEntry('R', new StaticInvGUIItem(
                            Utils.RIGHT_ARROW.copy().setCustomName(Text.of("Next")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                var cArg=((PlayerSelectorData)argument);

                                cArg.allPage++;
                                thisInv.getHandler().refresh();
                            })),
                    new InvGUIItem.InvGUIEntry('l', new StaticInvGUIItem(
                            Utils.LEFT_ARROW.copy().setCustomName(Text.of("Previous")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                var cArg=((PlayerSelectorData)argument);

                                cArg.addedPage=Math.max(cArg.addedPage-1, 0);
                                thisInv.getHandler().refresh();
                            })),
                    new InvGUIItem.InvGUIEntry('L', new StaticInvGUIItem(
                            Utils.LEFT_ARROW.copy().setCustomName(Text.of("Previous")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                var cArg=((PlayerSelectorData)argument);

                                cArg.allPage=Math.max(cArg.allPage-1,0);
                                thisInv.getHandler().refresh();
                            })),
                    new InvGUIItem.InvGUIEntry('s', new StaticInvGUIItem(
                            Items.GREEN_STAINED_GLASS_PANE, Text.of("Start"), 1,
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                player.closeHandledScreen();

                                var cArg = ((PlayerSelectorData)argument);
                                KyfexsMinigames.currMinigames.add(
                                        Minigame.startMinigame(cArg.minigame, cArg.leader, cArg.players));
                            })),
                    new InvGUIItem.InvGUIEntry('c', new StaticInvGUIItem(
                            Items.RED_STAINED_GLASS_PANE, Text.of("Close"), 1,
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                player.closeHandledScreen();
                            }))))
            .toBuild((player, template, argument) -> {
                var items = template.items.clone();

                for(int i=0;i<7;i++){
                    int finalI = i;
                    items[10+i] = removePlayerButton(()->{
                        var toReturn = new ArrayList<>(argument.players);
                        toReturn.remove(player);
                        return toReturn;
                    }, ()-> finalI +argument.addedPage*7);
                }

                var manager = player.server.getPlayerManager();
                for(int i=0;i<14;i++){
                    int finalI = i;
                    items[28 + i + (i / 7 * 2)] = addPlayerButton(()->
                            manager.getPlayerList().stream().filter(p->!argument.players.contains(p))
                                    .sorted(Comparator.comparing(o -> o.getName().getString()))
                                    .toList(), ()->finalI + argument.allPage * 14);
                }

                return new InvGUI<>(template.type,template.title,items);
            });
}
