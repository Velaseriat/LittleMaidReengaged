package net.blacklab.lmr.item;

import java.util.List;

import net.blacklab.lmr.LittleMaidReengaged;
import net.blacklab.lmr.entity.EntityLittleMaid;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMaidSpawnEgg extends Item
{

	public ItemMaidSpawnEgg()
	{
		setHasSubtypes(true);
		setCreativeTab(CreativeTabs.MISC);
		setUnlocalizedName(LittleMaidReengaged.DOMAIN + ":spawn_littlemaid_egg");
	}

	@Override
	public EnumActionResult onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, BlockPos par46X, EnumHand pHand, EnumFacing par7, float par8, float par9, float par10)
	{
		if (par3World.isRemote)
		{
			return EnumActionResult.SUCCESS;
		}
		//Block block = par3World.getBlockState(par46X).getBlock(); //Never used, not needed.
		int par4 = par46X.getX(); int par5 = par46X.getY() + 1; int par6 = par46X.getZ();
		/*
		par4 += Facing.offsetsXForSide[par7];
		par5 += Facing.offsetsYForSide[par7];
		par6 += Facing.offsetsZForSide[par7];
		*/

//		if (par7 == EnumFacing.UP && block.getRenderType(par3World.getBlockState(par46X)) == EnumBlockRenderType)
//		{
//			d0 = 0.5D;
//		}

		Entity entity = spawnMaid(par3World, par4 + 0.5D, par5 + 0.0D, par6 + 0.5D);

		if (entity != null)
		{
			if (entity instanceof EntityLivingBase && par1ItemStack.hasDisplayName())
			{
				(entity).setCustomNameTag(par1ItemStack.getDisplayName());
			}

			if (!par2EntityPlayer.capabilities.isCreativeMode)
			{
				--par1ItemStack.stackSize;
			}
		}else
		{
			return EnumActionResult.FAIL;
		}

		return EnumActionResult.SUCCESS;
	}

	public static Entity spawnMaid(World par0World, double par2, double par4, double par6)
	{
		EntityLiving entityliving;
		try {
			entityliving = new EntityLittleMaid(par0World);

			//TODO: come back to this.
			//entityliving.setLocationAndAngles(par2, par4, par6, MathHelper.wrapAngleTo180_float(par0World.rand.nextFloat() * 360.0F), 0.0F);
			entityliving.setLocationAndAngles(par2, par4, par6, MathHelper.wrapDegrees(par0World.rand.nextFloat() * 360.0F), 0.0F);
//			((LMM_EntityLittleMaid)entityliving).setTextureNames();
			((EntityLittleMaid) entityliving).onSpawnWithEgg();
			par0World.spawnEntityInWorld(entityliving);
			return entityliving;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to spawn maid!");
		}

		return new EntityLittleMaid(par0World);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item par1, CreativeTabs par2, List par3)
	{
		par3.add(new ItemStack(par1, 1));
	}

}
