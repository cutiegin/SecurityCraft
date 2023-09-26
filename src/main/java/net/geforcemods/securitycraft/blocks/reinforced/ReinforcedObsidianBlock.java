package net.geforcemods.securitycraft.blocks.reinforced;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class ReinforcedObsidianBlock extends BaseReinforcedBlock {
	public ReinforcedObsidianBlock(AbstractBlock.Properties properties, Block vB) {
		super(properties, vB);
	}

	@Override
	public boolean isPortalFrame(BlockState state, IBlockReader level, BlockPos pos) {
		return true;
	}
}
