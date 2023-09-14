package com.humbrain.suivichall;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.stream.Collectors;

public class SuiviChall implements ModInitializer {

    private static final int SPACING = 10;
    private static final int PADDING = 5;
    private static final int BORDER_THICKNESS = 5;
    private static final int BG_COLOR = 0x80000000;
    private static final int BORDER_COLOR = 0x00000000;

    @Override
    public void onInitialize() {
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (client.options.debugEnabled) {
                return;
            }
            if (player == null) return;

            int yStart = 10;

            for (ItemStack stack : player.getInventory().main) {
                if (stack.hasNbt() && stack.getNbt().contains("display")) {
                    NbtList loreTag = stack.getNbt().getCompound("display").getList("Lore", 8);
                    List<Text> lores = loreTag.stream().map(tag -> Text.Serializer.fromJson(tag.asString())).collect(Collectors.toList());
                    yStart = processLores(lores, matrixStack, yStart, client);
                }
            }
        });
    }

    private int processLores(List<Text> lores, MatrixStack matrixStack, int yStart, MinecraftClient client) {
        for (int i = 0; i < lores.size(); i++) {
            String loreStr = lores.get(i).getString();

            if (loreStr.contains("Progression :")) {
                String progressionText = createProgressionText(lores, i);
                int textWidth = client.textRenderer.getWidth(progressionText);
                yStart = drawProgression(matrixStack, client, yStart, progressionText, textWidth);
            }
        }
        return yStart;
    }

    private String createProgressionText(List<Text> lores, int index) {
        String loreStr = lores.get(index).getString();
        String[] parts = loreStr.split(":");
        String[] progression = parts[1].trim().split("/");

        int current = Integer.parseInt(progression[0].trim());
        int total = Integer.parseInt(progression[1].trim());
        double percentage = ((double) current / (double) total) * 100;
        percentage = Math.round(percentage * 100.0) / 100.0;

        String colorCode = getColorCode(percentage);
        StringBuilder allLinesBefore = getAllLinesBefore(lores, index);

        return allLinesBefore + " : " + colorCode + current + "/" + total + " (" + percentage + "%)§r";
    }

    private StringBuilder getAllLinesBefore(List<Text> lores, int index) {
        boolean starting = false;
        StringBuilder allLinesBefore = new StringBuilder();
        for (int j = 0; j < index; j++) {
            if (!starting) {
                if (lores.get(j).getString().contains("Objectif")) {
                    starting = true;
                }
                continue;
            }
            allLinesBefore.append(lores.get(j).getString().trim());
            if (j < index - 1) {
                allLinesBefore.append(" "); // Add a space between lines, you can replace with "\n" if you want them on separate lines
            }
        }
        return allLinesBefore;
    }

    private String getColorCode(double percentage) {
        if (percentage == 100) {
            return "§a";
        } else if (percentage > 50) {
            return "§6";
        } else {
            return "§c";
        }
    }

    private int drawProgression(MatrixStack matrixStack, MinecraftClient client, int yStart, String progressionText, int textWidth) {
        int frameWidth = textWidth + (2 * PADDING);
        int frameHeight = SPACING;

        int xPosition = 10;
        int yPosition = yStart;

        drawBackgroundWithBorder(matrixStack, xPosition, yPosition, frameWidth, frameHeight);
        drawText(matrixStack, client, progressionText, textWidth, xPosition, yPosition);

        return yStart + frameHeight + SPACING;
    }

    private void drawBackgroundWithBorder(MatrixStack matrixStack, int xPosition, int yPosition, int frameWidth, int frameHeight) {
        DrawableHelper.fill(matrixStack, xPosition - PADDING, yPosition - PADDING, xPosition + frameWidth, yPosition + frameHeight, BG_COLOR);
        drawBorder(matrixStack, xPosition, yPosition, frameWidth, frameHeight);
    }

    private void drawBorder(MatrixStack matrixStack, int xPosition, int yPosition, int frameWidth, int frameHeight) {
        DrawableHelper.fill(matrixStack, xPosition - PADDING, yPosition - PADDING, xPosition + frameWidth, yPosition - PADDING + BORDER_THICKNESS, BORDER_COLOR);
        DrawableHelper.fill(matrixStack, xPosition - PADDING, yPosition + frameHeight - BORDER_THICKNESS, xPosition + frameWidth, yPosition + frameHeight, BORDER_COLOR);
        DrawableHelper.fill(matrixStack, xPosition - PADDING, yPosition - PADDING, xPosition - PADDING + BORDER_THICKNESS, yPosition + frameHeight, BORDER_COLOR);
        DrawableHelper.fill(matrixStack, xPosition + frameWidth - BORDER_THICKNESS, yPosition - PADDING, xPosition + frameWidth, yPosition + frameHeight, BORDER_COLOR);
    }

    private void drawText(MatrixStack matrixStack, MinecraftClient client, String progressionText, int textWidth, int xPosition, int yPosition) {
        int frameWidth = textWidth + (2 * PADDING);
        int frameHeight = SPACING;

        int xCentered = xPosition + (frameWidth / 2) - (textWidth / 2);
        int yCentered = yPosition + (frameHeight - client.textRenderer.fontHeight) / 2;

        client.textRenderer.draw(matrixStack, progressionText, xCentered, yCentered, 0xFFFFFF);
    }


}
