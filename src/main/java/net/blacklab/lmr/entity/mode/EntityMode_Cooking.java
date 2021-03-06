package net.blacklab.lmr.entity.mode;

import net.blacklab.lmr.achievements.AchievementsLMRE;
import net.blacklab.lmr.entity.EntityLittleMaid;
import net.blacklab.lmr.inventory.InventoryLittleMaid;
import net.blacklab.lmr.util.EnumSound;
import net.blacklab.lmr.util.helper.ItemHelper;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class EntityMode_Cooking extends EntityModeBlockBase {

	public static final int mmode_Cooking = 0x0021;


	public EntityMode_Cooking(EntityLittleMaid pEntity) {
		super(pEntity);
	}

	@Override
	public int priority() {
		return 6000;
	}

	@Override
	public void init() {
		/* langファイルに移動
		ModLoader.addLocalization("littleMaidMob.mode.Cooking", "Cooking");
		ModLoader.addLocalization("littleMaidMob.mode.T-Cooking", "T-Cooking");
		ModLoader.addLocalization("littleMaidMob.mode.F-Cooking", "F-Cooking");
		ModLoader.addLocalization("littleMaidMob.mode.D-Cooking", "D-Cooking");
		*/
	}

	@Override
	public void addEntityMode(EntityAITasks pDefaultMove, EntityAITasks pDefaultTargeting) {
		// Cooking:0x0021
		EntityAITasks[] ltasks = new EntityAITasks[2];
		ltasks[0] = pDefaultMove;
		ltasks[1] = new EntityAITasks(owner.aiProfiler);

		owner.addMaidMode(ltasks, "Cooking", mmode_Cooking);
	}

	@Override
	public boolean changeMode(EntityPlayer pentityplayer) {
		ItemStack litemstack = owner.getHandSlotForModeChange();
		if (litemstack != null) {
			if (ItemHelper.isItemBurned(litemstack)) {
				owner.setMaidMode("Cooking");
				if (pentityplayer != null) {
					pentityplayer.addStat(AchievementsLMRE.ac_Cook);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean setMode(int pMode) {
		switch (pMode) {
		case mmode_Cooking :
			owner.setBloodsuck(false);
//			owner.aiJumpTo.setEnable(false);
			owner.aiFollow.setEnable(false);
			owner.aiAvoidPlayer.setEnable(false);
			owner.aiAttack.setEnable(false);
			owner.aiShooting.setEnable(false);
			return true;
		}

		return false;
	}

	@Override
	public int getNextEquipItem(int pMode) {
		int li;
		// モードに応じた識別判定、速度優先
		switch (pMode) {
		case mmode_Cooking :
			for (li = 0; li < owner.maidInventory.getSizeInventory(); li++) {
				// 調理
				if (owner.maidInventory.isItemBurned(li)) {
					swapItemIntoMainHandSlot(li);
					return InventoryLittleMaid.handInventoryOffset;
				}
			}
			break;
		}

		return -1;
	}

	@Override
	public boolean checkItemStack(ItemStack pItemStack) {
		return ItemHelper.isItemBurned(pItemStack) || ItemHelper.isItemSmelting(pItemStack);
	}

	@Override
	public boolean isSearchBlock() {
		if (!super.isSearchBlock()) return false;

		// 燃焼アイテムを持っている？
		if (owner.getCurrentEquippedItem() != null && owner.maidInventory.getSmeltingItem() > -1) {
			fDistance = Double.MAX_VALUE;
			owner.clearTilePos();
			owner.setSneaking(false);
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldBlock(int pMode) {
		return owner.maidTileEntity instanceof TileEntityFurnace &&
				(((TileEntityFurnace)owner.maidTileEntity).isBurning() ||
				ItemHelper.isItemBurned(owner.getCurrentEquippedItem()));
	}

	@Override
	public boolean checkBlock(int pMode, int px, int py, int pz) {
		TileEntity ltile = owner.worldObj.getTileEntity(new BlockPos(px, py, pz));
		if (!(ltile instanceof TileEntityFurnace)) {
			return false;
		}

		// 世界のメイドから
		if (checkWorldMaid(ltile)) return false;
		// 使用していた竈ならそこで終了
		if (owner.isUsingTile(ltile)) return true;

		double ldis = owner.getDistanceTilePosSq(ltile);
		if (fDistance > ldis) {
			owner.setTilePos(ltile);
			fDistance = ldis;
		}

		return false;
	}

	@Override
	public boolean executeBlock(int pMode, int px, int py, int pz) {
		if (!owner.isEqualTile()) {
			return false;
		}

		TileEntityFurnace ltile = (TileEntityFurnace)owner.maidTileEntity;
		ItemStack litemstack;
		boolean lflag = false;
		int li;

		if (owner.getSwingStatusDominant().canAttack()) {
			// 完成品回収
			litemstack = ltile.getStackInSlot(2);
			if (litemstack != null) {
				if (litemstack.stackSize > 0) {
					li = litemstack.stackSize;
					if (owner.maidInventory.addItemStackToInventory(litemstack)) {
						dropExpOrb(litemstack, li - litemstack.stackSize);
						owner.playSound("entity.item.pickup");
						owner.setSwing(5, EnumSound.cookingOver, false);
						owner.addMaidExperience(4.2f);
//                    	if (!pEntityLittleMaid.maidInventory.isItemBurned(pEntityLittleMaid.maidInventory.currentItem)) {
						owner.getNextEquipItem();
//                    	}
						lflag = true;
					}
				}
				ltile.setInventorySlotContents(2, null);
			}

			// 調理可能品を竈にぽーい
			if (!lflag && ltile.getStackInSlot(0) == null) {
				litemstack = ltile.getStackInSlot(2);
				li = owner.maidInventory.getSmeltingItem();
				owner.setEquipItem(li);
				if (li > -1) {
					litemstack = owner.maidInventory.getStackInSlot(li);
					// レシピ対応品
					if (litemstack.stackSize >= ltile.getInventoryStackLimit()) {
						ltile.setInventorySlotContents(0, litemstack.splitStack(ltile.getInventoryStackLimit()));
					} else {
						ltile.setInventorySlotContents(0, litemstack.splitStack(litemstack.stackSize));
					}
					if (litemstack.stackSize <= 0) {
						owner.maidInventory.setInventorySlotContents(li, null);
					}
					owner.playSound("entity.item.pickup");
					owner.setSwing(5, EnumSound.cookingStart, false);
					lflag = true;
				}
			}

			// 手持ちの燃料をぽーい
			if (!lflag && ltile.getStackInSlot(1) == null && ltile.getStackInSlot(0) != null) {
				owner.getNextEquipItem();
				litemstack = owner.getCurrentEquippedItem();
				if (ItemHelper.isItemBurned(litemstack)) {
					if (litemstack.stackSize >= ltile.getInventoryStackLimit()) {
						ltile.setInventorySlotContents(1, litemstack.splitStack(ltile.getInventoryStackLimit()));
					} else {
						ltile.setInventorySlotContents(1, litemstack.splitStack(litemstack.stackSize));
					}
					if (litemstack.stackSize <= 0) {
						owner.maidInventory.setInventoryCurrentSlotContents(null);
					}
					owner.getNextEquipItem();
					owner.playSound("entity.item.pickup");
					owner.setSwing(5, EnumSound.addFuel, false);
					lflag = true;
				} else {
					if (ltile.isBurning()) {
						lflag = true;
					} else {
						// 燃やせるアイテムを持ってないので調理可能品を回収
						ItemStack litemstack2 = ltile.removeStackFromSlot(0);
						if (owner.maidInventory.addItemStackToInventory(litemstack2)) {
							owner.playSound("entity.item.pickup");
							owner.setSwing(5, EnumSound.Null, false);
							owner.getNextEquipItem();
							lflag = false;
						} else {
							ltile.setInventorySlotContents(0, litemstack2);
						}
					}
				}
			}

			// 燃え終わってるのに燃料口に何かあるなら回収する
			if (!lflag && !ltile.isBurning() && ltile.getStackInSlot(1) != null) {
				ItemStack litemstack2 = ltile.removeStackFromSlot(1);
				if (owner.maidInventory.addItemStackToInventory(litemstack2)) {
					owner.playSound("entity.item.pickup");
					owner.setSwing(5, EnumSound.Null, false);
					owner.getNextEquipItem();
					lflag = ItemHelper.isItemBurned(owner.getCurrentEquippedItem());
				} else {
					ltile.setInventorySlotContents(1, litemstack2);
				}
			}
		} else {
			lflag = true;
		}
		if (ltile.isBurning()) {
			owner.setWorking(true);
			owner.setSneaking(py - (int)owner.posY <= 0);
			lflag = true;
		}
//mod_LMM_littleMaidMob.Debug("work" + lflag);
		return lflag;
	}

	@Override
	public void startBlock(int pMode) {
//		owner.setWorking(true);
	}

	@Override
	public void resetBlock(int pMode) {
		owner.setSneaking(false);
//		owner.setWorking(false);
	}


	public void dropExpOrb(ItemStack pItemStack, int pCount) {
		if (!owner.worldObj.isRemote) {
			float var3 = pItemStack.getItem().getSmeltingExperience(pItemStack);
			int var4;

			if (var3 == 0.0F) {
				pCount = 0;
			} else if (var3 < 1.0F) {
				var4 = MathHelper.floor_float(pCount * var3);

				if (var4 < MathHelper.ceiling_float_int(pCount * var3) && (float)Math.random() < pCount * var3 - var4) {
					++var4;
				}

				pCount = var4 == 0 ? 1 : var4;
			}

			while (pCount > 0) {
				var4 = EntityXPOrb.getXPSplit(pCount);
				pCount -= var4;
				owner.worldObj.spawnEntityInWorld(new EntityXPOrb(owner.worldObj, owner.posX, owner.posY + 0.5D, owner.posZ + 0.5D, var4));
			}
		}
	}

}
