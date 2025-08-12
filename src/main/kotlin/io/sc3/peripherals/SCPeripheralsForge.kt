package io.sc3.peripherals

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod("scperipherals")
class SCPeripheralsForge {
    init {
        val bus = FMLJavaModLoadingContext.get().modEventBus
        ModBlocks.register()
    }
}
