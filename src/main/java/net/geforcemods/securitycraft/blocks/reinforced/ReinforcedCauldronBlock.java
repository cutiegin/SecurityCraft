package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.blockentities.ReinforcedCauldronBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedCauldronBlock extends CauldronBlock implements IReinforcedBlock {
	public ReinforcedCauldronBlock(AbstractBlock.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext ctx) {
		Entity entity = ctx.getEntity();

		if (entity instanceof PlayerEntity) {
			PlayerEntity player = ((PlayerEntity) entity);
			TileEntity te = level.getBlockEntity(pos);

			if (te instanceof ReinforcedCauldronBlockEntity && ((ReinforcedCauldronBlockEntity) te).isAllowedToInteract(player))
				return SHAPE;
			else
				return VoxelShapes.block();
		}

		return SHAPE;
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		TileEntity te = level.getBlockEntity(pos);

		if (te instanceof ReinforcedCauldronBlockEntity && ((ReinforcedCauldronBlockEntity) te).isAllowedToInteract(player))
			return super.use(state, level, pos, player, hand, hit);

		return ActionResultType.PASS;
	}

	@Override
	public Block getVanillaBlock() {
		return Blocks.CAULDRON;
	}

	@Override
	public void setPlacedBy(World level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (placer instanceof PlayerEntity)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(level, pos, (PlayerEntity) placer));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader level) {
		return new ReinforcedCauldronBlockEntity();
	}
}
