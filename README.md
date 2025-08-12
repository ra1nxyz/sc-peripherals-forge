# SC-Peripherals Forge Port 

Currently tracking the progress of translating Kotlin (`.kt`) files from Fabric to Forge, including api imports
I have never worked with kotlin or mc mods so this is a personal project for fun requested by a friend :3

## what mean what

- [✅] — Translated and tested to work
- [❌] — Translated and in testing / not working
- [🛠️] — Work in progress 
- [ ] — Not yet translated


## Translation Progress Checklist

### library/networking
- [❌] NetworkUtil.kt
- [❌] ScLibraryPacket.kt

### peripherals/block
- [❌] ChameliumBlock.kt

### peripherals/client/block
- [🛠️] PosterPrinterRenderer.kt
- [ ] PrintBakedModel.kt
- [🛠️] PrinterRenderer.kt
- [ ] PrintUnbakedModel.kt

### peripherals/client/gui
- [ ] PosterPrinterScreen.kt
- [ ] PrinterScreen.kt
- [ ] ProgressBar.kt

### peripherals/client/item
- [ ] PosterHeadFeatureRenderer.kt
- [ ] PosterRenderer.kt
- [ ] ScPeripheralsClient.kt

### peripherals/config
- [ ] ModMenu.kt
- [ ] ScPeripheralsClientConfig.kt
- [ ] ScPeripheralsConfig.kt

### peripherals/datagen/recipes/handlers
- [ ] PrinterRecipes.kt
- [ ] RecipeHandlers.kt

### peripherals/datagen
- [ ] RecipeGenerator.kt
- [ ] BlockLootTableProvider.kt
- [ ] BlockTagProvider.kt
- [ ] ModelProvider.kt
- [ ] ScPeripheralsDatagen.kt

### peripherals/item
- [ ] ChameliumItem.kt
- [ ] EmptyInkCartridgeItem.kt
- [ ] InkCartridgeItem.kt
- [ ] TextureAnalyzerItem.kt

### peripherals/posters/printer
- [ ] PosterPrinterBlock.kt
- [🛠️] PosterPrinterBlockEntity.kt
- [ ] PosterPrinterInkPacket.kt
- [ ] PosterPrinterPeripheral.kt
- [ ] PosterPrinterScreenHandler.kt
- [ ] PosterPrinterStartPrintPacket.kt

### peripherals/posters
- [ ] PosterItem.kt
- [ ] PosterPrintData.kt
- [ ] PosterRequestC2SPacket.kt
- [ ] PosterState.kt
- [ ] PosterUpdateS2CPacket.kt
- [ ] PosterWorld.kt

### peripherals/prints/printer
- [ ] PrinterBlock.kt
- [ ] PrinterBlockEntity.kt
- [ ] PrinterDataPacket.kt
- [ ] PrinterInkPacket.kt
- [ ] PrinterPeripheral.kt
- [ ] PrinterScreenHandler.kt

### peripherals/prints
- [ ] PrintBlock.kt
- [ ] PrintBlockEntity.kt
- [ ] PrintData.kt
- [ ] PrintItem.kt
- [ ] PrintRecipe.kt
- [ ] Shape.kt
- [ ] Shapes.kt

### peripherals/util
- [ ] BaseBlock.kt
- [ ] BaseBlockEntity.kt
- [ ] BaseBlockWithEntity.kt
- [ ] BaseItem.kt
- [ ] ImplementedInventory.kt
- [ ] InventoryPeripheral.kt
- [ ] LuaTableExt.kt
- [ ] NbtExt.kt
- [ ] PropertyDelegateGetter.kt
- [ ] ScreenHandlerPropertyUpdateIntS2CPacket.kt
- [ ] ValidatingSlot.kt

### peripherals
- [🛠️] ModBlocks.kt
- [ ] Registration.kt
- [ ] ScPeripherals.kt
- [🛠️] SCPeripheralsForge.kt
- [ ] ScPeripheralsPrometheus.kt


---

## Credits

- **Original SC-Peripherals Mod:** [SwitchCraftCC/sc-peripherals](https://github.com/SwitchCraftCC/sc-peripherals)
- **SC-Library:** [SwitchCraftCC/sc-library](https://github.com/SwitchCraftCC/sc-library)

All thanks and credits to the original developers of the SwitchCraft CC mod packages.
