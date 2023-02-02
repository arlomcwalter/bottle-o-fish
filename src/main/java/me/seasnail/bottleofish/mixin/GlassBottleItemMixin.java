package me.seasnail.bottleofish.mixin;

import me.seasnail.bottleofish.BottleOFish;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GlassBottleItem.class)
public class GlassBottleItemMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;emitGameEvent(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/util/math/BlockPos;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, List<AreaEffectCloudEntity> list, ItemStack itemStack, HitResult hitResult, BlockPos blockPos) {
        List<Entity> candidates = world.getEntitiesByClass(Entity.class, new Box(blockPos), (entity) -> entity.isAlive() && entity instanceof Bucketable);
        if (candidates.isEmpty()) return;

        Entity entity = candidates.get(0);
        Bucketable bucketable = (Bucketable) entity;

        ItemStack newStack = new ItemStack(typeToItem(entity.getType()));
        bucketable.copyDataToStack(newStack);

        entity.discard();

        cir.setReturnValue(TypedActionResult.success(ItemUsage.exchangeStack(itemStack, user, newStack)));
    }

    @Unique
    private static Item typeToItem(EntityType<?> type) {
        if (type == EntityType.PUFFERFISH) {
            return BottleOFish.PUFFERFISH_BOTTLE;
        } else if (type == EntityType.SALMON) {
            return BottleOFish.SALMON_BOTTLE;
        } else if (type == EntityType.COD) {
            return BottleOFish.COD_BOTTLE;
        } else if (type == EntityType.TROPICAL_FISH) {
            return BottleOFish.TROPICAL_FISH_BOTTLE;
        } else if (type == EntityType.AXOLOTL) {
            return BottleOFish.AXOLOTL_BOTTLE;
        } else if (type == EntityType.TADPOLE) {
            return BottleOFish.TADPOLE_BOTTLE;
        }

        throw new IllegalArgumentException("Unknown entity type: " + type);
    }
}
