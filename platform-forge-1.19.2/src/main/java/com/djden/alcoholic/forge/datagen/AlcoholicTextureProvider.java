package com.djden.alcoholic.forge.datagen;

import com.djden.alcoholic.minecraft.menu.MachineLayout;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class AlcoholicTextureProvider implements DataProvider {
    private final Path outputRoot;

    AlcoholicTextureProvider(Path outputRoot) {
        this.outputRoot = outputRoot;
    }

    @Override
    public void run(CachedOutput cache) throws IOException {
        Map<String, Integer> textures = new LinkedHashMap<>();
        textures.put("block/barley_crop_0", 0xFF7A9A3A);
        textures.put("block/barley_crop_1", 0xFFC4B24A);
        textures.put("block/barley_crop_2", 0xFFD9B44A);
        textures.put("item/barley", 0xFFC9A227);
        textures.put("item/barley_seeds", 0xFF8C6B2A);
        textures.put("item/malted_barley", 0xFFA36A2B);
        textures.put("item/grist", 0xFFE6D3A3);
        textures.put("item/hops", 0xFF4F8F3A);
        textures.put("item/hop_rhizome", 0xFF6B4A2A);
        textures.put("item/spent_grain", 0xFF7A5A32);
        textures.put("item/wort_bucket", 0xFFC9A227);
        textures.put("item/hopped_wort_bucket", 0xFFB8860B);
        textures.put("item/beer_bucket", 0xFFD4A017);
        for (Map.Entry<String, Integer> entry : textures.entrySet()) {
            writePng(cache, "assets/alcoholic/textures/" + entry.getKey() + ".png", entry.getValue());
        }
        writeImage(cache, "assets/alcoholic/textures/item/wine_grimoire.png", bookIcon(0xFF6B2A4A, 0xFFE8D5B0));
        writeImage(cache, "assets/alcoholic/textures/item/beer_grimoire.png", bookIcon(0xFFC9A227, 0xFFF3E6C0));
        writeImage(cache, "assets/alcoholic/textures/gui/machine.png", machinePanel());
        writeImage(cache, "assets/alcoholic/textures/gui/elements.png", machineElements());
        writeImage(cache, "assets/alcoholic/textures/gui/grimoire/placeholder.png", grimoirePlaceholder());
    }

    private static BufferedImage bookIcon(int cover, int page) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fill(image, 3, 2, 11, 13, cover);
        fill(image, 3, 2, 2, 13, darken(cover));
        fill(image, 6, 3, 7, 11, page);
        fill(image, 7, 5, 5, 1, darken(page));
        fill(image, 7, 8, 5, 1, darken(page));
        fill(image, 7, 11, 4, 1, darken(page));
        return image;
    }

    private static BufferedImage grimoirePlaceholder() {
        int width = 100;
        int height = 56;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fill(image, 0, 0, width, height, 0xFFE8D5B0);
        rect(image, 0, 0, width, height, 0xFF6B4A2A);
        rect(image, 2, 2, width - 4, height - 4, 0xFFA67C52);
        fill(image, 8, 10, width - 16, 1, 0xFFC4A574);
        fill(image, 8, 18, width - 22, 1, 0xFFC4A574);
        fill(image, 8, 26, width - 18, 1, 0xFFC4A574);
        fill(image, 8, 34, width - 28, 1, 0xFFC4A574);
        fill(image, 8, 42, width - 24, 1, 0xFFC4A574);
        return image;
    }

    private static BufferedImage machinePanel() {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        fill(image, 0, 0, 176, 166, 0xFFC6C6C6);
        rect(image, 0, 0, 176, 166, 0xFF8B8B8B);
        fill(image, 1, 1, 174, 1, 0xFFFFFFFF);
        fill(image, 1, 1, 1, 164, 0xFFFFFFFF);
        fill(image, 7, 79, 162, 1, 0xFFA0A0A0);
        for (MachineLayout.SlotPos slot : MachineLayout.PLAYER_SLOTS) {
            drawSlotWell(image, slot.x() - 1, slot.y() - 1);
        }
        return image;
    }

    private static BufferedImage machineElements() {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        drawSlotWell(image, 0, 0);
        fill(image, 18, 0, 18, 52, 0xFF2B2B2B);
        rect(image, 18, 0, 18, 52, 0xFF8B8B8B);
        drawArrow(image, 36, 0, 0xFF8B8B8B);
        drawArrow(image, 36, 17, 0xFFC9A227);
        fill(image, 60, 0, 14, 14, 0xFF3A3A3A);
        rect(image, 60, 0, 14, 14, 0xFF6A6A6A);
        fill(image, 74, 0, 14, 14, 0xFFE08A1A);
        rect(image, 74, 0, 14, 14, 0xFFB05A10);
        return image;
    }

    private void writePng(CachedOutput cache, String relative, int argb) throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int border = darken(argb);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean edge = x == 0 || y == 0 || x == 15 || y == 15;
                image.setRGB(x, y, edge ? border : argb);
            }
        }
        writeImage(cache, relative, image);
    }

    private void writeImage(CachedOutput cache, String relative, BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        byte[] data = bytes.toByteArray();
        cache.writeIfNeeded(outputRoot.resolve(relative), data, Hashing.sha1().hashBytes(data));
    }

    private static void fill(BufferedImage image, int x, int y, int width, int height, int argb) {
        if (width <= 0 || height <= 0) {
            return;
        }
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                image.setRGB(x + column, y + row, argb);
            }
        }
    }

    private static void rect(BufferedImage image, int x, int y, int width, int height, int argb) {
        fill(image, x, y, width, 1, argb);
        fill(image, x, y + height - 1, width, 1, argb);
        fill(image, x, y, 1, height, argb);
        fill(image, x + width - 1, y, 1, height, argb);
    }

    private static void drawSlotWell(BufferedImage image, int x, int y) {
        fill(image, x, y, MachineLayout.SLOT_SIZE, MachineLayout.SLOT_SIZE, 0xFF8B8B8B);
        fill(image, x, y, MachineLayout.SLOT_SIZE, 1, 0xFF373737);
        fill(image, x, y, 1, MachineLayout.SLOT_SIZE, 0xFF373737);
        fill(image, x, y + MachineLayout.SLOT_SIZE - 1, MachineLayout.SLOT_SIZE, 1, 0xFFFFFFFF);
        fill(image, x + MachineLayout.SLOT_SIZE - 1, y, 1, MachineLayout.SLOT_SIZE, 0xFFFFFFFF);
        image.setRGB(x + MachineLayout.SLOT_SIZE - 1, y, 0xFF8B8B8B);
        image.setRGB(x, y + MachineLayout.SLOT_SIZE - 1, 0xFF8B8B8B);
    }

    private static void drawArrow(BufferedImage image, int x, int y, int argb) {
        fill(image, x, y + 6, 16, 5, argb);
        for (int step = 0; step < 8; step++) {
            fill(image, x + 16 + step, y + 2 + step, 1, 13 - step * 2, argb);
        }
    }

    private static int darken(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.max(0, ((argb >> 16) & 0xFF) - 40);
        int g = Math.max(0, ((argb >> 8) & 0xFF) - 40);
        int b = Math.max(0, (argb & 0xFF) - 40);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public String getName() {
        return "Alcoholic generated textures";
    }
}
