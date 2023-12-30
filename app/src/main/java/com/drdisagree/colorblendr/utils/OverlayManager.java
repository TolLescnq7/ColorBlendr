package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_SYSTEM;
import static com.drdisagree.colorblendr.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.content.Context;
import android.graphics.Color;
import android.os.RemoteException;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.service.IRootService;
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource;
import com.drdisagree.colorblendr.utils.monet.scheme.DynamicScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OverlayManager {

    private static final String TAG = OverlayManager.class.getSimpleName();
    private static final IRootService mRootService = ColorBlendr.getRootService();
    private static final String[][] colorNames = ColorUtil.getColorNames();

    public static void enableOverlay(String packageName) {
        try {
            mRootService.enableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void disableOverlay(String packageName) {
        try {
            mRootService.disableOverlay(Collections.singletonList(packageName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isOverlayInstalled(String packageName) {
        try {
            return mRootService.isOverlayInstalled(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isOverlayEnabled(String packageName) {
        try {
            return mRootService.isOverlayEnabled(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void uninstallOverlayUpdates(String packageName) {
        try {
            mRootService.uninstallOverlayUpdates(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void registerFabricatedOverlay(FabricatedOverlayResource fabricatedOverlay) {
        try {
            mRootService.registerFabricatedOverlay(fabricatedOverlay);
            mRootService.enableOverlayWithIdentifier(Collections.singletonList(fabricatedOverlay.overlayName));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void unregisterFabricatedOverlay(String packageName) {
        try {
            mRootService.unregisterFabricatedOverlay(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void applyFabricatedColors(Context context) {
        boolean pitchBlackTheme = RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);

        ArrayList<ArrayList<Integer>> palette = ColorUtil.generateModifiedColors(
                context,
                ColorSchemeUtil.stringToEnumMonetStyle(
                        context,
                        RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
                ),
                RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                pitchBlackTheme,
                RPrefs.getBoolean(MONET_ACCURATE_SHADES, true),
                false
        );

        ArrayList<FabricatedOverlayResource> fabricatedOverlays = new ArrayList<>();
        fabricatedOverlays.add(new FabricatedOverlayResource(
                FABRICATED_OVERLAY_NAME_SYSTEM,
                FRAMEWORK_PACKAGE
        ));

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 13; j++) {
                fabricatedOverlays.get(0).setColor(colorNames[i][j], palette.get(i).get(j));
            }
        }

        ColorSchemeUtil.MONET style = ColorSchemeUtil.stringToEnumMonetStyle(
                context,
                RPrefs.getString(
                        MONET_STYLE,
                        context.getString(R.string.monet_tonalspot)
                )
        );
        int seedColor = RPrefs.getInt(
                MONET_SEED_COLOR,
                WallpaperUtil.getWallpaperColor(context)
        );

        ArrayList<ArrayList<Integer>> mColorSchemeLight = ColorSchemeUtil.generateColorPalette(
                style,
                seedColor,
                false,
                5
        );

        DynamicScheme mDynamicSchemeDark = ColorSchemeUtil.getDynamicScheme(
                style,
                seedColor,
                true,
                5
        );

        DynamicScheme mDynamicSchemeLight = ColorSchemeUtil.getDynamicScheme(
                style,
                seedColor,
                false,
                5
        );

        FabricatedUtil.createDynamicOverlay(
                fabricatedOverlays.get(0),
                mColorSchemeLight,
                mDynamicSchemeDark,
                mDynamicSchemeLight
        );

        HashMap<String, Boolean> selectedApps = Const.getSelectedFabricatedApps();

        for (String packageName : selectedApps.keySet()) {
            if (Boolean.TRUE.equals(selectedApps.get(packageName))) {
                FabricatedOverlayResource fabricatedOverlayPerApp = getFabricatedColorsPerApp(
                        context,
                        packageName,
                        palette
                );

                fabricatedOverlays.add(fabricatedOverlayPerApp);
            }
        }

        if (pitchBlackTheme) {
            palette.get(3).set(11, Color.BLACK);
            palette.get(4).set(11, Color.BLACK);
        }

        for (FabricatedOverlayResource fabricatedOverlay : fabricatedOverlays) {
            registerFabricatedOverlay(fabricatedOverlay);
        }
    }

    public static void applyFabricatedColorsPerApp(Context context, String packageName, ArrayList<ArrayList<Integer>> palette) {
        registerFabricatedOverlay(
                getFabricatedColorsPerApp(
                        context,
                        packageName,
                        palette
                )
        );
    }

    private static FabricatedOverlayResource getFabricatedColorsPerApp(Context context, String packageName, ArrayList<ArrayList<Integer>> palette) {
        if (palette == null) {
            palette = ColorUtil.generateModifiedColors(
                    context,
                    ColorSchemeUtil.stringToEnumMonetStyle(
                            context,
                            RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
                    ),
                    RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                    RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                    RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                    RPrefs.getBoolean(MONET_ACCURATE_SHADES, true),
                    false
            );
        }

        FabricatedOverlayResource fabricatedOverlay = new FabricatedOverlayResource(
                String.format(FABRICATED_OVERLAY_NAME_APPS, packageName),
                packageName
        );

        FabricatedUtil.assignPerAppColorsToOverlay(fabricatedOverlay, palette);

        return fabricatedOverlay;
    }
}