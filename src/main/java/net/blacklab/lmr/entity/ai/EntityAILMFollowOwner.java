package net.blacklab.lmr.entity.ai;

import net.blacklab.lmr.entity.EntityLittleMaid;
import net.blacklab.lmr.util.helper.MaidHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;

public class EntityAILMFollowOwner extends EntityAIBase implements IEntityAILM {

	private EntityLittleMaid theMaid;
	private Entity theOwner;
	private float moveSpeed;
	private PathNavigate petPathfinder;
	private int field_48310_h;
	private double maxDist;
	private double minDist;
	protected double sprintDist;
	protected boolean isEnable;

	public EntityAILMFollowOwner(EntityLittleMaid par1EntityLittleMaid,
			float pSpeed, double pSprintDistSQ) {
		theMaid = par1EntityLittleMaid;
		moveSpeed = pSpeed;
		petPathfinder = par1EntityLittleMaid.getNavigator();
		sprintDist = pSprintDistSQ;
		isEnable = true;
		setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute() {
		if (!isEnable)
			return false;

		Entity entityliving = theMaid.getOwner();
		if (entityliving == null) {
			return false;
		}

		if (theMaid.isSitting()||theMaid.isMaidWait()) {
			return false;
		}

		theOwner = entityliving;
		return MaidHelper.canStartFollow(theMaid, theOwner, 0);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean continueExecuting() {
//		if(theMaid.handleWaterMovement()) return !theMaid.isMaidWait()&&!theMaid.isSitting();
		return !theMaid.getNavigator().noPath()
				&& MaidHelper.canStartFollow(theMaid, theOwner, 0)
				&& !theMaid.isSitting();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting() {
		field_48310_h = 0;
		//lastAvoidWater = petPathfinder.getAvoidsWater();
		//petPathfinder.setAvoidsWater(false);
//		if(!theMaid.isInWater()) ((PathNavigateGround)this.theMaid.getNavigator()).setAvoidsWater(false);
	}

	/**
	 * Resets the task
	 */
	public void resetTask() {
		theMaid.setSprinting(false);
		theOwner = null;
//		if(!theMaid.isInWater()) ((PathNavigateGround)this.theMaid.getNavigator()).setAvoidsWater(true);
		petPathfinder.clearPathEntity();
		//petPathfinder.setAvoidsWater(lastAvoidWater);
	}

	/**
	 * Updates the task
	 */
	public void updateTask() {
		double toDistance = theMaid.getDistanceSqToEntity(theOwner);
		
		if (toDistance - theMaid.getActiveModeClass().getDistanceSqToStartFollow() > 1.0) {
			theMaid.getLookHelper().setLookPositionWithEntity(theOwner, 10F, theMaid.getVerticalFaceSpeed());
		}

		if (theMaid.isSitting()) {
			return;
		}
		// 指定距離以上ならダッシュ
		if(!theMaid.isInWater()){
			theMaid.setSprinting(toDistance > sprintDist);
			if (--field_48310_h > 0) {
				return;
			}
		}

		field_48310_h = 10;

		Path entity = theMaid.getNavigator().getPathToEntityLiving(theOwner);
		/*
		if(entity==null){
			if(theMaid.isInWater()&&theMaid.swimmingEnabled){
				int x = MathHelper.floor_double(theOwner.posX);
				int z = MathHelper.floor_double(theOwner.posZ);
				int y = MathHelper.floor_double(theOwner.posY);
				LMM_LittleMaidMobNX.Debug("TARGET POS %d,%d,%d", x,y,z);
				if(theMaid.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock().getMaterial()!=Material.water){
					if(theMaid.worldObj.getBlockState(new BlockPos(x, y-1, z)).getBlock().getMaterial()==Material.water)
						entity = theMaid.getNavigator().getPathToXYZ(theOwner.posX, theOwner.posY-1, theOwner.posZ);
					else {
						theMaid.setLocationAndAngles(x, y+1, z, theMaid.rotationYaw, theMaid.rotationPitch);
					}
				}
			}
			return;
		}
		*/
		theMaid.getNavigator().setPath(entity, moveSpeed);
	}

	@Override
	public void setEnable(boolean pFlag) {
		isEnable = pFlag;
	}

	@Override
	public boolean getEnable() {
		return isEnable;
	}

}
