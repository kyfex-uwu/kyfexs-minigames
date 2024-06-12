package com.kyfexuwu.kyfexs_minigames.minigames.deathswap;

import com.kyfexuwu.kyfexs_minigames.Utils;
import com.kyfexuwu.kyfexs_minigames.minigames.Minigame;
import com.kyfexuwu.kyfexs_minigames.mixin_helpers.SPEDeathListener;
import com.kyfexuwu.server_guis.*;
import com.kyfexuwu.server_guis.consumers.ClickConsumer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deathswap extends Minigame {

    public static class GameSettings{
        public boolean timeIsRandom=false;
        public int minSecs=60;
        public int maxSecs=60*5;
        public Kit kit = Kit.kits[0];
        public final Deathswap inst;
        public GameSettings(Deathswap inst){
            this.inst=inst;
        }
    }
    private final GameSettings settings = new GameSettings(this);

    private boolean started=false;
    private final List<ServerPlayerEntity> alivePlayers;
    private int timer;
    private record PreGameData(PlayerInventory inv, Vec3d pos, ServerWorld world){}
    private final Map<ServerPlayerEntity, PreGameData> preGameData = new HashMap<>();
    public Deathswap(ServerPlayerEntity leader, List<ServerPlayerEntity> players) {
        super(leader, players);
        this.alivePlayers = new ArrayList<>(this.players);
        config.buildAndOpen(this.leader, settings);
    }

    @Override
    public void onTick() {
        if(this.started) this.game();
    }
    private void game(){
        if(this.timer==20*10) this.sendToAllPlayers(new LiteralText("10 seconds left")
                .setStyle(Style.EMPTY.withColor(0x3aba7c)));
        if(this.timer==20*5) this.sendToAllPlayers(new LiteralText("5")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xb1c21d)).withBold(true)));
        if(this.timer==20*4) this.sendToAllPlayers(new LiteralText("4")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xc2af1d)).withBold(true)));
        if(this.timer==20*3) this.sendToAllPlayers(new LiteralText("3")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xc28b1d)).withBold(true)));
        if(this.timer==20*2) this.sendToAllPlayers(new LiteralText("2")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xc2591d)).withBold(true)));
        if(this.timer==20) this.sendToAllPlayers(new LiteralText("1")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xc2301d)).withBold(true)));
        if(this.timer<=0) this.swap();

        this.timer--;
    }
    private void start(){
        var world = this.leader.getWorld();
        for(int i=0;i<this.players.size();i++){
            var player = this.players.get(i);
            var currInv = new PlayerInventory(null);
            player.getInventory().clone(currInv);
            this.preGameData.put(player, new PreGameData(currInv, player.getPos(), player.getWorld()));
            player.getInventory().clear();

            //find good spot for player and put them there
            var tpPos = SafeTP.spiral(i);
            SafeTP.teleport(player, world,
                    tpPos.x*200000+(int)(Math.random()*20000)-10000,
                    tpPos.y*200000+(int)(Math.random()*20000)-10000);

            this.settings.kit.init(player);

            ((SPEDeathListener)player).addDeathOrLogoutListener(()->{
                this.alivePlayers.remove(player);
                this.finishPlayer(player);

                if(this.alivePlayers.size()<=1){
                    this.finish();
                }else{
                    this.sendToAllPlayers(Text.of(this.alivePlayers.size()+" players left"));
                }

                return true;
            });
        }

        this.resetTimer();
        this.sendToAllPlayers(Text.of("Starting Deathswap!"));
        this.started=true;
    }

    @Override
    public void finish(){
        super.finish();

        for(var player : this.alivePlayers){
            this.finishPlayer(player);
        }

        if(this.alivePlayers.size()>1){
            this.alivePlayers.addAll(this.players);
            this.sendToAllPlayers(Text.of("Game ended prematurely (womp wah)"));
            return;
        }

        if(this.alivePlayers.size()==1){
            var winner = this.alivePlayers.get(0);
            this.alivePlayers.clear();
            this.alivePlayers.addAll(this.players);
            this.sendToAllPlayers(Text.of(winner.getName().getString()+" wins!"));
        }
    }
    private void finishPlayer(ServerPlayerEntity player){
        this.settings.kit.uninit(player);
        try {
            var preGameData = this.preGameData.get(player);

            player.getInventory().clone(preGameData.inv);
            player.teleport(preGameData.world,preGameData.pos.x,preGameData.pos.y,preGameData.pos.z,0,0);
        }catch(Exception ignored){}
    }

    private void sendToAllPlayers(Text message){
        for(var player : this.alivePlayers){
            player.sendMessage(message,false);
        }
    }
    private record SwapPos(Vec3d pos, float pitch, float yaw, ServerWorld world){}
    private void resetTimer(){
        this.timer = 20*((int)(Math.random()*(this.settings.maxSecs-this.settings.minSecs))+this.settings.minSecs);
    }
    private void swap(){
        this.resetTimer();

        var swapMap = new ArrayList<SwapPos>(this.alivePlayers.size());
        int offset = (int) Math.floor((this.alivePlayers.size()-1)*Math.random())+1;
        for(int i=0;i<this.alivePlayers.size();i++){
            var player = this.alivePlayers.get((i+offset)%this.alivePlayers.size());
            swapMap.add(new SwapPos(player.getPos(), player.getPitch(), player.getYaw(), player.getWorld()));
        }
        for(int i=0;i<this.alivePlayers.size();i++){
            var player = this.alivePlayers.get(i);
            var dest = swapMap.get(i);
            player.teleport(dest.world, dest.pos.x, dest.pos.y, dest.pos.z, dest.yaw, dest.pitch);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20*5, 3,
                    false, false));
        }
    }

    //--

    private static String formattedTime(int seconds){
        return (seconds/60)+":"+(seconds%60<10?"0":"")+(seconds%60);
    }
    private static final InvGUI.Template<GameSettings> config = new InvGUI.Template<GameSettings>(
            ServerGUIs.ScreenType.GENERIC_9X5, Text.of("Config"),
            InvGUIItem.decode("#########" +
                    "#  #Aa#r#" +
                    "#  #Dd###" +
                    "#  #Ss#B#" +
                    "#########",
                    new InvGUIItem.InvGUIEntry('#', ServerGUIs.IMMOVABLE),
                    new InvGUIItem.InvGUIEntry('r', new RenderedInvGUIItem<GameSettings>(
                            (serverPlayerEntity, invGUI, arg) -> arg.timeIsRandom?
                                    Utils.withLore(Items.END_CRYSTAL.getDefaultStack()
                                                    .setCustomName(Text.of("Random Intervals")),
                                            Utils.easierNBTText("Players will have a random amount of time " +
                                                    "before the swap. Click to toggle")):
                                    Utils.withLore(Items.REPEATER.getDefaultStack()
                                                    .setCustomName(Text.of("Set Interval")),
                                            Utils.easierNBTText("Players will have the same amount of time " +
                                                    "before the swap. Click to toggle")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                argument.timeIsRandom=!argument.timeIsRandom;
                                thisInv.getHandler().refresh();
                            }
                    )),new InvGUIItem.InvGUIEntry('B', new StaticInvGUIItem(
                            Items.GREEN_GLAZED_TERRACOTTA, Text.of("Start"), 1,
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                var cArg = ((GameSettings) argument);
                                if(!cArg.timeIsRandom) cArg.minSecs=cArg.maxSecs;
                                cArg.inst.start();
                                thisInv.getHandler().closeQuietly();
                            })),

                    new InvGUIItem.InvGUIEntry('D', new RenderedInvGUIItem<>(
                            (serverPlayerEntity, invGUI, arg) -> Utils.withLore(Items.CLOCK.getDefaultStack()
                                    .setCustomName(Text.of((arg.timeIsRandom?"Max Time - ":"Time - ")+
                                            formattedTime(arg.maxSecs))),
                                    Utils.easierNBTText("The "+(arg.timeIsRandom?"maximum ":"")+
                                            "amount of time before the swap")),
                            (ClickConsumer<GameSettings>) ServerGUIs.nothingClick()
                    )),
                    new InvGUIItem.InvGUIEntry('A', new RenderedInvGUIItem<GameSettings>(
                            (serverPlayerEntity, invGUI, arg) -> Utils.UP_ARROW.copy()
                                    .setCustomName(Text.of("+10s")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                argument.maxSecs += 10;
                                thisInv.getHandler().refresh();
                            }
                    )),
                    new InvGUIItem.InvGUIEntry('S', new RenderedInvGUIItem<GameSettings>(
                            (serverPlayerEntity, invGUI, arg) -> Utils.DOWN_ARROW.copy()
                                    .setCustomName(Text.of("-10s")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                argument.maxSecs = Math.max(10, argument.maxSecs - 10);
                                argument.minSecs = Math.min(argument.maxSecs, argument.minSecs);
                                thisInv.getHandler().refresh();
                            }
                    )),

                    new InvGUIItem.InvGUIEntry('d', new RenderedInvGUIItem<>(
                            (serverPlayerEntity, invGUI, arg) -> !arg.timeIsRandom?ItemStack.EMPTY:
                                    Utils.withLore(Items.CLOCK.getDefaultStack().setCustomName(Text.of(
                                            "Min Time - "+formattedTime(arg.minSecs))),
                                            Utils.easierNBTText("The minimum amount of time before the swap")),
                            (ClickConsumer<GameSettings>) ServerGUIs.nothingClick()
                    )),
                    new InvGUIItem.InvGUIEntry('a', new RenderedInvGUIItem<GameSettings>(
                            (serverPlayerEntity, invGUI, arg) -> !arg.timeIsRandom?ItemStack.EMPTY:
                                    Utils.UP_ARROW.copy().setCustomName(Text.of("+10s")),
                            (slotIndex, button, actionType, player, thisInv, argument) -> {
                                if(!argument.timeIsRandom) return;
                                argument.minSecs+=10;
                                argument.maxSecs=Math.max(argument.maxSecs, argument.minSecs);
                                thisInv.getHandler().refresh();
                            }
                    )),
                    new InvGUIItem.InvGUIEntry('s', new RenderedInvGUIItem<GameSettings>(
                            (serverPlayerEntity, invGUI, arg) -> !arg.timeIsRandom?ItemStack.EMPTY:
                                    Utils.DOWN_ARROW.copy().setCustomName(Text.of("-10s")),
                            (slotIndex, button, actionType, player, thisInv, argument) ->{
                                if(!argument.timeIsRandom) return;
                                argument.minSecs=Math.max(10, argument.minSecs-10);
                                thisInv.getHandler().refresh();
                            }
                    ))
            )).toBuild((player, template, argument)->{
                        var items=template.items.clone();
                        for(int y=0;y<3;y++){
                            for(int x=0;x<2;x++){
                                items[10+y*9+x] = Kit.kitSelector(y*2+x);
                            }
                        }

                        return new InvGUI<>(template.type,template.title,items);
                    }).onClose((player, thisInv, argument) -> {
                        argument.inst.finish();
                    });
}
