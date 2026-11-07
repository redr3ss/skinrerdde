package com.skinall;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SkinAllClient implements ClientModInitializer {

    public static final Identifier SKIN_ID = Identifier.of("skinall", "custom_skin");

    public static volatile boolean enabled = true;
    public static volatile boolean slim = false;
    public static volatile boolean includeSelf = true;
    public static volatile boolean ready = false;
    public static String file = "skin.png";

    private static Path dir() {
        return FabricLoader.getInstance().getConfigDir().resolve("skinall");
    }

    @Override
    public void onInitializeClient() {
        try { Files.createDirectories(dir()); } catch (Exception ignored) {}
        loadConfig();

        // Скин грузим, когда клиент уже полностью запущен
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED
                .register(client -> loadSkin(null));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) ->
                dispatcher.register(ClientCommandManager.literal("skinall")
                        .then(ClientCommandManager.literal("on").executes(c -> {
                            enabled = true; saveConfig(); say(c.getSource(), "Скин включён"); return 1; }))
                        .then(ClientCommandManager.literal("off").executes(c -> {
                            enabled = false; saveConfig(); say(c.getSource(), "Скин выключен"); return 1; }))
                        .then(ClientCommandManager.literal("reload").executes(c -> {
                            loadSkin(c.getSource()); return 1; }))
                        .then(ClientCommandManager.literal("slim").executes(c -> {
                            slim = true; saveConfig(); say(c.getSource(), "Модель: тонкие руки (Alex)"); return 1; }))
                        .then(ClientCommandManager.literal("classic").executes(c -> {
                            slim = false; saveConfig(); say(c.getSource(), "Модель: классическая (Steve)"); return 1; }))
                        .then(ClientCommandManager.literal("self")
                                .then(ClientCommandManager.literal("on").executes(c -> {
                                    includeSelf = true; saveConfig(); say(c.getSource(), "Скин применяется и к вам"); return 1; }))
                                .then(ClientCommandManager.literal("off").executes(c -> {
                                    includeSelf = false; saveConfig(); say(c.getSource(), "Ваш собственный скин не трогаем"); return 1; })))
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("name", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            file = StringArgumentType.getString(c, "name").trim();
                                            saveConfig();
                                            loadSkin(c.getSource());
                                            return 1;
                                        })))
                        .executes(c -> {
                            say(c.getSource(), "/skinall on|off|reload|slim|classic|self on|off|set <файл.png>");
                            say(c.getSource(), "Папка со скинами: " + dir());
                            return 1;
                        })));
    }

    public static void loadSkin(FabricClientCommandSource src) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Path p = dir().resolve(file).normalize();
        if (!p.startsWith(dir())) { say(src, "Недопустимое имя файла"); return; }
        try (InputStream in = Files.newInputStream(p)) {
            NativeImage img = NativeImage.read(in);
            if (img.getWidth() != 64 || img.getHeight() != 64) {
                int w = img.getWidth(), h = img.getHeight();
                img.close();
                say(src, "Нужен скин 64x64 (у вас " + w + "x" + h + ")");
                return;
            }
            mc.execute(() -> {
                mc.getTextureManager().destroyTexture(SKIN_ID);
                mc.getTextureManager().registerTexture(SKIN_ID, new NativeImageBackedTexture(() -> "skinall_custom_skin", img));
                ready = true;
                say(src, "Скин загружен: " + file);
            });
        } catch (java.nio.file.NoSuchFileException e) {
            ready = false;
            say(src, "Файл не найден: " + p);
        } catch (Exception e) {
            ready = false;
            say(src, "Ошибка загрузки скина: " + e.getMessage());
        }
    }

    private static void say(FabricClientCommandSource src, String msg) {
        if (src != null) src.sendFeedback(Text.literal("[SkinAll] " + msg));
        else System.out.println("[SkinAll] " + msg);
    }

    private static Path cfg() { return dir().resolve("config.properties"); }

    private static void loadConfig() {
        Properties pr = new Properties();
        try (InputStream in = Files.newInputStream(cfg())) {
            pr.load(in);
            enabled = Boolean.parseBoolean(pr.getProperty("enabled", "true"));
            slim = Boolean.parseBoolean(pr.getProperty("slim", "false"));
            includeSelf = Boolean.parseBoolean(pr.getProperty("includeSelf", "true"));
            file = pr.getProperty("file", "skin.png");
        } catch (Exception ignored) {}
    }

    private static void saveConfig() {
        Properties pr = new Properties();
        pr.setProperty("enabled", String.valueOf(enabled));
        pr.setProperty("slim", String.valueOf(slim));
        pr.setProperty("includeSelf", String.valueOf(includeSelf));
        pr.setProperty("file", file);
        try (var out = Files.newOutputStream(cfg())) {
            pr.store(out, "SkinAll");
        } catch (Exception ignored) {}
    }
}
