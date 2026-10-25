package dev.auramate.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.item.Items;

/** Common entrypoint: Fast Shield changes the shield item itself, so it lives here. */
public class AuraMate implements ModInitializer {
	private static final float BLOCK_DELAY_SECONDS = 0.15f;

	@Override
	public void onInitialize() {
		DefaultItemComponentEvents.MODIFY.register(context ->
				context.modify(item -> item == Items.SHIELD, (builder, item) -> {
					if (!Features.FAST_SHIELD.isEnabled()) return;
					BlocksAttacksComponent old = item.getComponents().get(DataComponentTypes.BLOCKS_ATTACKS);
					if (old == null) return;
					builder.add(DataComponentTypes.BLOCKS_ATTACKS, new BlocksAttacksComponent(
							BLOCK_DELAY_SECONDS,
							old.disableCooldownScale(),
							old.damageReductions(),
							old.itemDamage(),
							old.bypassedBy(),
							old.blockSound(),
							old.disableSound()));
				}));
	}
}
