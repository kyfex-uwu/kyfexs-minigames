package com.kyfexuwu.kyfexs_minigames.minigames.deathswap.items;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfiniteRocket extends FireworkRocketItem implements PolymerItem {
    public InfiniteRocket(Settings settings) {
        super(settings);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.FIREWORK_ROCKET;
    }

    private static final ItemStack stack;
    static{
        stack = Items.FIREWORK_ROCKET.getDefaultStack();
        stack.setCustomName(new LiteralText("Infinite Rocket").setStyle(Style.EMPTY
                .withColor(TextColor.parse("aqua"))
                .withItalic(false)));

        var eList = new NbtList();
        eList.add(new NbtCompound());
        stack.getOrCreateNbt().put("Enchantments", eList);
    }
    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return stack;
    }

    //--

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.success(context.getWorld().isClient);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isFallFlying()) {
            world.spawnEntity(new FireworkRocketEntity(world, user.getStackInHand(hand), user));
            return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
        } else {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new LiteralText("Use this rocket infinte times; it'll never run out!"));
    }
}
