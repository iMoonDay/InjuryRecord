package com.imoonday.injuryrecord.client;

import com.imoonday.injuryrecord.data.DamageData;
import com.imoonday.injuryrecord.data.DamageRecord;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 伤害记录列表界面
 */
public class DamageRecordListScreen extends Screen {

    private PlainTextButton textButton;
    private DamageRecordList damageRecordList;
    public DamageDataInfo damageDataInfo;
    private DamageRecord selectedRecord;
    private boolean synced;
    private boolean includeOffline;

    public DamageRecordListScreen() {
        super(Component.translatable("screen.injuryrecord.title"));
    }

    @Override
    protected void init() {
        super.init();
        int listWidth = width / 5;
        this.damageRecordList = new DamageRecordList(minecraft, listWidth, height, 20, height - 20, 20);
        addRenderableWidget(this.damageRecordList);

        this.damageDataInfo = new DamageDataInfo(minecraft, width - listWidth, height, 20, height - 20, 28);
        addRenderableWidget(this.damageDataInfo);

        int buttonY = height - 20 + (20 - font.lineHeight) / 2 + 1;

        MutableComponent message = Component.translatable("screen.injuryrecord.get_complete_data");
        int textWidth = font.width(message);
        textButton = new PlainTextButton(width - textWidth - 5, buttonY, textWidth, font.lineHeight + 1, message, button -> requestFullData(), font);
        if (synced) {
            updateTextButton();
            addRenderableWidget(textButton);
        }

        MutableComponent message1 = Component.translatable("screen.injuryrecord.refresh");
        int textWidth1 = font.width(message1);
        PlainTextButton refreshButton = new PlainTextButton((listWidth - textWidth1) / 2, buttonY, textWidth1, font.lineHeight + 1, message1, button -> ClientUtils.requestRecords(includeOffline), font);
        addRenderableWidget(refreshButton);

        if (!includeOffline) {
            MutableComponent message2 = Component.translatable("screen.injuryrecord.get_offline_data");
            int textWidth2 = font.width(message2);
            PlainTextButton requestOfflineButton = new PlainTextButton(width - textWidth2 - 5, 6, textWidth2, font.lineHeight, message2, button -> {
                includeOffline = true;
                ClientUtils.requestRecords(true);
                synced = false;
            }, font);
            addRenderableWidget(requestOfflineButton);
        }
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        if (!synced) {
            MutableComponent message = Component.translatable("screen.injuryrecord.syncing");
            font.draw(pPoseStack, message, (width - font.width(message)) / 2f, 6, 0xFFFFFF);
        } else if (this.selectedRecord != null) {
            MutableComponent text = title.copy().append(" - ").append(this.selectedRecord.getName());
            if (this.damageDataInfo != null) {
                text = text.append(" (" + this.damageDataInfo.children().size() + ")");
            }
            font.draw(pPoseStack, text, (width - font.width(text)) / 2f, 6, 0xFFFFFF);
        } else {
            font.draw(pPoseStack, title, (width - font.width(title)) / 2f, 6, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
        this.damageRecordList.children().stream().filter(entry -> entry.record == selectedRecord).findFirst().ifPresent(entry -> {
            this.damageRecordList.setFocused(entry);
            this.damageRecordList.ensureVisible(entry);
        });
        this.damageDataInfo.updateInfo();
    }

    public void updateList() {
        if (!synced) {
            synced = true;
            rebuildWidgets();
        } else if (this.damageRecordList != null) {
            this.damageRecordList.updateList();
        }
    }

    public void updateInfo() {
        if (this.damageDataInfo != null) {
            this.damageDataInfo.updateInfo();
        }
    }

    public void requestFullData() {
        if (selectedRecord != null) {
            ClientUtils.requestRecords(selectedRecord.getUuid(), includeOffline);
        }
    }

    public void updateTextButton() {
        if (textButton != null) {
            boolean active = selectedRecord != null && selectedRecord.isIncomplete();
            textButton.active = active;
            textButton.visible = active;
        }
    }

    public class DamageRecordList extends ObjectSelectionList<DamageRecordList.Entry> {

        public DamageRecordList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
            super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
            setRenderBackground(false);
            setRenderHeader(false, 0);
            setLeftPos(0);

            if (synced) {
                updateList();
                focusFirst();
            }
        }

        private void focusFirst() {
            List<Entry> children = children();
            if (!children.isEmpty() && getFocused() == null) {
                Entry first = children.get(0);
                setFocused(first);
                selectedRecord = first.record;
                updateInfo();
                updateTextButton();
            }
        }

        @Override
        public void ensureVisible(@NotNull Entry entry) {
            super.ensureVisible(entry);
        }

        @Override
        public int getRowLeft() {
            return 0;
        }

        @Override
        public int getRowWidth() {
            return width;
        }

        @Override
        protected int getScrollbarPosition() {
            return width - 6;
        }

        private void updateList() {
            UUID uuid = selectedRecord != null ? selectedRecord.getUuid() : null;
            Component name = selectedRecord != null ? selectedRecord.getName() : null;
            selectedRecord = null;
            clearEntries();
            Entry focusedEntry = null;

            Map<UUID, DamageRecord> records = ClientDamageRecordsCache.INSTANCE.getRecords();
            ArrayList<Map.Entry<UUID, DamageRecord>> entries = new ArrayList<>(records.entrySet());
            entries.sort(Comparator.comparing(e -> e.getValue().getName().getString()));
            entries.sort(Comparator.comparing(e -> ClientUtils.isOffline(e.getValue().getUuid())));
            for (Map.Entry<UUID, DamageRecord> e : entries) {
                UUID uuid1 = e.getKey();
                DamageRecord record = e.getValue();

                Entry entry = new Entry(record);
                addEntry(entry);

                if (selectedRecord == null && (uuid1.equals(uuid) || name != null && record.getName().getContents().equals(name.getContents()))) {
                    selectedRecord = record;
                    focusedEntry = entry;
                }
            }
            updateInfo();
            updateTextButton();

            if (focusedEntry != null) {
                damageRecordList.setFocused(focusedEntry);
                damageRecordList.ensureVisible(focusedEntry);
            } else if (selectedRecord == null) {
                focusFirst();
            }
        }

        public class Entry extends ObjectSelectionList.Entry<DamageRecordList.Entry> {

            private final DamageRecord record;

            public Entry(DamageRecord record) {
                this.record = record;
            }

            @Override
            public @NotNull Component getNarration() {
                return record.getName();
            }

            @Override
            public void render(@NotNull PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                if (selectedRecord == record) {
                    GuiComponent.fill(pPoseStack, pLeft, pTop, pLeft + pWidth, pTop + pHeight, 0x44FFFFFF);
                } else if (pIsMouseOver) {
                    GuiComponent.fill(pPoseStack, pLeft, pTop, pLeft + pWidth, pTop + pHeight, 0x88000000);
                }

                Component name = record.getName();
                if (ClientUtils.isOffline(record.getUuid())) {
                    name = Component.translatable("screen.injuryrecord.offline", name).withStyle(ChatFormatting.GRAY);
                }
                font.draw(pPoseStack, name, pLeft + (pWidth - font.width(name)) / 2f, pTop + (pHeight - font.lineHeight) / 2f + 1, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                if (pButton == 0) {
                    selectedRecord = record;
                    updateInfo();
                    updateTextButton();
                    return true;
                }
                return super.mouseClicked(pMouseX, pMouseY, pButton);
            }
        }
    }

    public class DamageDataInfo extends ObjectSelectionList<DamageDataInfo.Entry> {


        public DamageDataInfo(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
            super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
            setRenderBackground(false);
            setRenderHeader(false, 0);
            setLeftPos(DamageRecordListScreen.this.width - pWidth);

            if (synced) {
                updateInfo();
            }
        }

        public void updateInfo() {
            this.setScrollAmount(0);
            this.clearEntries();
            if (selectedRecord != null) {
                selectedRecord.getInjuries().forEach(data -> addEntry(new Entry(data)));
            }
        }

        @Override
        public int getRowLeft() {
            return this.x0 + 10;
        }

        @Override
        public int getRowWidth() {
            return this.width - 20 - 6;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.x1 - 6;
        }

        public class Entry extends ObjectSelectionList.Entry<DamageDataInfo.Entry> {

            private final DamageData data;

            public Entry(DamageData data) {
                this.data = data;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(@NotNull PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                renderData(pPoseStack, pMouseX, pMouseY, pLeft, pTop, pWidth, pHeight, data, pIsMouseOver);
            }

            public void renderData(PoseStack poseStack, int mouseX, int mouseY, int x, int y, int width, int height, DamageData data, boolean isMouseOver) {
                int startY = y + (height / 2 - font.lineHeight) / 2 + 1;
                int startY2 = y + height - font.lineHeight - 1;
                boolean dead = data.isDead();
                int color = dead ? 0xFF0000 : 0xFFFFFF;

                renderName(poseStack, x, startY, data, color);
                renderLocation(poseStack, x + width, startY, data, color);
                renderDamage(poseStack, x, startY2, dead, data, color);
                renderTime(poseStack, x + width, startY2, data, color);

                if (dead) {
                    Component deathMessage = data.getDeathMessage();
                    if (deathMessage != null && isMouseOver) {
                        renderTooltip(poseStack, deathMessage, mouseX, mouseY);
                    }
                }
            }

            private void renderTime(PoseStack poseStack, int right, int y, DamageData data, int color) {
                String time = data.getFormattedTime();
                font.draw(poseStack, time, right - 4 - font.width(time), y, color);
            }

            private void renderDamage(PoseStack poseStack, int x, int y, boolean dead, DamageData data, int color) {
                String amount = String.format("%.1f", data.getAmount());
                if (amount.endsWith(".0")) {
                    amount = amount.substring(0, amount.length() - 2);
                }
                MutableComponent damageText = dead ? Component.translatable("screen.injuryrecord.dead") : Component.translatable("screen.injuryrecord.damage", amount);
                font.draw(poseStack, damageText, x + 4, y, color);
            }

            private void renderLocation(PoseStack poseStack, int right, int y, DamageData data, int color) {
                GlobalPos location = data.getLocation();
                ResourceLocation dimensionId = location.dimension().location();
                BlockPos pos = location.pos();
                String dimensionKey = "dimension." + dimensionId.toLanguageKey();
                Component locationText = Component.translatable("screen.injuryrecord.location", I18n.exists(dimensionKey) ? I18n.get(dimensionKey) : dimensionId.toString(), pos.getX(), pos.getY(), pos.getZ());
                font.draw(poseStack, locationText, right - 4 - font.width(locationText), y, color);
            }

            private void renderName(PoseStack poseStack, int x, int y, DamageData data, int color) {
                Component name = data.getDirectEntityName();
                if (name == null) {
                    name = Component.literal(data.getMsgId());
                } else {
                    Component attackerName = data.getAttackerName();
                    if (attackerName != null && !attackerName.getContents().equals(name.getContents())) {
                        name = name.copy().append(" (").append(attackerName).append(")");
                    }
                }
                font.draw(poseStack, Component.translatable("screen.injuryrecord.source", name), x + 4, y, color);
            }
        }
    }
}
