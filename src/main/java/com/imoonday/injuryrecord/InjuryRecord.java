package com.imoonday.injuryrecord;

import com.imoonday.injuryrecord.item.HistoricalDamageViewerItem;
import com.imoonday.injuryrecord.network.Network;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("removal")
@Mod(InjuryRecord.MODID)
public class InjuryRecord {

    public static final String MODID = "injuryrecord";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> HISTORICAL_DAMAGE_VIEWER = ITEMS.register("historical_damage_viewer", () -> new HistoricalDamageViewerItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));

    public InjuryRecord() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        Network.register();
    }
}
