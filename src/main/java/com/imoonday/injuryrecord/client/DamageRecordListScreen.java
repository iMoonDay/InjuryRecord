package com.imoonday.injuryrecord.client;

import com.imoonday.injuryrecord.Config;
import com.imoonday.injuryrecord.data.DamageData;
import com.imoonday.injuryrecord.data.DamageRecord;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 伤害记录列表界面
 */
public class DamageRecordListScreen extends Screen {

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

        MutableComponent refreshMessage = Component.translatable("screen.injuryrecord.refresh");
        int textWidth = font.width(refreshMessage);
        PlainTextButton refreshButton = new PlainTextButton((listWidth - textWidth) / 2, buttonY, textWidth, font.lineHeight + 1, refreshMessage, button -> ClientUtils.requestRecords(includeOffline), font);
        addRenderableWidget(refreshButton);

        Boolean permission = Config.removePermission.get();

        MutableComponent deleteAllMessage = Component.translatable("screen.injuryrecord.delete_all");
        int textWidth1 = font.width(deleteAllMessage);
        PlainTextButton deleteAllButton = new PlainTextButton(width - textWidth1 - 5, buttonY, textWidth1, font.lineHeight + 1, deleteAllMessage, button -> {
            minecraft.setScreen(new ConfirmScreen(b -> {
                if (b) {
                    ClientUtils.requestRemoveRecord(null, includeOffline);
                }
                minecraft.setScreen(this);
            }, Component.translatable("screen.injuryrecord.confirm.title.all"), Component.translatable("screen.injuryrecord.confirm.message")));
        }, font);
        addRenderableWidget(deleteAllButton);

        MutableComponent deleteSelectedMessage = Component.translatable("screen.injuryrecord.delete_selected");
        int textWidth2 = font.width(deleteSelectedMessage);
        PlainTextButton deleteSelectedButton = new PlainTextButton(deleteAllButton.x - textWidth2 - 5, buttonY, textWidth2, font.lineHeight + 1, deleteSelectedMessage, button -> {
            if (selectedRecord != null) {
                minecraft.setScreen(new ConfirmScreen(b -> {
                    if (b) {
                        ClientUtils.requestRemoveRecord(selectedRecord.getUuid(), includeOffline);
                    }
                    minecraft.setScreen(this);
                }, Component.translatable("screen.injuryrecord.confirm.title.selected", selectedRecord.getName()), Component.translatable("screen.injuryrecord.confirm.message")));
            }
        }, font);
        addRenderableWidget(deleteSelectedButton);

        if (permission != null && permission) {
            LocalPlayer player = minecraft.player;
            if (player == null || !player.hasPermissions(2)) {
                deleteAllButton.active = false;
                deleteSelectedButton.active = false;
            }
        }

        if (!includeOffline) {
            includeOffline = damageRecordList.containsOffline();
        }
        if (!includeOffline) {
            MutableComponent message2 = Component.translatable("screen.injuryrecord.get_offline_data");
            int textWidth3 = font.width(message2);
            PlainTextButton requestOfflineButton = new PlainTextButton(width - textWidth3 - 5, 6, textWidth3, font.lineHeight, message2, button -> {
                includeOffline = true;
                synced = false;
                ClientUtils.requestRecords(true);
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

    public void updateData(UUID uuid, DamageData data) {
        if (this.selectedRecord != null && this.selectedRecord.getUuid().equals(uuid)) {
            this.damageDataInfo.addData(data);
        } else {
            this.damageRecordList.addInjury(uuid, data);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        ClientDamageRecordsCache.INSTANCE.clearRecords();
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

            if (focusedEntry != null) {
                damageRecordList.setFocused(focusedEntry);
                damageRecordList.ensureVisible(focusedEntry);
            } else if (selectedRecord == null) {
                focusFirst();
            }
        }

        public void addInjury(UUID uuid, DamageData data) {
            children().stream().filter(entry -> entry.record.getUuid().equals(uuid)).findFirst().ifPresent(entry -> entry.addInjury(data));
        }

        public boolean containsOffline() {
            return children().stream().anyMatch(entry -> ClientUtils.isOffline(entry.record.getUuid()));
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
                    return true;
                }
                return super.mouseClicked(pMouseX, pMouseY, pButton);
            }

            public void addInjury(DamageData data) {
                record.addInjury(data);
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

        public void addData(DamageData data) {
            boolean onBottom = this.getScrollAmount() >= this.getMaxScroll();
            Entry entry = new Entry(data);
            addEntry(entry);
            if (onBottom) {
                ensureVisible(entry);
            }
        }

        public class Entry extends ObjectSelectionList.Entry<DamageDataInfo.Entry> {

            private final DamageData data;
            private boolean hasTooltip;
            private int nameEndX = -1;
            private int locationStartX = -1;

            public Entry(DamageData data) {
                this.data = data;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(@NotNull PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                hasTooltip = false;
                nameEndX = -1;
                renderData(pPoseStack, pMouseX, pMouseY, pLeft, pTop, pWidth, pHeight, data, pIsMouseOver);
            }

            public void renderData(PoseStack poseStack, int mouseX, int mouseY, int x, int y, int width, int height, DamageData data, boolean isMouseOver) {
                int startY = y + (height / 2 - font.lineHeight) / 2 + 1;
                int startY2 = y + height - font.lineHeight - 1;
                boolean dead = data.isDead();
                int color = dead ? 0xFF0000 : 0xFFFFFF;

                renderLocation(poseStack, x + width, startY, data, color);
                renderName(poseStack, x, startY, mouseX, mouseY, data, color);
                renderDamage(poseStack, x, startY2, dead, data, color);
                renderTime(poseStack, x + width, startY2, data, color);

                renderItems(poseStack, x + width / 2 - 36, y + (height - 16) / 2, mouseX, mouseY, data);

                if (dead && !hasTooltip) {
                    Component deathMessage = data.getDeathMessage();
                    if (deathMessage != null && isMouseOver) {
                        renderTooltip(poseStack, font.split(deathMessage, DamageRecordListScreen.this.width / 3), mouseX, mouseY);
                    }
                }
            }

            private void renderTime(PoseStack poseStack, int right, int y, DamageData data, int color) {
                String time = data.getFormattedTime();
                font.draw(poseStack, time, right - 4 - font.width(time), y, color);
            }

            private void renderDamage(PoseStack poseStack, int x, int y, boolean dead, DamageData data, int color) {
                float amount = data.getAmount();
                String amountString = Float.compare(amount, Float.MAX_VALUE) >= 0 ? "∞" : String.format("%.1f", amount);
                if (amountString.endsWith(".0")) {
                    amountString = amountString.substring(0, amountString.length() - 2);
                }
                MutableComponent damageText = dead ? Component.translatable("screen.injuryrecord.dead") : Component.translatable("screen.injuryrecord.damage", amountString);
                font.draw(poseStack, damageText, x + 4, y, color);
            }

            private void renderLocation(PoseStack poseStack, int right, int y, DamageData data, int color) {
                GlobalPos location = data.getLocation();
                ResourceLocation dimensionId = location.dimension().location();
                BlockPos pos = location.pos();
                String dimensionKey = "dimension." + dimensionId.toLanguageKey();
                Component locationText = Component.translatable("screen.injuryrecord.location", I18n.exists(dimensionKey) ? I18n.get(dimensionKey) : dimensionId.toString(), pos.getX(), pos.getY(), pos.getZ());

                int x = right - 4 - font.width(locationText);
                font.draw(poseStack, locationText, x, y, color);

                locationStartX = x;
            }

            private void renderName(PoseStack poseStack, int x, int y, int mouseX, int mouseY, DamageData data, int color) {
                Component name = data.getDirectEntityName();
                if (name == null) {
                    name = Component.literal(data.getMsgId());
                } else if (data.isRemote()) {
                    Component attackerName = data.getAttackerName();
                    if (attackerName != null) {
                        name = name.copy().append(" (").append(attackerName).append(")");
                    }
                }
                MutableComponent text = Component.translatable("screen.injuryrecord.source", name);

                nameEndX = x + 4 + font.width(text);

                if (locationStartX == -1 || nameEndX < locationStartX - 4) {
                    font.draw(poseStack, text, x + 4, y, color);
                } else {
                    String string = font.substrByWidth(text, locationStartX - x - 4 - 4).getString();
                    String finalString = string.substring(0, string.length() - 3) + "...";
                    font.draw(poseStack, finalString, x + 4, y, color);
                    if (mouseX >= x + 4 && mouseX <= x + 4 + font.width(finalString) && mouseY >= y && mouseY < y + font.lineHeight) {
                        renderTooltip(poseStack, font.split(name, DamageRecordListScreen.this.width / 3), mouseX, mouseY);
                        hasTooltip = true;
                    }
                }
            }

            private void renderItems(PoseStack poseStack, int x, int y, int mouseX, int mouseY, DamageData data) {
                if (nameEndX != -1 && x < nameEndX) {
                    y += font.lineHeight - 1;
                }

                if (renderItem(poseStack, x, y, mouseX, mouseY, data.getAttackerMainHandItem())) {
                    x += 18;
                }
                if (renderItem(poseStack, x, y, mouseX, mouseY, data.getAttackerOffHandItem())) {
                    x += 18;
                }

                if (!data.isRemote()) return;

                if (renderItem(poseStack, x, y, mouseX, mouseY, data.getDirectEntityMainHandItem())) {
                    x += 18;
                }
                renderItem(poseStack, x, y, mouseX, mouseY, data.getDirectEntityOffHandItem());
            }

            private boolean renderItem(PoseStack poseStack, int x, int y, int mouseX, int mouseY, ItemStack stack) {
                if (stack == null || stack.isEmpty()) {
                    return false;
                }

                itemRenderer.renderAndDecorateItem(stack, x, y);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16 && !hasTooltip) {
                    renderTooltip(poseStack, stack, mouseX, mouseY);
                    hasTooltip = true;
                }
                return true;
            }
        }
    }
}
