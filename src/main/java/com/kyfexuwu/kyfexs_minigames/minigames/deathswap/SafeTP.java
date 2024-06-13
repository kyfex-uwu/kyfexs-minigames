package com.kyfexuwu.kyfexs_minigames.minigames.deathswap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class SafeTP {
    public static Vec2i spiral(int i){
        if(i<=0) return new Vec2i(0,0);

        var phase = (int) Math.floor(Math.sqrt(i+0.25)-0.5);
        i-=(phase*phase)+phase;
        int sign=phase%2*2-1;//even -1, odd 1
        if(i<=phase+1) return new Vec2i((phase+1)/2*sign, ((phase + 1) / 2 - i) * sign);
        else return new Vec2i(((phase + 1) / 2 - (i-phase-1)) * sign, (phase + 2) / 2 * -sign);
    }
    public static class Vec2i{
        public int x;
        public int y;
        public Vec2i(int x, int y){
            this.x=x;
            this.y=y;
        }

        @Override
        public String toString() {
            return "("+this.x+", "+this.y+")";
        }
    }
    public static boolean teleport(ServerPlayerEntity player, ServerWorld world, int xP, int zP){
        return teleport(player, world, xP, zP, 25);
    }
    public static boolean teleport(ServerPlayerEntity player, ServerWorld world, int xP, int zP, int attempts){
        int counter=0;
        int x=xP;
        int z=zP;

        var teleported=false;
        while(counter<attempts){
            var destChunk = world.getChunk(x/16, z/16);
            var heightmap = destChunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
            for(int subX=-1;subX<=1;subX++){
                for(int subZ=-1;subZ<=1;subZ++){
                    var height = heightmap.get(8+subX,8+subZ);
                    if(height==destChunk.getBottomY()) continue;
                    var blockUnder = destChunk.getBlockState(new BlockPos(x+subX+8, height-1, z+subZ+8));
                    if(blockUnder.streamTags().anyMatch(tag->tag.equals(BlockTags.VALID_SPAWN))){
                        player.teleport(world, x+subX+8+0.5, height, z+subZ+8+0.5, 0, 0);
                        teleported=true;
                        break;
                    }
                }
                if(teleported) break;
            }
            if(teleported) break;

            var newPos = spiral(counter);
            x=xP+newPos.x*16;
            z=zP+newPos.y*16;
            counter++;
        }
        return teleported;
    }
}
