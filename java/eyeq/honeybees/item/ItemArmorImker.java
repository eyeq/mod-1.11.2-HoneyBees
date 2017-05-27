package eyeq.honeybees.item;

import eyeq.honeybees.HoneyBees;
import eyeq.util.UItemArmor;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;

public class ItemArmorImker extends UItemArmor {
    public static final ArmorMaterial material = EnumHelper.addArmorMaterial("imker", "", 15, new int[]{3, 7, 5, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F);

    private static final ResourceLocation armorName = new ResourceLocation(HoneyBees.MOD_ID, "imker");

    public ItemArmorImker(EntityEquipmentSlot equipmentSlot) {
        super(material, 0, equipmentSlot, armorName);
    }

    @Override
    public String getArmorTexture(ItemStack itemStack, Entity entity, EntityEquipmentSlot slot, String type) {
        if(type == null) {
            return super.getArmorTexture(itemStack, entity, slot, type);
        }
        return null;
    }
}
