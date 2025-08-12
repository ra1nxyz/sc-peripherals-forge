package io.sc3.peripherals

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.eventbus.api.IEventBus

@Mod("scperipherals")
class SCPeripheralsForge {
    init {
        val bus: IEventBus = FMLJavaModLoadingContext.get().modEventBus
        ModBlocks.register()
    }
}
