package io.github.qwerty770.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.qwerty770.DataExporter;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.minecraft.server.commands.data.DataCommands.getSingleTag;

@Mixin(DataCommands.class)
public class DataCommandMixin {
    private static void exportStr(String str, MinecraftServer server){
        System.out.println(str);
        if (server.getGameRules().getBoolean(DataExporter.RULE_EXPORT_TO_CLIPBOARD)) {
            DataExporter.LOGGER.debug("Exporting to clipboard");
            try {
                Minecraft.getInstance().keyboardHandler.setClipboard(str);
            } catch (Exception e) {
                DataExporter.LOGGER.warn("Could not set clipboard string");
            }
        }
        if (server.getGameRules().getBoolean(DataExporter.RULE_EXPORT_TO_FILE)) {
            DataExporter.LOGGER.debug("Exporting to file");
            try {
                File fileDir = new File("export\\");

                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                File file = new File("export\\data-export-" +
                        new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + ".txt");
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    int i = 0;
                    while (file.exists()){
                        i++;
                        file = new File("export\\data-export-" +
                                new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + i + ".txt");
                    }
                }
                file.createNewFile();
                FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8);
                fileWriter.write(str);
                fileWriter.close();
            } catch (IOException e) {
                DataExporter.LOGGER.warn("Could not save to file");
            }
        }
    }
    @Inject(at = @At("RETURN"), method = "getData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;)I", cancellable = true)
    private static void export(CommandSourceStack pSource, DataAccessor pAccessor, CallbackInfoReturnable<Integer> info) throws CommandSyntaxException {
        if (!(pSource.getEntity() instanceof Player)) {
            return;
        }
        Component component = pAccessor.getPrintSuccess(pAccessor.getData());
        String str = component.getString();
        exportStr(str, pSource.getServer());
    }

    @Inject(at = @At("RETURN"), method = "getData(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/server/commands/data/DataAccessor;Lnet/minecraft/commands/arguments/NbtPathArgument$NbtPath;)I", cancellable = true)
    private static void export(CommandSourceStack pSource, DataAccessor pAccessor, NbtPathArgument.NbtPath pPath, CallbackInfoReturnable<Integer> info) throws CommandSyntaxException {
        Tag tag = getSingleTag(pPath, pAccessor);
        if (!(pSource.getEntity() instanceof Player)) {
            return;
        }
        Component component = pAccessor.getPrintSuccess(tag);
        String str = component.getString();
        exportStr(str, pSource.getServer());
    }
}
