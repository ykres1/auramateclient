package dev.auramate.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Small draggable "task manager" panel: one row per feature with an on/off switch.
 * Mouse input is polled in render() so it doesn't depend on screen event signatures.
 */
public class PanelScreen extends Screen {
	private static final int W = 196;
	private static final int HEADER = 16;
	private static final int ROW = 20;
	private static final int FOOTER = 14;

	private static final int BG = 0xE6101418;
	private static final int HEAD = 0xFF1B2128;
	private static final int BORDER = 0xFF2C3640;
	private static final int ROW_HOVER = 0x33FFFFFF;
	private static final int TEXT = 0xFFE6EDF3;
	private static final int MUTED = 0xFF8B98A5;
	private static final int ACCENT = 0xFF3DDC97;
	private static final int OFF = 0xFF3A444F;
	private static final int CLOSE_HOVER = 0xFFE5534B;

	private int px = Config.getInt("panel.x", -1);
	private int py = Config.getInt("panel.y", -1);
	private boolean wasDown;
	private boolean dragging;
	private int dragDx, dragDy;

	public PanelScreen() {
		super(Text.literal("AuraMateClient"));
	}

	@Override
	protected void init() {
		wasDown = MinecraftClient.getInstance().mouse.wasLeftButtonClicked();
		if (px < 0 || py < 0) {
			px = (width - W) / 2;
			py = height / 4;
		}
		clamp();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		// No blur / dim: keep it a small floating window over the game.
	}

	private int panelHeight() {
		return HEADER + Features.all().size() * ROW + FOOTER;
	}

	private void clamp() {
		px = Math.max(0, Math.min(px, width - W));
		py = Math.max(0, Math.min(py, height - panelHeight()));
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		boolean down = MinecraftClient.getInstance().mouse.wasLeftButtonClicked();
		List<Feature> feats = Features.all();

		// ---- input (edge-detected polling) ----
		if (down && !wasDown) {
			onPress(mx, my, feats);
		} else if (down && dragging) {
			px = mx - dragDx;
			py = my - dragDy;
			clamp();
		} else if (!down && dragging) {
			dragging = false;
			Config.set("panel.x", px);
			Config.set("panel.y", py);
			Config.save();
		}
		wasDown = down;

		// ---- draw ----
		int h = panelHeight();
		ctx.fill(px - 1, py - 1, px + W + 1, py + h + 1, BORDER);
		ctx.fill(px, py, px + W, py + h, BG);
		ctx.fill(px, py, px + W, py + HEADER, HEAD);
		ctx.drawTextWithShadow(textRenderer, "AuraMate", px + 6, py + 4, ACCENT);
		int active = (int) feats.stream().filter(Feature::isEnabled).count();
		String count = active + "/" + feats.size();
		ctx.drawTextWithShadow(textRenderer, count, px + W - 18 - textRenderer.getWidth(count), py + 4, MUTED);

		if (in(mx, my, px + W - 14, py + 2, 12, 12)) ctx.fill(px + W - 14, py + 2, px + W - 2, py + 14, CLOSE_HOVER);
		ctx.drawTextWithShadow(textRenderer, "x", px + W - 10, py + 4, TEXT);

		String hint = "Right Shift to open  |  drag header";
		for (int i = 0; i < feats.size(); i++) {
			Feature f = feats.get(i);
			int ry = py + HEADER + i * ROW;
			if (in(mx, my, px, ry, W, ROW)) {
				ctx.fill(px, ry, px + W, ry + ROW, ROW_HOVER);
				hint = f.description + (f.needsRestart ? " (restart to apply)" : "");
			}
			ctx.fill(px + 6, ry + 8, px + 10, ry + 12, f.isEnabled() ? ACCENT : OFF);
			ctx.drawTextWithShadow(textRenderer, f.name, px + 15, ry + 6, f.isEnabled() ? TEXT : MUTED);

			Feature.Setting s = f.setting();
			if (s != null) drawStepper(ctx, mx, my, stepperX(), ry, s);
			drawSwitch(ctx, px + W - 30, ry + 5, f.isEnabled());
		}

		int fy = py + h - FOOTER;
		ctx.fill(px, fy, px + W, fy + 1, BORDER);
		String shown = textRenderer.getWidth(hint) > W - 8 ? textRenderer.trimToWidth(hint, W - 14) + "..." : hint;
		ctx.drawTextWithShadow(textRenderer, shown, px + 4, fy + 4, MUTED);
	}

	private int stepperX() {
		return px + W - 30 - 14 - 26 - 14;
	}

	private void drawSwitch(DrawContext ctx, int sx, int sy, boolean on) {
		ctx.fill(sx, sy, sx + 24, sy + 10, on ? ACCENT : OFF);
		int kx = on ? sx + 14 : sx + 1;
		ctx.fill(kx, sy + 1, kx + 9, sy + 9, 0xFFFFFFFF);
	}

	private void drawStepper(DrawContext ctx, int mx, int my, int sx, int ry, Feature.Setting s) {
		boolean hm = in(mx, my, sx, ry + 4, 12, 12);
		boolean hp = in(mx, my, sx + 38, ry + 4, 12, 12);
		ctx.fill(sx, ry + 4, sx + 12, ry + 16, hm ? 0xFF4A5663 : OFF);
		ctx.fill(sx + 38, ry + 4, sx + 50, ry + 16, hp ? 0xFF4A5663 : OFF);
		ctx.drawTextWithShadow(textRenderer, "-", sx + 4, ry + 6, TEXT);
		ctx.drawTextWithShadow(textRenderer, "+", sx + 42, ry + 6, TEXT);
		String v = s.get().getAsInt() + "t";
		ctx.drawTextWithShadow(textRenderer, v, sx + 12 + (26 - textRenderer.getWidth(v)) / 2, ry + 6, TEXT);
	}

	private void onPress(int mx, int my, List<Feature> feats) {
		if (in(mx, my, px + W - 14, py + 2, 12, 12)) {
			close();
			return;
		}
		if (in(mx, my, px, py, W, HEADER)) {
			dragging = true;
			dragDx = mx - px;
			dragDy = my - py;
			return;
		}
		for (int i = 0; i < feats.size(); i++) {
			Feature f = feats.get(i);
			int ry = py + HEADER + i * ROW;
			if (!in(mx, my, px, ry, W, ROW)) continue;
			Feature.Setting s = f.setting();
			if (s != null) {
				int sx = stepperX();
				int cur = s.get().getAsInt();
				if (in(mx, my, sx, ry + 4, 12, 12)) {
					s.set().accept(Math.max(s.min(), cur - 1));
					return;
				}
				if (in(mx, my, sx + 38, ry + 4, 12, 12)) {
					s.set().accept(Math.min(s.max(), cur + 1));
					return;
				}
			}
			f.toggle();
			return;
		}
	}

	private static boolean in(int mx, int my, int rx, int ry, int rw, int rh) {
		return mx >= rx && mx < rx + rw && my >= ry && my < ry + rh;
	}
}
