package dev.auramate.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Features {
	private static final List<Feature> ALL = new ArrayList<>();

	public static final LungeSwapFeature LUNGE_SWAP = register(new LungeSwapFeature());
	public static final Feature FAST_SHIELD = register(new Feature(
			"fastshield", "Fast Shield", "Shields start blocking after 3 ticks instead of 5.", true, true));
	public static final Feature HEARTS = register(new Feature(
			"iseeyourhearts", "See Hearts", "Shows other players' hearts and saturation in their nametag.", false, true));
	public static final Feature EFFECT_TIMER = register(new Feature(
			"effecttimer", "Effect Timer", "Shows active status effects with a countdown (top-left).", false, true));
	public static final Feature ARMOR_HUD = register(new Feature(
			"armorhud", "Armor HUD", "Shows your armor pieces with durability % (right side).", false, true));

	private Features() {}

	public static <T extends Feature> T register(T feature) {
		ALL.add(feature);
		return feature;
	}

	public static List<Feature> all() {
		return Collections.unmodifiableList(ALL);
	}
}
