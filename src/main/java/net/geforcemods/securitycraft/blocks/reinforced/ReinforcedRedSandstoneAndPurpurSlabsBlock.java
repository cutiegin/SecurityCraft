package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedRedSandstoneAndPurpurSlabsBlock extends BlockSlab implements ITileEntityProvider, IOverlayDisplay, IReinforcedBlock {
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);
	private final boolean isDouble;

	public ReinforcedRedSandstoneAndPurpurSlabsBlock(boolean isDouble, Material blockMaterial) {
		super(blockMaterial);

		this.isDouble = isDouble;

		if (!isDouble())
			useNeighborBrightness = true;

		setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.RED_SANDSTONE));
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		return Float.MAX_VALUE;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return Float.MAX_VALUE;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (placer instanceof EntityPlayer)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (EntityPlayer) placer));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(SCContent.reinforcedStoneSlabs2);
	}

	@Override
	public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
		if (!isDouble) {
			for (EnumType et : EnumType.values()) {
				items.add(new ItemStack(this, 1, et.getMetadata()));
			}
		}
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public String getTranslationKey(int meta) {
		return super.getTranslationKey() + "." + EnumType.byMetadata(meta).getTranslationKey();
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return EnumType.byMetadata(stack.getMetadata() & 7);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta & 7)).withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		byte b0 = 0;
		int meta = b0 | state.getValue(VARIANT).getMetadata();

		if (state.getValue(HALF) == EnumBlockHalf.TOP)
			meta |= 8;

		return meta;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, HALF, VARIANT);
	}

	@Override
	public boolean isDouble() {
		return isDouble;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new OwnableBlockEntity();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(SCContent.reinforcedStoneSlabs2, 1, damageDropped(state));
	}

	@Override
	public List<Block> getVanillaBlocks() {
		return Arrays.asList(Blocks.STONE_SLAB2, Blocks.PURPUR_SLAB);
	}

	@Override
	public int getAmount() {
		return 2;
	}

	public static enum EnumType implements IStringSerializable {
		RED_SANDSTONE(0, "red_sandstone"),
		PURPUR(1, "purpur");

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final int meta;
		private final String name;
		private final String unlocalizedName;

		private EnumType(int meta, String name) {
			this(meta, name, name);
		}

		private EnumType(int meta, String name, String unlocalizedName) {
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public int getMetadata() {
			return meta;
		}

		@Override
		public String toString() {
			return name;
		}

		public static EnumType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length)
				meta = 0;

			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return name;
		}

		public String getTranslationKey() {
			return unlocalizedName;
		}

		static {
			EnumType[] values = values();
			int length = values.length;

			for (int i = 0; i < length; ++i) {
				EnumType type = values[i];
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}

	@Override
	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos) {
		return new ItemStack(Item.getItemFromBlock(SCContent.reinforcedStoneSlabs2), 1, getMetaFromState(state) % 8);
	}

	@Override
	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos) {
		return true;
	}
}