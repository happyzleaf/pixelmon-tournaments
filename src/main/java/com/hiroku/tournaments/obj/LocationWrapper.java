package com.hiroku.tournaments.obj;

import com.happyzleaf.tournaments.text.Text;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocationWrapper {
	public final RegistryKey<World> dimensionKey;
	public final Vector3d position;
	public final Vector2f rotation;

	public LocationWrapper(World world, Vector3d position, Vector2f rotation) {
		this.dimensionKey = world == null ? null : world.getDimensionKey();
		this.position = position;
		this.rotation = rotation;
	}

	public LocationWrapper(Entity at) {
		this(checkNotNull(at, "at").getEntityWorld(), at.getPositionVec(), at.getPitchYaw());
	}

	public LocationWrapper() {
		this(null, null, null);
	}

	public void sendPlayer(PlayerEntity player) {
		if (this.dimensionKey == null) {
			// TODO: proper error? When would this happen?
			player.sendMessage(Text.ERROR, Util.DUMMY_UUID);
			return;
		}

		if (!player.getEntityWorld().getDimensionKey().equals(this.dimensionKey)) {
			player.setWorld(player.getServer().getWorld(this.dimensionKey));
		}

		Vector3d position = this.position == null ? player.getPositionVec() : this.position;
		Vector2f rotation = this.rotation == null ? player.getPitchYaw() : this.rotation;
		player.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), rotation.x, rotation.y);
	}
}
