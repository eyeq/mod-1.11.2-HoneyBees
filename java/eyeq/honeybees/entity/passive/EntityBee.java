package eyeq.honeybees.entity.passive;

import java.util.List;
import java.util.UUID;

import eyeq.util.entity.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import eyeq.honeybees.HoneyBees;

public class EntityBee extends EntityAnimal {
    private UUID targetID;
    private int angerLevel;

    public EntityBee(World world) {
        super(world);
        this.setSize(0.1F, 1.5F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIMoveToHoney(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0, false));
        this.tasks.addTask(3, new EntityAITempt(this, 0.85, HoneyBees.honey, false));
        this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByAggressor(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.5);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.8);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        angerLevel = tagCompund.getShort("Anger");
        String s = tagCompund.getString("HurtBy");
        if(!s.isEmpty()) {
            targetID = UUID.fromString(s);
            this.setTargetFromGroup();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setShort("Anger", (short) angerLevel);
        tagCompound.setString("HurtBy", targetID == null ? "" : targetID.toString());
    }

    @Override
    protected Item getDropItem() {
        return HoneyBees.honey;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
        dropItem(HoneyBees.honey, 1);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() * 0.95F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HoneyBees.entityBeeAmbient;
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if(targetID == null) {
            return;
        }
        EntityPlayer player = this.world.getPlayerEntityByUUID(this.targetID);
        for(EntityBee bee : this.world.getEntitiesWithinAABB(EntityBee.class, this.getEntityBoundingBox().expand(8.0, 4.0, 8.0))) {
            bee.becomeAngryAt(player);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if(this.isAngry()) {
            this.angerLevel--;
        }
        if(this.inWater) {
            this.setDead();
        }
        if(this.motionY < 0) {
            this.motionY *= 0.8F;
        }
    }

    @Override
    protected void updateAITasks() {
        if(this.angerLevel > 0 && this.getAITarget() == null) {
            this.setTargetFromGroup();
        }
        super.updateAITasks();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if(source == DamageSource.IN_WALL) {
            this.motionY += 0.5F;
            return false;
        }
        Entity entity = source.getEntity();
        if(entity instanceof EntityLivingBase) {
            this.becomeAngryAt((EntityLivingBase) entity);
        }
        return super.attackEntityFrom(source, amount);
    }

    private void setTargetFromGroup() {
        if(targetID == null) {
            return;
        }
        EntityPlayer player = this.world.getPlayerEntityByUUID(targetID);
        this.setRevengeTarget(player);
        this.attackingPlayer = player;
        this.recentlyHit = this.getRevengeTimer();
    }

    public void becomeAngryAt(EntityLivingBase source) {
        if(source == null) {
            return;
        }
        this.angerLevel = 400 + rand.nextInt(400);
        this.setRevengeTarget(source);
    }

    public boolean isAngry() {
        return this.angerLevel > 0;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if(!entity.isEntityAlive()) {
            return false;
        }
        ItemStack armor = EntityUtils.getArmor(entity, EntityEquipmentSlot.HEAD);
        if(armor.getItem() == HoneyBees.imkerHelmet) {
            return false;
        }
        if(!entity.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())) {
            return false;
        }
        if(entity instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) entity;
            PotionEffect potion = target.getActivePotionEffect(MobEffects.POISON);
            int amplifier = potion == null ? 0 : potion.getAmplifier() + 1;
            int duration = 0;
            switch(this.world.getDifficulty()) {
            case HARD:
                duration += 30;
            case NORMAL:
                duration += 50;
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, duration * 20, amplifier));
                break;
            }
            this.onDeath(DamageSource.causeMobDamage(target));
        }
        return true;
    }

    @Override
    public void setRevengeTarget(EntityLivingBase entity) {
        super.setRevengeTarget(entity);
        if(entity != null) {
            targetID = entity.getUniqueID();
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public void setInWeb() {
        super.setInWeb();
        this.setDead();
    }

    @Override
    public float getEyeHeight() {
        return height;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entity) {
        return null;
    }

    protected class EntityAIMoveToHoney extends EntityAIMoveToBlock {
        private final EntityBee entity;
        private boolean isBeehive;

        public EntityAIMoveToHoney(EntityBee entity) {
            super(entity, 0.7, 8);
            this.entity = entity;
        }

        @Override
        public void updateTask() {
            super.updateTask();
            if(!this.isBeehive) {
                return;
            }
            List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, entity.getEntityBoundingBox().expand(4.0, 4.0, 4.0));
            if(list.isEmpty()) {
                return;
            }
            entity.becomeAngryAt(list.get(0));
        }

        @Override
        protected boolean shouldMoveTo(World world, BlockPos pos) {
            Block block = world.getBlockState(pos).getBlock();
            if(block == HoneyBees.beehive || block == HoneyBees.beehiveSim) {
                this.isBeehive = true;
                return true;
            }
            this.isBeehive = false;
            if(block == HoneyBees.honeyTorch || block instanceof BlockFlower) {
                return true;
            }
            return false;
        }
    }

    protected class EntityAIHurtByAggressor extends EntityAIHurtByTarget {
        public EntityAIHurtByAggressor(EntityCreature entity) {
            super(entity, true);
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature entity, EntityLivingBase attacker) {
            super.setEntityAttackTarget(entity, attacker);
            if(entity instanceof EntityBee) {
                ((EntityBee) entity).becomeAngryAt(attacker);
            }
        }
    }
}
