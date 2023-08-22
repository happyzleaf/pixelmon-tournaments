package com.hiroku.tournaments.obj;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LocationWrapper {
	public RegistryKey<World> dimensionKey;
	public Vector3d position;
	public Vector2f rotation;

	public LocationWrapper() {
	}

	public LocationWrapper(World world, Vector3d position, Vector2f rotation) {
		this.dimensionKey = world.getDimensionKey();
		this.position = position;
		this.rotation = rotation;
	}

	public void sendPlayer(PlayerEntity player) {
		if (!player.getEntityWorld().getDimensionKey().equals(dimensionKey)) {
			player.setWorld(player.getServer().getWorld(dimensionKey));
		}

		player.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), rotation.x, rotation.y);
	}
}
