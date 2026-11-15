package com.sprintmenu.mixin;

import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RabbitEntity.class)
public interface RabbitEntityAccessor {
    @Invoker("setVariant")
    void sprintmenu$setVariant(RabbitEntity.Variant variant);
}
