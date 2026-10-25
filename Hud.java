package dev.auramate.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Status effect timer (top-left) and armor durability HUD (right of screen). */
public final class Hud implements HudElement {
	private static final EquipmentSlot[] ARMOR = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	@Override
	public void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity p = mc.player;
		if (p == null || mc.options.hudHidden) return;
		if (Features.EFFECT_TIMER.isEnabled()) drawEffects(ctx, mc, p);
		if (Features.ARMOR_HUD.isEnabled()) drawArmor(ctx, mc, p);
	}

	private static void drawEffects(DrawContext ctx, MinecraftClient mc, PlayerEntity p) {
		List<StatusEffectInstance> list = new ArrayList<>(p.getStatusEffects());
		if (list.isEmpty()) return;
		list.sort(Comparator.comparingInt((StatusEffectInstance e) -> e.isInfinite() ? Integer.MAX_VALUE : e.getDuration()));
		int x = 4, y = 4;
		for (StatusEffectInstance e : list) {
			String name = e.getEffectType().value().getName().getString();
			if (e.getAmplifier() > 0) name += " " + roman(e.getAmplifier() + 1);
			String time = e.isInfinite() ? "**:**" : format(e.getDuration());
			int color = 0xFF000000 | e.getEffectType().value().getColor();
			boolean low = !e.isInfinite() && e.getDuration() < 200;
			boolean show = !low || (e.getDuration() / 10) % 2 == 0; // blink under 10s
			ctx.drawTextWithShadow(mc.textRenderer, name, x, y, color);
			if (show) {
				ctx.drawTextWithShadow(mc.textRenderer, time, x + mc.textRenderer.getWidth(name) + 6, y,
						low ? 0xFFFF5555 : 0xFFFFFFFF);
			}
			y += 11;
		}
	}

	private static void drawArmor(DrawContext ctx, MinecraftClient mc, PlayerEntity p) {
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();
		int rows = 0;
		for (EquipmentSlot s : ARMOR) if (!p.getEquippedStack(s).isEmpty()) rows++;
		if (rows == 0) return;
		int x = sw - 52;
		int y = sh / 2 - rows * 10;
		for (EquipmentSlot s : ARMOR) {
			ItemStack stack = p.getEquippedStack(s);
			if (stack.isEmpty()) continue;
			ctx.drawItem(stack, x, y);
			if (stack.isDamageable()) {
				int max = stack.getMaxDamage();
				int left = max - stack.getDamage();
				int pct = Math.max(0, Math.min(100, Math.round(left * 100f / max)));
				ctx.drawTextWithShadow(mc.textRenderer, pct + "%", x + 19, y + 4, durabilityColor(pct));
			}
			y += 20;
		}
	}

	private static int durabilityColor(int pct) {
		if (pct > 60) return 0xFF55FF55;
		if (pct > 30) return 0xFFFFFF55;
		if (pct > 15) return 0xFFFFAA00;
		return 0xFFFF5555;
	}

	private static String format(int ticks) {
		int total = ticks / 20;
		return String.format("%d:%02d", total / 60, total % 60);
	}

	private static String roman(int n) {
		String[] r = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
		return n < r.length ? r[n] : Integer.toString(n);
	}
}
