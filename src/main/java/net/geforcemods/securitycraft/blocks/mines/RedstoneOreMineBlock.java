package net.geforcemods.securitycraft.blocks.mines;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneOreMineBlock extends BaseFullMineBlock {
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public RedstoneOreMineBlock(AbstractBlock.Properties properties, Block disguisedBlock) {
		super(properties, disguisedBlock);

		registerDefaultState(defaultBlockState().setValue(LIT, false));
	}

	@Override
	public void attack(BlockState state, World level, BlockPos pos, PlayerEntity player) {
		activate(state, level, pos);
		super.attack(state, level, pos, player);
	}

	@Override
	public void stepOn(World level, BlockPos pos, Entity entity) {
		activate(level.getBlockState(pos), level, pos);
		super.stepOn(level, pos, entity);
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide)
			spawnParticles(level, pos);
		else
			activate(state, level, pos);

		return stack.getItem() instanceof BlockItem && (new BlockItemUseContext(player, hand, stack, hit)).canPlace() ? ActionResultType.PASS : ActionResultType.SUCCESS;
	}

	private static void activate(BlockState state, World level, BlockPos pos) {
		spawnParticles(level, pos);

		if (!state.getValue(LIT))
			level.setBlock(pos, state.setValue(LIT, true), 3);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return state.getValue(LIT);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld level, BlockPos pos, Random rand) {
		if (state.getValue(LIT))
			level.setBlock(pos, state.setValue(LIT, false), 3);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World level, BlockPos pos, Random rand) {
		if (state.getValue(LIT))
			spawnParticles(level, pos);
	}

	private static void spawnParticles(World level, BlockPos pos) {
		Random random = level.random;

		for (Direction direction : Direction.values()) {
			BlockPos offsetPos = pos.relative(direction);

			if (!level.getBlockState(offsetPos).isSolidRender(level, offsetPos)) {
				Direction.Axis axis = direction.getAxis();
				double d1 = axis == Direction.Axis.X ? 0.5D + 0.5625D * direction.getStepX() : (double) random.nextFloat();
				double d2 = axis == Direction.Axis.Y ? 0.5D + 0.5625D * direction.getStepY() : (double) random.nextFloat();
				double d3 = axis == Direction.Axis.Z ? 0.5D + 0.5625D * direction.getStepZ() : (double) random.nextFloat();

				level.addParticle(RedstoneParticleData.REDSTONE, pos.getX() + d1, pos.getY() + d2, pos.getZ() + d3, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}
}
