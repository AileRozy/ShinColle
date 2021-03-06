package com.lulan.shincolle.utility;

import com.lulan.shincolle.capability.CapaShipInventory;
import com.lulan.shincolle.client.gui.inventory.ContainerShipInventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.oredict.OreDictionary;

/**
 * itemstack and slots helper
 */
public class InventoryHelper
{

	
	public InventoryHelper() {}
	
	/**
	 * check inventory has more items than temp setting
	 * 
	 * if excess TRUE (excess mode)
	 *   targetStacks is null = return true
	 *   targetStacks isn't null = check specified itemstack in inventory
	 *   return TRUE if all target amount is more than temp setting
	 *   
	 * if excess FALSE (remain mode)
	 */
	public static boolean checkInventoryAmount(IInventory inv, ItemStack[] tempStacks, boolean[] modeStacks, boolean checkMetadata, boolean checkNbt, boolean checkOredict, boolean excess)
	{
		if (inv == null) return true;
		
		boolean noTempItem = true;
		int[] targetAmount = new int[9];
		
		//null cehck
		if (tempStacks == null || tempStacks.length != 9)
		{
			return true;
		}
		else
		{
			//check itemstack temp setting
			for (int i = 0; i < 9; i++)
			{
				//if temp stack existed
				if (tempStacks[i] != null)
				{
					noTempItem = false;
					
					//ignore NOT mode item
					if (!modeStacks[i])
					{
						//calc the total number of target item
						targetAmount[i] = calcItemStackAmount(inv, tempStacks[i], checkMetadata, checkNbt, checkOredict);
					}
				}
				
				//if no specified itemstack, return true
				if (i == 8 && noTempItem)
				{
					return true;
				}//end temp all null
			}//end loop all temp slots
		}
		
		for (int i = 0; i < 9; i++)
		{
			//EXCESS MODE: all item amount must GREATER or EQUAL to temp setting
			if (excess)
			{
				if (tempStacks[i] != null && !modeStacks[i])
				{
					if (targetAmount[i] < tempStacks[i].stackSize) return false;
				}
			}
			//REMAIN MODE: all item amount must LESSER or EQUAL to temp setting
			else
			{
				if (tempStacks[i] != null && !modeStacks[i])
				{
					if (targetAmount[i] > tempStacks[i].stackSize) return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * check all itemstack in the inventory is full, empty or not fluid container
	 * 
	 * checkFull:
	 *   true: check all container is full or not container
	 *   false: check all container is empty or not container
	 */
	public static boolean checkFluidContainer(IInventory inv, FluidStack targetFluid, boolean checkFull)
	{
		if (inv == null) return true;
		
		//inventory is ship inv
		if(inv instanceof CapaShipInventory)
		{
			CapaShipInventory shipInv = (CapaShipInventory) inv;
			
			for (int i = ContainerShipInventory.SLOTS_SHIPINV; i < shipInv.getSizeInventoryPaged(); i++)
			{
				//check all slots are full
				if (checkFull)
				{
					if (!checkFluidContainer(shipInv.getStackInSlotWithoutPaging(i), targetFluid, true))
						return false;
				}
				//check all slots are empty
				else
				{
					if (!checkFluidContainer(shipInv.getStackInSlotWithoutPaging(i), targetFluid, false))
						return false;
				}
			}
		}
		//inventory is vanilla chest
		else if (inv instanceof TileEntityChest)
		{
			//check main chest
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				//check all slots are full
				if (checkFull)
				{
					if (!checkFluidContainer(inv.getStackInSlot(i), targetFluid, true))
						return false;
				}
				//check all slots are empty
				else
				{
					if (!checkFluidContainer(inv.getStackInSlot(i), targetFluid, false))
						return false;
				}
			}
			
			//check adj chest
			TileEntityChest chest2 = TileEntityHelper.getAdjChest((TileEntityChest) inv);
			
			if (chest2 != null)
			{
				for (int i = 0; i < chest2.getSizeInventory(); i++)
				{
					//check all slots are full
					if (checkFull)
					{
						if (!checkFluidContainer(chest2.getStackInSlot(i), targetFluid, true))
							return false;
					}
					//check all slots are empty
					else
					{
						if (!checkFluidContainer(chest2.getStackInSlot(i), targetFluid, false))
							return false;
					}
				}
			}
		}
		//other inventory
		else
		{
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				//check all slots are full
				if (checkFull)
				{
					if (!checkFluidContainer(inv.getStackInSlot(i), targetFluid, true))
						return false;
				}
				//check all slots are empty
				else
				{
					if (!checkFluidContainer(inv.getStackInSlot(i), targetFluid, false))
						return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * checkFull = TRUE:  return FALSE if itemstack can accept target fluid
	 *           = FALSE: return FALSE if itemstack can not accept target fluid
	 */
	public static boolean checkFluidContainer(ItemStack stack, FluidStack targetFluid, boolean checkFull)
	{
		if (stack != null)
		{
			//if item has fluid capability
			if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP))
			{
				IFluidHandler fh = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
				if (fh == null) return true;	//is same with non fluid container
				
				IFluidTankProperties[] tanks = fh.getTankProperties();
				if (tanks == null) return true;	//is same with non fluid container
				
				//check fluid amount in all tanks
				for (IFluidTankProperties tank : tanks)
				{
					FluidStack fstack = tank.getContents();
					
					//check container is full
					if (checkFull)
					{
						//if container can be filled = it's not full
						if (fstack == null ||
							tank.canFill() && fstack.amount < tank.getCapacity() &&
							(targetFluid == null || targetFluid.equals(fstack)))
						{
							return false;
						}
					}
					//check container is empty
					else
					{
						if (fstack != null && tank.canDrain() && fstack.amount > 0)
						{
							return false;
						}
					}
				}
			}//end get fluid capa
		}
		
		return true;
	}
	
	/** check inventory is full */
	public static boolean checkInventoryFull(IInventory inv)
	{
		if (inv == null) return true;
		
		int i = 0;
		
		//inventory is ship inv
		if(inv instanceof CapaShipInventory)
		{
			CapaShipInventory shipInv = (CapaShipInventory) inv;
			
			//get any empty slot = false
			for (i = ContainerShipInventory.SLOTS_SHIPINV; i < shipInv.getSizeInventoryPaged(); i++)
			{
				if (shipInv.getStackInSlotWithoutPaging(i) == null) return false;
			}
		}
		//inventory is vanilla chest
		else if (inv instanceof TileEntityChest)
		{
			//check main chest
			for (i = 0; i < inv.getSizeInventory(); i++)
			{
				if (inv.getStackInSlot(i) == null) return false;
			}
			
			//check adj chest
			TileEntityChest chest2 = TileEntityHelper.getAdjChest((TileEntityChest) inv);
			
			if (chest2 != null)
			{
				for (i = 0; i < chest2.getSizeInventory(); i++)
				{
					if (chest2.getStackInSlot(i) == null) return false;
				}
			}
		}
		//other inventory
		else
		{
			for (i = 0; i < inv.getSizeInventory(); i++)
			{
				if (inv.getStackInSlot(i) == null) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * check inventory is empty
	 * 
	 * if targetStacks is null = check all slots is null
	 * if targetStacks isn't null = check no specified itemstack in inventory
	 * 
	 * modeStacks = TRUE: itemstack is NOT mode
	 *              FALSE: itemstack is NORMAL mode
	 */
	public static boolean checkInventoryEmpty(IInventory inv, ItemStack[] tempStacks, boolean[] modeStacks, boolean checkMetadata, boolean checkNbt, boolean checkOredict)
	{
		if (inv == null) return true;
		
		boolean noTempItem = true;
		
		//null cehck
		if (tempStacks == null || tempStacks.length != 9 || modeStacks == null || modeStacks.length != 9)
		{
			return isAllSlotNull(inv, tempStacks, modeStacks, checkMetadata, checkNbt, checkOredict);
		}
		//check specified itemstack in inventory
		else
		{
			for (int i = 0; i < 9; i++)
			{
				if (tempStacks[i] != null)
				{
					noTempItem = false;
					
					//ignore NOT mode item
					if (!modeStacks[i])
					{
						//stack found, chest is not empty
						if (matchTargetItem(inv, tempStacks[i], checkMetadata, checkNbt, checkOredict))
							return false;
					}
				}
				
				//if no specified itemstack, check all slot is null
				if (i == 8 && noTempItem)
				{
					return isAllSlotNull(inv, tempStacks, modeStacks, checkMetadata, checkNbt, checkOredict);
				}//end temp all null
			}//end loop all temp slots
		}
		
		return true;
	}
	
	/**
	 * return TRUE if all slot is null or NOT mode item
	 */
	public static boolean isAllSlotNull(IInventory inv, ItemStack[] targetStacks, boolean[] modeStacks, boolean checkMetadata, boolean checkNbt, boolean checkOredict)
	{
		//inventory is ship inv
		if(inv instanceof CapaShipInventory)
		{
			CapaShipInventory shipInv = (CapaShipInventory) inv;
			
			//get any empty slot = false
			for (int i = ContainerShipInventory.SLOTS_SHIPINV; i < shipInv.getSizeInventoryPaged(); i++)
			{
				if (shipInv.getStackInSlotWithoutPaging(i) != null) return false;
			}
		}
		//inventory is vanilla chest
		else if (inv instanceof TileEntityChest)
		{
			//check main chest
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (inv.getStackInSlot(i) != null) return false;
			}
			
			//check adj chest
			TileEntityChest chest2 = TileEntityHelper.getAdjChest((TileEntityChest) inv);
			
			if (chest2 != null)
			{
				for (int i = 0; i < chest2.getSizeInventory(); i++)
				{
					if (chest2.getStackInSlot(i) != null) return false;
				}
			}
		}
		//other inventory
		else
		{
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (inv.getStackInSlot(i) != null) return false;
			}
		}
		
		return true;
	}
	
	/**
	 * calculate the total "STACK NUMBER" of target item, not item amount!!
	 * 
	 * return TRUE if total stack number = temp stackSize
	 */
	public static int calcItemStackAmount(IInventory inv, ItemStack temp, boolean checkMetadata, boolean checkNbt, boolean checkOredict)
	{
		int targetAmount = 0;
		
		//inventory is ship inv
		if(inv instanceof CapaShipInventory)
		{
			CapaShipInventory shipInv = (CapaShipInventory) inv;
			
			//get any empty slot = false
			for (int i = ContainerShipInventory.SLOTS_SHIPINV; i < shipInv.getSizeInventoryPaged(); i++)
			{
				if (matchTargetItem(shipInv.getStackInSlotWithoutPaging(i), temp, checkMetadata, checkNbt, checkOredict)) targetAmount++;
			}
		}
		//inventory is vanilla chest
		else if (inv instanceof TileEntityChest)
		{
			//check main chest
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (matchTargetItem(inv.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) targetAmount++;
			}
			
			//check adj chest
			TileEntityChest chest2 = TileEntityHelper.getAdjChest((TileEntityChest) inv);
			
			if (chest2 != null)
			{
				for (int i = 0; i < chest2.getSizeInventory(); i++)
				{
					if (matchTargetItem(chest2.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) targetAmount++;
				}
			}
		}
		//other inventory
		else
		{
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (matchTargetItem(inv.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) targetAmount++;
			}
		}
		
		return targetAmount;
	}
	
	/**
	 * check inventory has temp stack, return TRUE if item found
	 */
	public static boolean matchTargetItem(IInventory inv, ItemStack temp, boolean checkMetadata, boolean checkNbt, boolean checkOredict)
	{
		//inventory is ship inv
		if(inv instanceof CapaShipInventory)
		{
			CapaShipInventory shipInv = (CapaShipInventory) inv;
			
			//get any empty slot = false
			for (int i = ContainerShipInventory.SLOTS_SHIPINV; i < shipInv.getSizeInventoryPaged(); i++)
			{
				if (matchTargetItem(shipInv.getStackInSlotWithoutPaging(i), temp, checkMetadata, checkNbt, checkOredict)) return true;
			}
		}
		//inventory is vanilla chest
		else if (inv instanceof TileEntityChest)
		{
			//check main chest
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (matchTargetItem(inv.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) return true;
			}
			
			//check adj chest
			TileEntityChest chest2 = TileEntityHelper.getAdjChest((TileEntityChest) inv);
			
			if (chest2 != null)
			{
				for (int i = 0; i < chest2.getSizeInventory(); i++)
				{
					if (matchTargetItem(chest2.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) return true;
				}
			}
		}
		//other inventory
		else
		{
			for (int i = 0; i < inv.getSizeInventory(); i++)
			{
				if (matchTargetItem(inv.getStackInSlot(i), temp, checkMetadata, checkNbt, checkOredict)) return true;
			}
		}
		
		return false;
	}
	
	/**
	 * check target stack is same with temp stack
	 */
	public static boolean matchTargetItem(ItemStack target, ItemStack temp, boolean checkMetadata, boolean checkNbt, boolean checkOredict)
	{
		if (temp != null && target != null)
		{
			//check item type
			if (target.getItem() == temp.getItem())
			{
				//check both nbt and meta
				if (checkNbt && checkMetadata)
				{
					if (ItemStack.areItemStackTagsEqual(target, temp) &&
						target.getItemDamage() == temp.getItemDamage()) return true;
				}
				//check nbt only
				else if (checkNbt)
				{
					if (ItemStack.areItemStackTagsEqual(target, temp)) return true;
				}
				//check meta only
				else if (checkMetadata)
				{
					if (target.getItemDamage() == temp.getItemDamage()) return true;
				}
				//dont check nbt and meta
				else
				{
					return true;
				}
			}
			//is not same item, try forge ore dict
			else
			{
				//check ore dict
				if (checkOredict)
				{
					int[] a = OreDictionary.getOreIDs(target);
					int[] b = OreDictionary.getOreIDs(temp);
					
					if (a.length > 0 && b.length > 0 && a[0] == b[0])
					{
						return true;
					}
				}
			}
		}
		else if (temp == null && target == null)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * check slot is NOT mode, return TRUE = NOT MODE
	 * 
	 * itemMode: right most bits are slots mode: 1 = NOT MODE, 0 = NORMAL MODE
	 *     bits: 17 16 15 ... 3 2 1 0 = (18 slots)
	 */
	public static boolean getItemMode(int slotID, int stackMode)
	{
		return ((stackMode >> slotID) & 1) == 1 ? true : false;
	}
	
	/** set item mode, return new stackMode (INT 32 bits = max 32 slots) */
	public static int setItemMode(int slotID, int stackMode, boolean notMode)
	{
		int slot = 1 << slotID;
		
		//set bit to 1
		if (notMode)
		{
			stackMode = stackMode | slot;
		}
		//set bit to 0
		else
		{
			stackMode = stackMode & (~slot);
		}
		
		return stackMode;
	}
	
	
}