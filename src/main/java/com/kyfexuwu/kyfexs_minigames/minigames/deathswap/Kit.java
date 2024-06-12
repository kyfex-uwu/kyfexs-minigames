package com.kyfexuwu.kyfexs_minigames.minigames.deathswap;

import com.kyfexuwu.kyfexs_minigames.Utils;
import com.kyfexuwu.server_guis.RenderedInvGUIItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public abstract class Kit {
    public static final Kit[] kits = {
            new Kit(Items.BARRIER.getDefaultStack().setCustomName(Text.of("None"))){},
            new Kit(Items.LEATHER_HELMET.getDefaultStack().setCustomName(Text.of("Basic"))){
                @Override
                public void init(ServerPlayerEntity player) {
                    super.init(player);

                    this.giveItems(player,
                            Items.IRON_LEGGINGS.getDefaultStack(),
                            Items.IRON_CHESTPLATE.getDefaultStack(),
                            Items.IRON_PICKAXE.getDefaultStack(),
                            Kit.withCount(Items.BREAD.getDefaultStack(), 10));
                }
            },
            new Kit(Items.DIAMOND_CHESTPLATE.getDefaultStack().setCustomName(Text.of("To The Nines"))) {
                private double health;
                @Override
                public void init(ServerPlayerEntity player) {
                    super.init(player);

                    this.giveItems(player,
                            Items.DIAMOND_CHESTPLATE.getDefaultStack(),
                            Items.DIAMOND_BOOTS.getDefaultStack(),
                            Items.DIAMOND_LEGGINGS.getDefaultStack(),
                            Items.DIAMOND_HELMET.getDefaultStack(),

                            Items.DIAMOND_PICKAXE.getDefaultStack(),
                            Items.DIAMOND_SWORD.getDefaultStack(),

                            Kit.withCount(Items.GOLDEN_CARROT.getDefaultStack(), 32),
                            Items.WATER_BUCKET.getDefaultStack(),
                            Kit.withCount(Items.TORCH.getDefaultStack(), 64));

                    var maxHealth=player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    this.health=maxHealth.getBaseValue();
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(30);
                }

                @Override
                public void uninit(ServerPlayerEntity player) {
                    super.uninit(player);
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(this.health);
                }
            },
            new Kit(Items.FIREWORK_ROCKET.getDefaultStack().setCustomName(Text.of("High Flier"))) {
                @Override
                public void init(ServerPlayerEntity player) {
                    super.init(player);

                    var elytra = Items.ELYTRA.getDefaultStack();
                    elytra.addEnchantment(Enchantments.BINDING_CURSE, 1);
                    elytra.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    var boots = Items.LEATHER_BOOTS.getDefaultStack();
                    boots.addEnchantment(Enchantments.FEATHER_FALLING, 5);
                    elytra.addEnchantment(Enchantments.BINDING_CURSE, 1);
                    elytra.addEnchantment(Enchantments.VANISHING_CURSE, 1);
                    this.giveRemovableItems(player,
                            elytra,
                            boots);
                }
            }
    };
    public final ItemStack icon;

    public Kit(ItemStack icon) {
        this.icon = icon;
    }

    static RenderedInvGUIItem<Deathswap.GameSettings> kitSelector(int index){
        return new RenderedInvGUIItem<>((player, invGui, arg) -> {
            if(index>=kits.length) return ItemStack.EMPTY;

            var toReturn = kits[index].icon.copy();
            if(arg.kit==kits[index]){
                var eList = new NbtList();
                eList.add(new NbtCompound());
                toReturn.setSubNbt("Enchantments", eList);

                var display = toReturn.getOrCreateSubNbt("display");
                if(display.get("Lore")==null) display.put("Lore", new NbtList());
                display.getList("Lore", 8).add(NbtString.of("["+
                        new Utils.EasyNBTText("Selected", "green",
                                false, false, false, false, false)
                                .toNBTString()+"]"));
            }
            return toReturn;
        }, (slotIndex, button, actionType, player, thisInv, argument) -> {
            if(index>=kits.length) return;

            argument.kit = kits[index];
            thisInv.getHandler().refresh();
        });
    }

    public void init(ServerPlayerEntity player) {
    }

    private final Map<ServerPlayerEntity, List<ItemStack>> toRemove = new HashMap<>();
    private static final Inventory fakeCrafting = new SimpleInventory(0);
    public void uninit(ServerPlayerEntity player) {
        var items = toRemove.get(player);
        if(items==null) return;
        for(var item : items) player.getInventory().remove(s->s==item, Integer.MAX_VALUE, fakeCrafting);
    }

    public void giveItems(ServerPlayerEntity player, ItemStack... items) {
        for (var item : items) {
            if (player.canEquip(item)) {
                player.equipStack(MobEntity.getPreferredEquipmentSlot(item), item);
            } else {
                player.giveItemStack(item.copy());
            }
        }
    }
    public void giveRemovableItems(ServerPlayerEntity player, ItemStack... items){
        if(!this.toRemove.containsKey(player))
            this.toRemove.put(player, new ArrayList<>());

        this.toRemove.get(player).addAll(Arrays.asList(items));
        giveItems(player, items);
    }
    public static ItemStack withCount(ItemStack stack, int count){
        stack.setCount(count);
        return stack;
    }
}
