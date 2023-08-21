package com.hiroku.tournaments.util;

import java.util.UUID;

import com.hiroku.tournaments.rules.player.Healing;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.status.StatusType;
import com.pixelmonmod.pixelmon.storage.NbtKeys;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Representation of a state that a Pokémon was last seen, in terms of HP and status. This is used for the {@link Healing} rule.
 * 
 * @author Hiroku
 */
public class PokemonState
{
	/** The unique Pokémon ID (Pixelmon's) */
	public UUID id;
	/** The HP the Pokémon was last seen on*/
	public float hp;
	/** The status the Pokémon last had */
	public StatusType status = StatusType.None;
	
	/**
	 * Generates a {@link PokemonState} based on a provided {@link Pokemon}
	 * 
	 * @param pokemon - The Pokémon currently
	 */
	public PokemonState(Pokemon pokemon)
	{
		id = pokemon.getUUID();
		this.hp = pokemon.getHealth();
		if (pokemon.getStatus() != null && pokemon.getStatus().type != StatusType.None)
			status = pokemon.getStatus().type;
	}
	
	/**
	 * Determines whether the Pokémon has been healed since this state. Losing health or gaining a status is ignored
	 * 
	 * @param nbt - The {@link NBTTagCompound} for the Pokémon to be checked
	 * 
	 * @return - true if they have healed in any way. Otherwise false.
	 */
	public boolean hasHealed(NBTTagCompound nbt)
	{
		if (hp < nbt.getFloat(NbtKeys.HEALTH))
			return true;
		return status != StatusType.None && (!nbt.hasKey(NbtKeys.STATUS) || nbt.getInteger(NbtKeys.STATUS) != status.ordinal());
	}
	
	/**
	 * Determines whether the Pokémon has been healed since this state. Losing health or gaining a status is ignored
	 * 
	 * @param pokemon - The Pokémon to be checked
	 * 
	 * @return - true if they have healed in any way. Otherwise false.
	 */
	public boolean hasHealed(Pokemon pokemon)
	{
		if (hp < pokemon.getHealth())
			return true;
		return status != StatusType.None && (pokemon.getStatus() == null || pokemon.getStatus().type != status);
	}
}
