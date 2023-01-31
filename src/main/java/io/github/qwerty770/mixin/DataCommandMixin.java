package io.github.qwerty770.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.qwerty770.DataExporter;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.command.impl.data.DataCommand;
import net.minecraft.command.impl.data.IDataAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import static net.minecraft.command.impl.data.DataCommand.getSingleTag;

@Mixin(DataCommand.class)
public class DataCommandMixin {
    private static void exportStr(String str, MinecraftServer server){
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
                Path filePath = FileSystems.getDefault().getPath("export");
                File fileDir = new File(filePath.toString());

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
                Writer fileWriter = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
                fileWriter.write(str);
                fileWriter.close();
            } catch (IOException e) {
                DataExporter.LOGGER.warn("Could not save to file");
            }
        }
    }
    @Inject(at = @At("RETURN"), method = "getData(Lnet/minecraft/command/CommandSource;Lnet/minecraft/command/impl/data/IDataAccessor;)I")
    private static void export(CommandSource pSource, IDataAccessor pAccessor, CallbackInfoReturnable<Integer> info) throws CommandSyntaxException {
        if (!(pSource.getEntity() instanceof PlayerEntity)) {
            return;
        }
        ITextComponent component = pAccessor.getPrintSuccess(pAccessor.getData());
        String str = component.getString();
        exportStr(str, pSource.getServer());
    }

    @Inject(at = @At("RETURN"), method = "getData(Lnet/minecraft/command/CommandSource;Lnet/minecraft/command/impl/data/IDataAccessor;Lnet/minecraft/command/arguments/NBTPathArgument$NBTPath;)I")
    private static void export(CommandSource pSource, IDataAccessor pAccessor, NBTPathArgument.NBTPath pPath, CallbackInfoReturnable<Integer> info) throws CommandSyntaxException {
        INBT tag = getSingleTag(pPath, pAccessor);
        if (!(pSource.getEntity() instanceof PlayerEntity)) {
            return;
        }
        ITextComponent component = pAccessor.getPrintSuccess(tag);
        String str = component.getString();
        exportStr(str, pSource.getServer());
    }
}
