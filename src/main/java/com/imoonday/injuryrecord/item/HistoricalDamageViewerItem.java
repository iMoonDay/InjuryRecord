package com.imoonday.injuryrecord.item;

import com.imoonday.injuryrecord.client.ClientUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HistoricalDamageViewerItem extends Item {

    public HistoricalDamageViewerItem(Properties pProperties) {
        super(pProperties);
    }

    /**
     * 右键时打开UI并请求数据
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        if (pLevel.isClientSide) {
            ClientUtils.openDamageRecordList();
            ClientUtils.requestRecords(false);
        }
        return InteractionResultHolder.consume(pPlayer.getItemInHand(pUsedHand));
    }
}
