package net.blacklab.lmr.api.mode;

import java.util.UUID;

import net.blacklab.lmr.entity.littlemaid.EntityLittleMaid;
import net.blacklab.lmr.util.TriggerSelect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IPlantable;

public class UtilModeFarmer {

	/**
	 * 渡されたItemが「種」であるかどうか
	 */
	public static boolean isSeed(UUID pUuid, Item pItem){
		return pItem instanceof IPlantable || TriggerSelect.checkTrigger(pUuid, "Seed", pItem);
	}

	/**
	 * 渡されたItemStackがクワのものかどうか
	 */
	public static boolean isHoe(EntityLittleMaid owner, ItemStack pItemStack){
		if(pItemStack.isEmpty()) return false;
		return pItemStack.getItem() instanceof ItemHoe ||
				TriggerSelect.checkTrigger(owner.getMaidMasterUUID(), "Hoe", pItemStack.getItem());
	}

}
