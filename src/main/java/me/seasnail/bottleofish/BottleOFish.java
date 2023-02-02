package me.seasnail.bottleofish;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BottleOFish implements ModInitializer {
    public static Item PUFFERFISH_BOTTLE;
    public static Item SALMON_BOTTLE;
    public static Item COD_BOTTLE;
    public static Item TROPICAL_FISH_BOTTLE;
    public static Item AXOLOTL_BOTTLE;
    public static Item TADPOLE_BOTTLE;

    private static Item last = Items.TADPOLE_BUCKET;

    @Override
    public void onInitialize() {
        PUFFERFISH_BOTTLE = create("pufferfish", EntityType.PUFFERFISH);
        SALMON_BOTTLE = create("salmon", EntityType.SALMON);
        COD_BOTTLE = create("cod", EntityType.COD);
        TROPICAL_FISH_BOTTLE = create("tropical_fish", EntityType.TROPICAL_FISH);
        AXOLOTL_BOTTLE = create("axolotl", EntityType.AXOLOTL);
        TADPOLE_BOTTLE = create("tadpole", EntityType.TADPOLE);
    }

    public static Item create(String name, EntityType<?> type) {
        Item item = new EntityBottleItem<>(type);
        Registry.register(Registries.ITEM, new Identifier("bottle-o-fish", "%s_bottle".formatted(name)), item);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.addAfter(last, item);
            last = item;
        });
        return item;
    }
}
