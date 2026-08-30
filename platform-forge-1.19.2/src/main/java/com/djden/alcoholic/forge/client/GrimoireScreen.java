package com.djden.alcoholic.forge.client;

import com.djden.alcoholic.minecraft.guide.GrimoireCatalog;
import com.djden.alcoholic.minecraft.guide.GrimoireChapter;
import com.djden.alcoholic.minecraft.guide.GrimoireKind;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class GrimoireScreen extends Screen {
    private static final ResourceLocation BOOK =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/book.png");
    private static final ResourceLocation PLACEHOLDER = ResourceLocation.fromNamespaceAndPath(
            "alcoholic",
            GrimoireCatalog.PLACEHOLDER_TEXTURE
    );
    private static final int BOOK_WIDTH = 192;
    private static final int BOOK_HEIGHT = 192;
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_LEFT = 36;
    private static final int TITLE_TOP = 16;
    private static final int BODY_TOP = 30;
    private static final int PAGE_BOTTOM = 146;
    private static final int TOC_LINK_TOP = 148;
    private static final int ARROW_Y = 159;
    private static final int ARROW_WIDTH = 23;
    private static final int ARROW_HEIGHT = 13;
    private static final int PREV_ARROW_X = 26;
    private static final int NEXT_ARROW_X = 116;
    private static final int MARGIN = 16;
    private static final int PLATE_WIDTH = GrimoireCatalog.ILLUSTRATION_WIDTH;
    private static final int PLATE_HEIGHT = GrimoireCatalog.ILLUSTRATION_HEIGHT;

    private final GrimoireKind kind;
    private final List<GrimoireChapter> chapters;
    private final List<TocHit> tocHits = new ArrayList<>();
    private boolean tableOfContents = true;
    private int chapterIndex;
    private int pageIndex;
    private float scale = 1.0F;
    private int originX;
    private int originY;

    public GrimoireScreen(GrimoireKind kind) {
        super(Component.translatable(GrimoireCatalog.itemTranslationKey(kind)));
        this.kind = kind;
        this.chapters = GrimoireCatalog.chapters(kind);
    }

    @Override
    protected void init() {
        layoutBook();
        rebuildButtons();
    }

    private void layoutBook() {
        scale = Math.min((width - MARGIN * 2) / (float) BOOK_WIDTH, (height - MARGIN * 2) / (float) BOOK_HEIGHT);
        int drawWidth = Math.round(BOOK_WIDTH * scale);
        int drawHeight = Math.round(BOOK_HEIGHT * scale);
        originX = (width - drawWidth) / 2;
        originY = (height - drawHeight) / 2;
    }

    private void rebuildButtons() {
        clearWidgets();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderBackground(pose);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BOOK);
        pose.pushPose();
        pose.translate(originX, originY, 0.0F);
        pose.scale(scale, scale, 1.0F);
        blit(pose, 0, 0, 0, 0, BOOK_WIDTH, BOOK_HEIGHT);
        if (tableOfContents) {
            renderTableOfContents(pose);
        } else {
            renderChapterPage(pose, mouseX, mouseY);
        }
        pose.popPose();
        super.render(pose, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (tableOfContents) {
            for (TocHit hit : tocHits) {
                if (hit.contains(mouseX, mouseY)) {
                    tableOfContents = false;
                    chapterIndex = hit.chapterIndex;
                    pageIndex = 0;
                    playClick();
                    return true;
                }
            }
        } else if (bookHit(mouseX, mouseY, TEXT_LEFT, TOC_LINK_TOP, tocLinkWidth(), font.lineHeight)) {
            tableOfContents = true;
            playClick();
            return true;
        } else if (pageIndex > 0 && bookHit(mouseX, mouseY, PREV_ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
            pageIndex--;
            playClick();
            return true;
        } else if (hasNextPage() && bookHit(mouseX, mouseY, NEXT_ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT)) {
            pageIndex++;
            playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderTableOfContents(PoseStack pose) {
        tocHits.clear();
        font.draw(pose, title, TEXT_LEFT, TITLE_TOP, 0x3F2A14);
        int y = BODY_TOP;
        int step = font.lineHeight + 1;
        for (int index = 0; index < chapters.size(); index++) {
            Component line = Component.literal((index + 1) + ". ")
                    .append(Component.translatable(chapters.get(index).titleKey()));
            font.draw(pose, line, TEXT_LEFT, y, 0x000000);
            tocHits.add(new TocHit(screenX(TEXT_LEFT), screenY(y), screenSize(TEXT_WIDTH), screenSize(step), index));
            y += step;
        }
    }

    private void renderChapterPage(PoseStack pose, int mouseX, int mouseY) {
        GrimoireChapter chapter = currentChapter();
        font.draw(pose, Component.translatable(chapter.titleKey()), TEXT_LEFT, TITLE_TOP, 0x3F2A14);
        int textTop = BODY_TOP;
        if (pageIndex == 0) {
            int plateX = TEXT_LEFT + (TEXT_WIDTH - PLATE_WIDTH) / 2;
            renderIllustration(pose, plateX, textTop, chapter);
            textTop += PLATE_HEIGHT + 4;
        }
        drawWrapped(pose, Component.translatable(chapter.pageKeys().get(pageIndex)), TEXT_LEFT, textTop);
        Component toc = Component.translatable("grimoire.alcoholic.toc");
        boolean tocHover = bookHit(mouseX, mouseY, TEXT_LEFT, TOC_LINK_TOP, font.width(toc), font.lineHeight);
        font.draw(pose, toc, TEXT_LEFT, TOC_LINK_TOP, tocHover ? 0xC45A12 : 0x3F2A14);
        RenderSystem.setShaderTexture(0, BOOK);
        if (pageIndex > 0) {
            boolean hover = bookHit(mouseX, mouseY, PREV_ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT);
            blit(pose, PREV_ARROW_X, ARROW_Y, 0, hover ? 215 : 192, ARROW_WIDTH, ARROW_HEIGHT);
        }
        if (hasNextPage()) {
            boolean hover = bookHit(mouseX, mouseY, NEXT_ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT);
            blit(pose, NEXT_ARROW_X, ARROW_Y, 23, hover ? 215 : 192, ARROW_WIDTH, ARROW_HEIGHT);
        }
    }

    private void drawWrapped(PoseStack pose, Component text, int x, int y) {
        List<FormattedCharSequence> lines = font.split(text, TEXT_WIDTH);
        int maxLines = Math.max(0, (PAGE_BOTTOM - y) / font.lineHeight);
        int limit = Math.min(lines.size(), maxLines);
        for (int index = 0; index < limit; index++) {
            font.draw(pose, lines.get(index), x, y + index * font.lineHeight, 0x000000);
        }
    }

    private void renderIllustration(PoseStack pose, int x, int y, GrimoireChapter chapter) {
        ResourceLocation plate = ResourceLocation.fromNamespaceAndPath(
                "alcoholic",
                GrimoireCatalog.illustrationTexture(kind, chapter.illustrationId())
        );
        ResourceLocation texture = hasTexture(plate) ? plate : PLACEHOLDER;
        RenderSystem.setShaderTexture(0, texture);
        blit(
                pose,
                x,
                y,
                0,
                0,
                PLATE_WIDTH,
                PLATE_HEIGHT,
                GrimoireCatalog.ILLUSTRATION_WIDTH,
                GrimoireCatalog.ILLUSTRATION_HEIGHT
        );
    }

    private boolean hasTexture(ResourceLocation location) {
        return minecraft != null && minecraft.getResourceManager().getResource(location).isPresent();
    }

    private GrimoireChapter currentChapter() {
        return chapters.get(chapterIndex);
    }

    private boolean hasNextPage() {
        return pageIndex < currentChapter().pageKeys().size() - 1;
    }

    private int tocLinkWidth() {
        return font.width(Component.translatable("grimoire.alcoholic.toc"));
    }

    private boolean bookHit(double mouseX, double mouseY, int bookX, int bookY, int bookW, int bookH) {
        return mouseX >= screenX(bookX)
                && mouseX < screenX(bookX + bookW)
                && mouseY >= screenY(bookY)
                && mouseY < screenY(bookY + bookH);
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private int screenX(int bookX) {
        return originX + Math.round(bookX * scale);
    }

    private int screenY(int bookY) {
        return originY + Math.round(bookY * scale);
    }

    private int screenSize(int bookSize) {
        return Math.max(1, Math.round(bookSize * scale));
    }

    private record TocHit(int x, int y, int width, int height, int chapterIndex) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
