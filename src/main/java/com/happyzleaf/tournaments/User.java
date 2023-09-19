package com.happyzleaf.tournaments;

import com.happyzleaf.tournaments.text.Text;
import com.mojang.authlib.GameProfile;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.OpEntry;
import net.minecraft.util.Util;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;

import javax.annotation.Nullable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author happyz
 */
public class User {
    public final UUID id;
    public final GameProfile profile;

    public User(UUID id) {
        this.id = checkNotNull(id, "id");

        // TODO: This might be null T.T
        this.profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(id);
    }

    public User(GameProfile profile) {
        this.id = checkNotNull(profile, "profile").getId();
        this.profile = profile;
    }

    public User(PlayerEntity player) {
        this.id = checkNotNull(player, "player").getUniqueID();
        this.profile = player.getGameProfile();
    }

    public PlayerEntity getPlayer() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(id);
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public String getName() {
        return profile.getName();
    }

    public void sendMessage(Text text) {
        PlayerEntity player = getPlayer();
        if (player != null) {
            player.sendMessage(text, Util.DUMMY_UUID);
        }
    }

    public PlayerPartyStorage getParty() {
        return StorageProxy.getParty(id);
    }

    public PCStorage getPC() {
        return StorageProxy.getPCForPlayer(id);
    }

    public boolean is(PlayerEntity player) {
        return player != null && id == player.getUniqueID();
    }

    public boolean is(GameProfile profile) {
        return profile != null && id == profile.getId();
    }

    public boolean is(CommandSource source) {
        return source != null && source.getEntity() instanceof PlayerEntity && id == ((PlayerEntity) source.getEntity()).getUniqueID();
    }

    public boolean hasPermission(String node) {
        return hasPermission(profile, getPlayer(), node);
    }

    private static boolean hasPermission(GameProfile profile, @Nullable PlayerEntity player, String node) {
        OpEntry entry = ServerLifecycleHooks.getCurrentServer().getPlayerList().getOppedPlayers().getEntry(profile);
        if (entry != null) {
            return true;
        }

        return PermissionAPI.hasPermission(profile, node, player == null ? null : new PlayerContext(player));
    }

    public static boolean hasPermission(GameProfile profile, String node) {
        PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(checkNotNull(profile, "profile").getId());
        return hasPermission(profile, player, node);
    }

    public static boolean hasPermission(PlayerEntity player, String node) {
        return hasPermission(checkNotNull(player, "player").getGameProfile(), player, node);
    }

    public static boolean hasPermission(CommandSource source, String node) {
        if (source.source instanceof MinecraftServer) {
            return true;
        }

        if (source.getEntity() instanceof PlayerEntity) {
            return hasPermission(((PlayerEntity) source.getEntity()).getGameProfile(), (PlayerEntity) source.getEntity(), node);
        }

        return false;
    }
}
