package com.hiroku.tournaments.obj;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;

public class LocationWrapper
{
	public UUID worldUUID;
	public Vector3d position;
	public Vector3d rotation;
	
	public LocationWrapper()
	{
		;
	}
	
	public LocationWrapper(Location<World> location, Vector3d rotation)
	{
		this.worldUUID = location.getExtent().getUniqueId();
		this.position = location.getPosition();
		this.rotation = rotation;
	}
	
	public void sendPlayer(Player player)
	{
		player.setLocationAndRotation(new Location<>(Sponge.getServer().getWorld(worldUUID).get(), position), rotation);
	}
}
