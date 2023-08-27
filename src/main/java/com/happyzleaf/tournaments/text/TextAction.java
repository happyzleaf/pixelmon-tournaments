package com.happyzleaf.tournaments.text;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.UUIDArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Trying not to expose any data, so I'm not giving away any info if the command was executed incorrectly.
 *
 * @author happyz
 */
public class TextAction {
    public static final SimpleCommandExceptionType DIFFERENT_SOURCE = new SimpleCommandExceptionType(Text.of(TextFormatting.RED, "This is not for you."));

    private static final UUID KEY = UUID.fromString("e1664588-d2f1-4fdf-8487-2a67fd85956e");
    private static final Map<UUID, Controller> ACTIONS = new HashMap<>();

    /**
     * Registers the action and gives back the command to execute.
     *
     * @param recipient The {@link PlayerEntity} that shall use this command. Optional.
     * @param action    The {@link Callback} to run when the command executes.
     * @return The command to execute.
     */
    public static String register(@Nullable PlayerEntity recipient, Callback action) {
        UUID id = UUID.randomUUID();
        ACTIONS.put(id, new Controller(recipient, action));
        return "/" + KEY + " " + id;
    }

    public static LiteralArgumentBuilder<CommandSource> build() {
        return Commands.literal(KEY.toString())
                .then(
                        Commands.argument("action", new UUIDArgument())
                                .executes(TextAction::execute)
                );
    }

    private static int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
        UUID actionId = context.getArgument("action", UUID.class);
        Controller action = ACTIONS.get(actionId);
        if (action != null && action.run(context.getSource())) {
            // If the action exists and returned true, we can dispose it
            ACTIONS.remove(actionId);
        }

        return 1;
    }

    public interface Callback {
        void run(PlayerEntity src, Controller ctx) throws CommandSyntaxException;
    }

    public static class Controller {
        private final PlayerEntity recipient;
        private final Callback callback;

        private boolean keepAlive = false;

        public Controller(@Nullable PlayerEntity recipient, Callback callback) {
            this.recipient = recipient;
            this.callback = callback;
        }

        public void keepAlive() {
            this.keepAlive = true;
        }

        public boolean run(CommandSource source) throws CommandSyntaxException {
            if (!(source.getEntity() instanceof PlayerEntity)) {
                return false;
            }

            if (this.recipient != null && (!(this.recipient.getEntity() instanceof PlayerEntity) || !this.recipient.getEntity().getUniqueID().equals(source.getEntity().getUniqueID()))) {
                throw DIFFERENT_SOURCE.create();
            }

            this.callback.run((PlayerEntity) source.getEntity(), this);
            boolean cancel = !this.keepAlive;
            this.keepAlive = false;
            return cancel;
        }
    }
}
