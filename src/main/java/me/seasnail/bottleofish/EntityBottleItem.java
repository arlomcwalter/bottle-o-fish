package me.seasnail.bottleofish;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * A bottle item that spawns an entity when used.
 * @see net.minecraft.item.EntityBucketItem
 */
public class EntityBottleItem<T extends Entity> extends Item {
    private final EntityType<T> entityType;
    private final SoundEvent emptyingSound;

    public EntityBottleItem(EntityType<T> type) {
        super(new FabricItemSettings().maxCount(1));

        this.entityType = type;

        if (type == EntityType.AXOLOTL) {
            this.emptyingSound = SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL;
        } else if (type == EntityType.TADPOLE) {
            this.emptyingSound = SoundEvents.ITEM_BUCKET_EMPTY_TADPOLE;
        } else {
            this.emptyingSound = SoundEvents.ITEM_BUCKET_EMPTY_FISH;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        BlockHitResult blockHitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        }

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getSide();
            BlockPos offsetPos = blockPos.offset(direction);

            if (!world.canPlayerModifyAt(user, blockPos) || !user.canPlaceOn(offsetPos, direction, itemStack)) {
                return TypedActionResult.fail(itemStack);
            }

            FluidState state = world.getFluidState(blockPos);
            if (!state.isEqualAndStill(Fluids.WATER)) {
                return TypedActionResult.pass(itemStack);
            }

            if (world instanceof ServerWorld serverWorld) {
                T entity = entityType.spawnFromItemStack(serverWorld, itemStack, null, blockPos, SpawnReason.BUCKET, true, false);
                if (!(entity instanceof Bucketable bucketable)) {
                    return TypedActionResult.pass(itemStack);
                }

                bucketable.copyDataFromNbt(itemStack.getOrCreateNbt());
                bucketable.setFromBucket(true);

                world.emitGameEvent(user, GameEvent.ENTITY_PLACE, blockPos);
            }

            world.playSound(user, blockPos, emptyingSound, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            return TypedActionResult.success(ItemUsage.exchangeStack(itemStack, user, Items.GLASS_BOTTLE.getDefaultStack()));
        }

        return TypedActionResult.pass(itemStack);
    }
}
