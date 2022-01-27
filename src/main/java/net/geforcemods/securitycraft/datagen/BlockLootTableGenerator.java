package net.geforcemods.securitycraft.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.blocks.mines.IMSBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDoorBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSlabBlock;
import net.geforcemods.securitycraft.misc.conditions.TileEntityNBTCondition;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext.EntityTarget;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.EntityHasProperty;
import net.minecraft.loot.conditions.Inverted;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

public class BlockLootTableGenerator implements IDataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	protected final Map<Supplier<Block>, LootTable.Builder> lootTables = new HashMap<>();
	private final DataGenerator generator;

	public BlockLootTableGenerator(DataGenerator generator) {
		this.generator = generator;
	}

	private void addTables() {
		for (RegistryObject<Block> obj : SCContent.BLOCKS.getEntries()) {
			Block block = obj.get();

			if (block instanceof ReinforcedSlabBlock)
				putSlabLootTable(obj);
			else if (block instanceof IExplosive)
				putMineLootTable(obj);
			else if (block.asItem() != Items.AIR)
				putStandardBlockLootTable(obj);
		}

		lootTables.remove(SCContent.REINFORCED_PISTON_HEAD);
		putMineLootTable(SCContent.ANCIENT_DEBRIS_MINE);
		putSlabLootTable(SCContent.CRYSTAL_QUARTZ_SLAB);

		StandaloneLootEntry.Builder<?> imsLootEntryBuilder = ItemLootEntry.lootTableItem(SCContent.BOUNCING_BETTY.get());

		for (int i = 0; i <= 4; i++) {
			if (i == 1)
				continue;

			//@formatter:off
			imsLootEntryBuilder.apply(SetCount.setCount(ConstantRange.exactly(i))
					.when(BlockStateProperty.hasBlockStateProperties(SCContent.IMS.get())
							.setProperties(StatePropertiesPredicate.Builder.properties()
									.hasProperty(IMSBlock.MINES, i))));
		}

		lootTables.put(SCContent.IMS, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(imsLootEntryBuilder)));
		putStandardBlockLootTable(SCContent.KEY_PANEL_BLOCK, SCContent.KEY_PANEL.get());
		putStandardBlockLootTable(SCContent.KEYPAD_CHEST);
		putDoorLootTable(SCContent.KEYPAD_DOOR, SCContent.KEYPAD_DOOR_ITEM);
		putDoorLootTable(SCContent.REINFORCED_DOOR, SCContent.REINFORCED_DOOR_ITEM);
		lootTables.put(SCContent.REINFORCED_IRON_BARS,
				LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(SCContent.REINFORCED_IRON_BARS.get())
								.when(TileEntityNBTCondition.builder().equals("canDrop", true)))
						.when(SurvivesExplosion.survivesExplosion())));
		putDoorLootTable(SCContent.SCANNER_DOOR, SCContent.SCANNER_DOOR_ITEM);
		putStandardBlockLootTable(SCContent.SECRET_ACACIA_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_ACACIA_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_BIRCH_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_BIRCH_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_CRIMSON_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_CRIMSON_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_DARK_OAK_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_DARK_OAK_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_JUNGLE_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_JUNGLE_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_OAK_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_OAK_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_SPRUCE_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_SPRUCE_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_WARPED_SIGN);
		putStandardBlockLootTable(SCContent.SECRET_WARPED_WALL_SIGN);
		putStandardBlockLootTable(SCContent.SECURITY_CAMERA);
		putStandardBlockLootTable(SCContent.STAIRS_CRYSTAL_QUARTZ);
		lootTables.put(SCContent.SONIC_SECURITY_SYSTEM, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(SCContent.SONIC_SECURITY_SYSTEM_ITEM.get())
								.apply(CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
										.copy("LinkedBlocks", "LinkedBlocks")))));
		//@formatter:on
	}

	protected final LootTable.Builder createStandardBlockLootTable(Supplier<Block> drop) {
		return createStandardBlockLootTable(drop.get());
	}

	protected final LootTable.Builder createStandardBlockLootTable(IItemProvider drop) {
		//@formatter:off
		return LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(drop.asItem()))
						.when(SurvivesExplosion.survivesExplosion()));
		//@formatter:on
	}

	protected final void putDoorLootTable(Supplier<Block> door, Supplier<Item> doorItem) {
		//@formatter:off
		lootTables.put(door, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(doorItem.get())
								.when(BlockStateProperty.hasBlockStateProperties(door.get())
										.setProperties(StatePropertiesPredicate.Builder.properties()
												.hasProperty(ReinforcedDoorBlock.HALF, DoubleBlockHalf.LOWER)))
								.when(SurvivesExplosion.survivesExplosion()))));
		//@formatter:on
	}

	protected final void putStandardBlockLootTable(Supplier<Block> block) {
		putStandardBlockLootTable(block, block.get());
	}

	protected final void putStandardBlockLootTable(Supplier<Block> block, IItemProvider drop) {
		lootTables.put(block, createStandardBlockLootTable(drop));
	}

	protected final void putMineLootTable(Supplier<Block> mine) {
		//@formatter:off
		lootTables.put(mine, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(mine.get()))
						.when(SurvivesExplosion.survivesExplosion())
						.when(Inverted.invert(EntityHasProperty.entityPresent(EntityTarget.THIS)))));
		//@formatter:on
	}

	protected final void putSlabLootTable(Supplier<Block> slab) {
		//@formatter:off
		lootTables.put(slab, LootTable.lootTable()
				.withPool(LootPool.lootPool()
						.setRolls(ConstantRange.exactly(1))
						.add(ItemLootEntry.lootTableItem(slab.get())
								.apply(SetCount.setCount(ConstantRange.exactly(2))
										.when(BlockStateProperty.hasBlockStateProperties(slab.get())
												.setProperties(StatePropertiesPredicate.Builder.properties()
														.hasProperty(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE))))
								.apply(ExplosionDecay.explosionDecay()))));
		//@formatter:on
	}

	@Override
	public void run(DirectoryCache cache) throws IOException {
		Map<ResourceLocation, LootTable> tables = new HashMap<>();

		addTables();

		for (Map.Entry<Supplier<Block>, LootTable.Builder> entry : lootTables.entrySet()) {
			tables.put(entry.getKey().get().getLootTable(), entry.getValue().setParamSet(LootParameterSets.BLOCK).build());
		}

		tables.forEach((key, lootTable) -> {
			try {
				IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable), generator.getOutputFolder().resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public String getName() {
		return "SecurityCraft Block Loot Tables";
	}
}
