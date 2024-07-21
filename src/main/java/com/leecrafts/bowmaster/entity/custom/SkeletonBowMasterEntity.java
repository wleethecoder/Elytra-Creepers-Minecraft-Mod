package com.leecrafts.bowmaster.entity.custom;

import com.leecrafts.bowmaster.entity.goal.AIRangedBowAttackGoal;
import com.leecrafts.bowmaster.neuralnetwork.NeuralNetwork;
import com.leecrafts.bowmaster.util.NeuralNetworkUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkeletonBowMasterEntity extends AbstractSkeleton {

    // TRAINING and PRODUCTION should not both be true
    public static final boolean TRAINING = true;
    public static final boolean PRODUCTION = false;

    protected boolean shouldForwardImpulse = false;
    private NeuralNetwork network;
    private final ArrayList<double[]> states = new ArrayList<>();
    private final ArrayList<List<double[]>> actionProbs = new ArrayList<>();
    private final ArrayList<int[]> actions = new ArrayList<>();
    private final ArrayList<Double> rewards = new ArrayList<>();

    public SkeletonBowMasterEntity(EntityType<? extends AbstractSkeleton> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        if (!pLevel.isClientSide) {
            this.network = NeuralNetworkUtil.loadOrCreateModel();
//            this.network.printWeights();
        }
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
//        super.registerGoals();
        this.goalSelector.addGoal(2, new AIRangedBowAttackGoal<>(this));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, SkeletonBowMasterEntity.class, false));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 128);
    }

    @Override
    public void setZza(float pAmount) {
        if ((this.shouldForwardImpulse && pAmount != 0) ||
                (!this.shouldForwardImpulse && pAmount == 0)) {
            super.setZza(pAmount);
        }
    }

    public void forwardImpulse(float amount) {
        this.setZza(amount);
        this.shouldForwardImpulse = amount != 0;
    }

    public NeuralNetwork getNetwork() {
        return this.network;
    }

    @Override
    protected @NotNull SoundEvent getStepSound() {
        return SoundEvents.SKELETON_STEP;
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource pRandom, @NotNull DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, @NotNull MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
//        pSpawnData = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        this.populateDefaultEquipmentSlots(pLevel.getRandom(), pDifficulty);
        return pSpawnData;
    }

    @Override
    public void reassessWeaponGoal() {
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity pTarget, float pDistanceFactor) {
        ItemStack mainHandItem = this.getMainHandItem();
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem)));
        ArrowItem arrowItem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
        AbstractArrow abstractarrow = arrowItem.createArrow(this.level(), itemstack, this);
        if (mainHandItem.getItem() instanceof BowItem bowItem) {
            abstractarrow = bowItem.customArrow(abstractarrow);
        }
        Vec3 vec3 = this.getLookAngle();
        abstractarrow.shoot(vec3.x, vec3.y, vec3.z, pDistanceFactor * 3.0f, 1.0f);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        if (pDistanceFactor == 1.0F) {
            abstractarrow.setCritArrow(true);
        }

        int j = mainHandItem.getEnchantmentLevel(Enchantments.POWER_ARROWS);
        if (j > 0) {
            abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)j * 0.5D + 0.5D);
        }
        int k = mainHandItem.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
        if (k > 0) {
            abstractarrow.setKnockback(k);
        }
        if (mainHandItem.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
            abstractarrow.setSecondsOnFire(100);
        }

        this.level().addFreshEntity(abstractarrow);
    }

    public void storeStates(double[] observations) {
        this.states.add(observations);
    }

    public void storeActionProbs(List<double[]> actionProbs) {
        this.actionProbs.add(actionProbs);
    }

    public void storeActions(int[] actions) {
        this.actions.add(actions);
    }

    public void storeRewards(double reward) {
        if (!this.rewards.isEmpty() && reward > 0) {
            this.rewards.set(this.rewards.size() - 1, reward);
        }
        else {
            this.rewards.add(reward);
        }
    }

    public ArrayList<double[]> getStates() {
        return this.states;
    }

    public ArrayList<List<double[]>> getActionProbs() {
        return this.actionProbs;
    }

    public ArrayList<int[]> getActions() {
        return this.actions;
    }

    public ArrayList<Double> getRewards() {
        return this.rewards;
    }

}
