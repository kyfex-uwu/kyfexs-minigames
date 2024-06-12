package com.kyfexuwu.kyfexs_minigames;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static void _try(Runnable toRun){
        try{
            toRun.run();
        }catch(Exception ignored){}
    }

    public record EasyNBTText(String text, String color,
        Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough, Boolean obfuscated){
        public String toNBTString(){
            return "{\"text\":"+NbtString.escape(this.text)+
                    (this.color==null?"":(",\"color\":\""+this.color+"\"")) +
                    (this.bold==null?"":(",\"bold\":"+this.bold)) +
                    (this.italic==null?"":(",\"italic\":"+this.italic)) +
                    (this.underlined==null?"":(",\"underlined\":"+this.underlined)) +
                    (this.strikethrough==null?"":(",\"strikethrough\":"+this.strikethrough)) +
                    (this.obfuscated==null?"":(",\"obfuscated\":"+this.obfuscated))+"}";
        }
        public static EasyNBTText fromStrNoI(String text){//from string, no italics
            return new EasyNBTText(text, null, null, false, null, null, null);
        }
        public static EasyNBTText fromStr(String text){
            return new EasyNBTText(text, null, null, null, null, null, null);
        }
    }
    public static EasyNBTText[][] easierNBTText(String text){
        var words = text.split(" ");
        if(words.length==0) return new EasyNBTText[][]{};
        var toReturn = new ArrayList<EasyNBTText[]>();
        var currLine=words[0];
        for(int i=1;i<words.length;i++){
            var word = words[i];
            if((currLine+" "+word).length()>25){
                toReturn.add(new EasyNBTText[]{EasyNBTText.fromStrNoI(currLine)});
                currLine=word;
            }else{
                currLine+=" "+word;
            }
        }
        toReturn.add(new EasyNBTText[]{EasyNBTText.fromStrNoI(currLine)});
        return toReturn.toArray(new EasyNBTText[0][0]);
    }
    public static ItemStack withLore(ItemStack stack, EasyNBTText[]... lines){
        var loreList = new NbtList();
        for(var lore : lines){
            List<String> sublist = new ArrayList<>();
            for(var section : lore){
                sublist.add(section.toNBTString());
            }
            loreList.add(NbtString.of("["+String.join(",", sublist)+"]"));
        }

        stack.getOrCreateSubNbt("display").put("Lore", loreList);
        return stack;
    }

    private static ItemStack withCustomModel(Item item, int data){
        var toReturn = item.getDefaultStack();
        toReturn.setSubNbt("CustomModelData", NbtInt.of(data));
        return toReturn;
    }
    public static final ItemStack UP_ARROW = withCustomModel(Items.WHITE_STAINED_GLASS_PANE, 2990);
    public static final ItemStack DOWN_ARROW = withCustomModel(Items.WHITE_STAINED_GLASS_PANE, 2991);
    public static final ItemStack LEFT_ARROW = withCustomModel(Items.WHITE_STAINED_GLASS_PANE, 2992);
    public static final ItemStack RIGHT_ARROW = withCustomModel(Items.WHITE_STAINED_GLASS_PANE, 2993);
}
