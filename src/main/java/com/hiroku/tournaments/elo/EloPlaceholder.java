package com.hiroku.tournaments.elo;

import com.hiroku.tournaments.Tournaments;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public class EloPlaceholder {
	public static void addPlaceholder() {
		Sponge.getServiceManager().provideUnchecked(PlaceholderService.class).loadAll(new EloPlaceholder(), Tournaments.INSTANCE).stream().map(builder -> {
			if ("elo".equals(builder.getId())) {
				return builder.tokens("avg", "single", "double", "double1v1", "double2v2").description("Tournaments Elo Placeholders.");
			}
			return builder;
		}).map(builder -> builder.author("Hiroku").plugin(Tournaments.INSTANCE).version(Tournaments.VERSION)).forEach(builder -> {
			try {
				builder.buildAndRegister();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	@Placeholder(id = "elo")
	public Object elo(@Source Player player, @Token String token) {
		if (token.toLowerCase().equals("avg")) {
			return EloStorage.getAverageElo(player.getUniqueId());
		}
		return EloStorage.getElo(player.getUniqueId(), token);
	}
}
