package com.lulan.shincolle.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

/** show mats amount in the heavy grudge block
 */
public class ItemBlockGrudgeHeavy extends BasicItemBlock
{

	
	public ItemBlockGrudgeHeavy(Block block)
	{
		super(block);
	}
	
	//display egg information
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
    {
    	int[] mats = new int[4];
    	
    	if (itemstack.hasTagCompound())
    	{ 	//正常製造egg, 會有四個材料tag		
    		mats = itemstack.getTagCompound().getIntArray("mats"); 
    		
    		list.add(TextFormatting.WHITE + "" + mats[0] + " " + I18n.format("item.shincolle:Grudge.name"));
            list.add(TextFormatting.RED + "" + mats[1] + " " + I18n.format("item.shincolle:AbyssMetal.name"));
            list.add(TextFormatting.GREEN + "" + mats[2] + " " + I18n.format("item.shincolle:Ammo.name"));
            list.add(TextFormatting.AQUA + "" + mats[3] + " " + I18n.format("item.shincolle:AbyssMetal1.name"));
        }
    	
    }
    
    
}
