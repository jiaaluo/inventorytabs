package com.kqp.inventorytabs.mixin.client;

import com.kqp.inventorytabs.interf.TabManagerContainer;
import com.kqp.inventorytabs.tabs.TabManager;
import com.kqp.inventorytabs.tabs.render.TabRenderingHints;
import com.kqp.inventorytabs.tabs.tab.SimpleBlockTab;
import com.kqp.inventorytabs.tabs.tab.Tab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public class VanillaScreenTabAdder implements TabRenderingHints {
    @Inject(
            method = "init",
            at = @At("RETURN")
    )
    private void initTabRenderer(CallbackInfo callbackInfo) {
        if (screenSupported()) {
            MinecraftClient client = MinecraftClient.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            tabManager.onScreenOpen((HandledScreen) (Object) this);

            Tab tabOpened = null;

            if ((Object) this instanceof InventoryScreen) {
                tabOpened = tabManager.tabs.get(0);
            } else if (!tabManager.screenOpenedViaTab()) {
                // If the screen was NOT opened via tab,
                // check what block player is looking at for context

                if (client.crosshairTarget instanceof BlockHitResult) {
                    BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                    BlockPos blockPos = blockHitResult.getBlockPos();

                    for (int i = 0; i < tabManager.tabs.size(); i++) {
                        Tab tab = tabManager.tabs.get(i);

                        if (tab instanceof SimpleBlockTab) {
                            if (((SimpleBlockTab) tab).blockPos.equals(blockPos)) {
                                tabOpened = tab;
                            }
                        }
                    }
                }
            }

            if (tabOpened != null) {
                tabManager.onOpenTab(tabOpened);
            }
        }
    }

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    protected void drawBackgroundTabs(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callbackInfo) {
        if (screenSupported()) {
            if (!screenDoesDumbBlock()) {
                MinecraftClient client = MinecraftClient.getInstance();
                TabManager tabManager = ((TabManagerContainer) client).getTabManager();

                tabManager.tabRenderer.renderBackground(matrices);
            }
        }
    }

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    protected void drawForegroundTabs(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo callbackInfo) {
        if (screenSupported()) {
            MinecraftClient client = MinecraftClient.getInstance();
            TabManager tabManager = ((TabManagerContainer) client).getTabManager();

            tabManager.tabRenderer.renderForeground(matrices, mouseX, mouseY);
            tabManager.tabRenderer.renderHoverTooltips(matrices, mouseX, mouseY);
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (screenSupported()) {
            TabManager tabManager = ((TabManagerContainer) MinecraftClient.getInstance()).getTabManager();

            if (tabManager.mouseClicked(mouseX, mouseY, button)) {
                callbackInfo.setReturnValue(true);
            }
        }
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true
    )
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (screenSupported()) {
            TabManager tabManager = ((TabManagerContainer) MinecraftClient.getInstance()).getTabManager();

            if (tabManager.keyPressed(keyCode, scanCode, modifiers)) {
                callbackInfo.setReturnValue(true);
            }
        }
    }

    @Override
    public int getTopRowXOffset() {
        HandledScreen screen = (HandledScreen) (Object) this;
        if (screen instanceof InventoryScreen) {
            if (((InventoryScreen) screen).getRecipeBookWidget().isOpen()) {
                return 77;
            }
        } else if (screen instanceof AbstractFurnaceScreen) {
            if (((AbstractFurnaceScreen) screen).recipeBook.isOpen()) {
                return 77;
            }
        } else if (screen instanceof CraftingScreen) {
            if (((CraftingScreen) screen).getRecipeBookWidget().isOpen()) {
                return 77;
            }
        }

        return 0;
    }

    @Override
    public int getBottomRowXOffset() {
        return getTopRowXOffset();
    }

    @Override
    public int getBottomRowYOffset() {
        return screenNeedsOffset() ? -1 : 0;
    }

    private boolean screenSupported() {
        HandledScreen screen = (HandledScreen) (Object) this;

        return screen instanceof GenericContainerScreen
                || screen instanceof InventoryScreen
                || screen instanceof AbstractFurnaceScreen
                || screen instanceof AnvilScreen
                || screen instanceof CraftingScreen
                || screen instanceof ShulkerBoxScreen
                || screen instanceof EnchantmentScreen
                || screen instanceof BrewingStandScreen
                || screen instanceof SmithingScreen
                || screen instanceof CartographyTableScreen
                || screen instanceof LoomScreen
                || screen instanceof StonecutterScreen
                || screen instanceof GrindstoneScreen;
    }

    private boolean screenDoesDumbBlock() {
        HandledScreen screen = (HandledScreen) (Object) this;

        return screen instanceof CartographyTableScreen
                || screen instanceof LoomScreen
                || screen instanceof StonecutterScreen;
    }

    private boolean screenNeedsOffset() {
        HandledScreen screen = (HandledScreen) (Object) this;

        return screen instanceof ShulkerBoxScreen
                || screen instanceof GenericContainerScreen;
    }
}