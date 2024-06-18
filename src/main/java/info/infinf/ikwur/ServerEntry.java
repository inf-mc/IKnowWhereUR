package info.infinf.ikwur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

public final class ServerEntry implements ModInitializer {
    public static class Config {
        public boolean glow;
        public int glow_ticks;

        public Config(boolean glow, int glow_ticks) {
            this.glow = glow;
            this.glow_ticks = glow_ticks;
        }

        public static Config getDefault() {
            return new Config(true, 200);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("IKnowWhereUR");
    public static final String MOD_ID = "ikwur";
    public static final String CONF_FILE_NAME = "config.json";
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .setLenient()
            .create();

    public Config config = Config.getDefault();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::initCmd);

        var confDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        var confFile = confDir.resolve(CONF_FILE_NAME).toFile();
        try {
            confDir.toFile().mkdirs();
            var tmp = GSON.fromJson(new FileReader(confDir.resolve(CONF_FILE_NAME).toFile()), Config.class);
            if (tmp != null) {
                config = tmp;
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("Config file not found, try to create one.");
            try {
                var f = new FileWriter(confFile);
                f.write(GSON.toJson(config));
                f.close();
            } catch (Exception e2) {
                LOGGER.error("Failed to create config file.", e2);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config file, use default configs.", e);
        }
    }

    public void initCmd(
            @NotNull CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment env) {
        dispatcher.register(CommandManager.literal("w")
                .executes(ctx -> {
                    var sb = new StringBuilder();
                    for (var pl: ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                        if (config.glow) {
                            pl.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.GLOWING, config.glow_ticks, 0, false, false));
                        }
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
