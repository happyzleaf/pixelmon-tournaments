package fr.pokepixel.pokewiki.translation;

import java.util.HashMap;

public class MapIgnoreDuplicates<A, B> extends HashMap<A, B> {
	@Override
	public B put(A key, B value) {
		super.put(key, value);
		return null;
	}
}
