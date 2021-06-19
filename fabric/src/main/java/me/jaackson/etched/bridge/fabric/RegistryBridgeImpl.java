package me.jaackson.etched.bridge.fabric;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.jaackson.etched.Etched;
import me.jaackson.etched.bridge.RegistryBridge;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Jackson
 */
public class RegistryBridgeImpl {

    public static <T extends SoundEvent> Supplier<T> registerSound(String name, Supplier<T> object) {
        T register = Registry.register(Registry.SOUND_EVENT, new ResourceLocation(Etched.MOD_ID, name), object.get());
        return () -> register;
    }

    public static <T extends Item> Supplier<T> registerItem(String name, Supplier<T> object) {
        T register = Registry.register(Registry.ITEM, new ResourceLocation(Etched.MOD_ID, name), object.get());
        return () -> register;
    }

    public static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> object) {
        T register = Registry.register(Registry.BLOCK, new ResourceLocation(Etched.MOD_ID, name), object.get());
        return () -> register;
    }

    public static <B extends BlockEntity, T extends BlockEntityType.Builder<B>> Supplier<BlockEntityType<B>> registerBlockEntity(String name, Supplier<T> object) {
        BlockEntityType<B> register = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(Etched.MOD_ID, name), object.get().build(null));
        return () -> register;
    }

    public static <E extends Entity, T extends EntityType.Builder<E>> Supplier<EntityType<E>> registerEntity(String name, Supplier<T> object) {
        EntityType<E> register = Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(Etched.MOD_ID, name), object.get().build(Etched.MOD_ID + ":" + name));
        return () -> register;
    }

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, RegistryBridge.MenuFactory<T> object) {
        MenuType<T> register = ScreenHandlerRegistry.registerSimple(new ResourceLocation(Etched.MOD_ID, name), object::create);
        return () -> register;
    }

    public static Supplier<VillagerProfession> registerProfession(String name, Supplier<PoiType> poiType, @Nullable Supplier<SoundEvent> workSound) {
        VillagerProfession register = Registry.register(Registry.VILLAGER_PROFESSION, new ResourceLocation(Etched.MOD_ID, name),
                VillagerProfessionBuilder.create()
                        .id(new ResourceLocation(Etched.MOD_ID, name))
                        .workstation(poiType.get())
                        .workSound(workSound != null ? workSound.get() : null)
                        .build());
        return () -> register;
    }

    public static Supplier<PoiType> registerPOI(String name, Supplier<Block> block, int maxTickets, int validRange) {
        PoiType register = PointOfInterestHelper.register(new ResourceLocation(Etched.MOD_ID, name), maxTickets, validRange, ImmutableSet.copyOf(block.get().getStateDefinition().getPossibleStates()));
        return () -> register;
    }

    public static void registerVillagerTrades(Supplier<VillagerProfession> prof, Supplier<Int2ObjectMap<VillagerTrades.ItemListing[]>> listings) {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> VillagerTrades.TRADES.put(prof.get(), listings.get()));
    }

    @SafeVarargs
    @Environment(EnvType.CLIENT)
    public static void registerItemColor(ItemColor color, Supplier<Item>... items) {
        for (Supplier<Item> item : items) {
            ColorProviderRegistry.ITEM.register(color, item.get());
        }
    }

    @SuppressWarnings("deprecation")
    @Environment(EnvType.CLIENT)
    public static void registerItemOverride(Item item, ResourceLocation resourceLocation, ItemPropertyFunction itemPropertyFunction) {
        FabricModelPredicateProviderRegistry.register(item, resourceLocation, new ClampedItemPropertyFunction() {
            @Override
            public float call(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
                return itemPropertyFunction.call(itemStack, clientLevel, livingEntity, i);
            }

            @Override
            public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
                return 0;
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static void registerBlockRenderType(Block block, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    @Environment(EnvType.CLIENT)
    public static <T extends Entity> void registerEntityRenderer(EntityType<T> entityType, EntityRendererProvider<T> factory) {
        EntityRendererRegistry.INSTANCE.register(entityType, factory);
    }

    @Environment(EnvType.CLIENT)
    public static void registerSprite(ResourceLocation sprite, ResourceLocation atlas) {
        ClientSpriteRegistryCallback.event(atlas).register((atlasTexture, registry) -> registry.register(sprite));
    }

    @Environment(EnvType.CLIENT)
    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void registerScreenFactory(MenuType<M> type, RegistryBridge.ScreenFactory<M, S> object) {
        ScreenRegistry.register(type, object::create);
    }
}
