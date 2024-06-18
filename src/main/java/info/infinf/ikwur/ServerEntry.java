package info.infinf.ikwur;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;

public final class ServerEntry implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(ServerEntry::initCmd);
	}

	public static void initCmd(
			CommandDispatcher<ServerCommandSource> dispatcher,
			CommandRegistryAccess registryAccess,
			CommandManager.RegistrationEnvironment env) {
		dispatcher.register(CommandManager.literal("w")
			.executes(ctx -> {
				var sb = new StringBuilder();
				for (var pl: ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
					sb.append(pl.getName().getString());
					sb.append(": [");
					var pos = pl.getBlockPos();
					sb.append(pos.getX());
					sb.append(',');
					sb.append(pos.getY());
					sb.append(',');
					sb.append(pos.getZ());
					sb.append("] ");
					sb.append(pl.getWorld().getRegistryKey().getValue());
					sb.append('\n');
				}
				ctx.getSource().sendFeedback(Text.of(sb.toString()), false);
				return 1;
			}));
	}
}
