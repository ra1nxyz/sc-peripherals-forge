package io.sc3.peripherals.block

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.material.Material

// If BaseBlock is just extending Block, you can keep it.
open class BaseBlock(props: BlockBehaviour.Properties) : Block(props)

class ChameliumBlock(props: BlockBehaviour.Properties) : BaseBlock(props)
