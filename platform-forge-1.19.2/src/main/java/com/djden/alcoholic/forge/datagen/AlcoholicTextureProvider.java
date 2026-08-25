package com.djden.alcoholic.forge.datagen;

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
        textures.put("block/hop_bine_0", 0xFF3F7A32);
        textures.put("block/hop_bine_1", 0xFF4C8F3C);
        textures.put("block/hop_bine_2", 0xFF5EA24A);
        textures.put("block/malting_floor", 0xFF8B6914);
        textures.put("block/mash_tun", 0xFF6B4F2A);
        textures.put("block/brewing_kettle", 0xFF6E7A84);
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
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        byte[] data = bytes.toByteArray();
        cache.writeIfNeeded(outputRoot.resolve(relative), data, Hashing.sha1().hashBytes(data));
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
