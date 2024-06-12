package com.kyfexuwu.kyfexs_minigames.mixin;

import com.kyfexuwu.kyfexs_minigames.mixin_helpers.SPEDeathListener;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Supplier;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements SPEDeathListener {
    @Unique
    public final ArrayList<Supplier<Boolean>> onDeath = new ArrayList<>();

    private void onDeathOrLogout(){
        for(int i=0;i<this.onDeath.size();i++){
            if(this.onDeath.get(i).get()){
                this.onDeath.remove(i);
                i--;
            }
        }
    }

    @Inject(at=@At("RETURN"), method = "onDeath")
    public void kyfexs_minigames__onDeath(DamageSource source, CallbackInfo ci){
        this.onDeathOrLogout();
    }
    @Inject(at=@At("RETURN"), method = "onDisconnect")
    public void kyfexs_minigames__onDisconnect(CallbackInfo ci){
        this.onDeathOrLogout();
    }

    @Override
    public void addDeathOrLogoutListener(Supplier<Boolean> listener) {
        this.onDeath.add(listener);
    }
}
