package io.sc3.peripherals.client.block

import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.renderer.texture.TextureAtlas
import java.util.function.Function
import net.minecraft.client.resources.model.Material

class PrintUnbakedModel : UnbakedModel {
  private val particleMaterial = Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation("block/stone"))

  override fun getDependencies(): MutableCollection<ResourceLocation> = mutableSetOf()

  override fun resolveParents(resolver: Function<ResourceLocation, UnbakedModel>) {
    // not done apparently :)
  }

  override fun bake(
    baker: ModelBaker,
    textureGetter: Function<Material, TextureAtlasSprite>,
    modelState: ModelState,
    modelLocation: ResourceLocation
  ): BakedModel {
    // The actual quads are generated in PrintBakedModel as this needs to be done dynamically for each block entity
    // or item stack. Just load the particle sprite here.
    val sprite = textureGetter.apply(particleMaterial)
    return PrintBakedModel(sprite)
  }
}
