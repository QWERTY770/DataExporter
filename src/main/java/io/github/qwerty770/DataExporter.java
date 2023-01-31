package io.github.qwerty770;

import net.minecraft.world.GameRules;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("dataexp")
public class DataExporter {
    public static final Logger LOGGER = LogManager.getLogger();
    public static GameRules.RuleKey<GameRules.BooleanValue> RULE_EXPORT_TO_CLIPBOARD;
    public static GameRules.RuleKey<GameRules.BooleanValue> RULE_EXPORT_TO_FILE;

    public DataExporter(){
        RULE_EXPORT_TO_CLIPBOARD = GameRules.register("exportToClipboard", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
        RULE_EXPORT_TO_FILE = GameRules.register("exportToFile", GameRules.Category.MISC, GameRules.BooleanValue.create(false));
        LOGGER.info("loaded 2 new gamerules");
    }
}
