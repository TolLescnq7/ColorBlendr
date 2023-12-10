package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentStylingBinding;
import com.drdisagree.colorblendr.ui.viewmodel.SharedViewModel;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.xposed.modules.utils.ColorModifiers;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView;

public class StylingFragment extends Fragment {

    private FragmentStylingBinding binding;
    private SharedViewModel sharedViewModel;
    private LinearLayout[] colorTableRows;
    private static final int[] colorCodes = {
            0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000
    };
    private static boolean accurateShades = RPrefs.getBoolean(MONET_ACCURATE_SHADES, true);
    private final int[] monetAccentSaturation = new int[]{RPrefs.getInt(MONET_ACCENT_SATURATION, 100)};
    private final int[] monetBackgroundSaturation = new int[]{RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100)};
    private final int[] monetBackgroundLightness = new int[]{RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100)};
    private int[] monetSeedColor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStylingBinding.inflate(inflater, container, false);

        colorTableRows = new LinearLayout[]{
                binding.colorPreview.systemAccent1,
                binding.colorPreview.systemAccent2,
                binding.colorPreview.systemAccent3,
                binding.colorPreview.systemNeutral1,
                binding.colorPreview.systemNeutral2
        };

        monetSeedColor = new int[]{RPrefs.getInt(
                MONET_SEED_COLOR,
                getPrimaryColor()
        )};

        assignStockColorsToPalette();

        // Primary color
        binding.seedColorPicker.setPreviewColor(RPrefs.getInt(
                MONET_SEED_COLOR,
                getPrimaryColor()
        ));
        binding.seedColorPicker.setOnClickListener(v -> new ColorPickerDialog()
                .withCornerRadius(10)
                .withColor(monetSeedColor[0])
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView.class)
                .withListener((pickerView, color) -> {
                    if (monetSeedColor[0] != color) {
                        monetSeedColor[0] = color;
                        binding.seedColorPicker.setPreviewColor(color);
                        RPrefs.putInt(MONET_SEED_COLOR, monetSeedColor[0]);
                    }
                })
                .show(getChildFragmentManager(), "seedColorPicker")
        );
        binding.seedColorPicker.setVisibility(
                RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false) ?
                        View.VISIBLE :
                        View.GONE
        );

        // Monet primary accent saturation
        binding.accentSaturation.setSliderValue(RPrefs.getInt(MONET_ACCENT_SATURATION, 100));

        binding.accentSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetAccentSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.accentSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetAccentSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0]);
            }
        });

        // Long Click Reset
        binding.accentSaturation.setResetClickListener(v -> {
            monetAccentSaturation[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_ACCENT_SATURATION);
            return true;
        });

        // Monet background saturation
        binding.backgroundSaturation.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100));

        binding.backgroundSaturation.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundSaturation[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundSaturation.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundSaturation[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0]);
            }
        });

        // Reset button
        binding.backgroundSaturation.setResetClickListener(v -> {
            monetBackgroundSaturation[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_BACKGROUND_SATURATION);
            return true;
        });

        // Monet background lightness
        binding.backgroundLightness.setSliderValue(RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100));

        binding.backgroundLightness.setOnSliderChangeListener((slider, value, fromUser) -> {
            monetBackgroundLightness[0] = (int) value;
            assignCustomColorsToPalette();
        });

        binding.backgroundLightness.setOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                monetBackgroundLightness[0] = (int) slider.getValue();
                RPrefs.putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0]);
            }
        });

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener(v -> {
            monetBackgroundLightness[0] = 100;
            assignCustomColorsToPalette();
            RPrefs.clearPref(MONET_BACKGROUND_LIGHTNESS);
            return true;
        });

        return binding.getRoot();
    }

    private void assignStockColorsToPalette() {
        int[][] systemColors = ColorUtil.getSystemColors(requireContext());

        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(systemColors[i][j]);
                colorTableRows[i].getChildAt(j).setTag(systemColors[i][j]);

                TextView textView = new TextView(requireContext());
                textView.setText(String.valueOf(colorCodes[j]));
                textView.setRotation(270);
                textView.setTextColor(ColorUtil.calculateTextColor(systemColors[i][j]));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setAlpha(0.8f);
                textView.setMaxLines(1);
                textView.setSingleLine(true);
                textView.setAutoSizeTextTypeUniformWithConfiguration(
                        1,
                        20,
                        1,
                        TypedValue.COMPLEX_UNIT_SP
                );

                ((ViewGroup) colorTableRows[i].getChildAt(j)).addView(textView);
                ((LinearLayout) colorTableRows[i].getChildAt(j)).setGravity(Gravity.CENTER);
            }
        }
    }

    private void assignCustomColorsToPalette() {
        ArrayList<ArrayList<Integer>> palette = convertIntArrayToList(ColorUtil.getSystemColors(requireContext()));

        // Modify colors
        for (int i = 0; i < palette.size(); i++) {
            ArrayList<Integer> modifiedShades = ColorModifiers.modifyColors(
                    new ArrayList<>(palette.get(i).subList(1, palette.get(i).size())),
                    new AtomicInteger(i),
                    monetAccentSaturation[0],
                    monetBackgroundSaturation[0],
                    monetBackgroundLightness[0],
                    RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false)
            );
            for (int j = 1; j < palette.get(i).size(); j++) {
                palette.get(i).set(j, modifiedShades.get(j - 1));
            }
        }

        // Update preview colors
        for (int i = 0; i < colorTableRows.length; i++) {
            for (int j = 0; j < colorTableRows[i].getChildCount(); j++) {
                colorTableRows[i].getChildAt(j).getBackground().setTint(palette.get(i).get(j));
                colorTableRows[i].getChildAt(j).setTag(palette.get(i).get(j));
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel.getVisibilityStates().observe(getViewLifecycleOwner(), this::updateViewVisibility);
    }

    private void updateViewVisibility(Map<String, Integer> visibilityStates) {
        Integer visibility = visibilityStates.get(MONET_SEED_COLOR_ENABLED);
        if (visibility != null && binding.seedColorPicker.getVisibility() != visibility) {
            binding.seedColorPicker.setVisibility(visibility);
            monetSeedColor = new int[]{RPrefs.getInt(
                    MONET_SEED_COLOR,
                    getPrimaryColor()
            )};
            binding.seedColorPicker.setPreviewColor(monetSeedColor[0]);
            RPrefs.clearPref(MONET_SEED_COLOR);
        }
    }

    private static ArrayList<ArrayList<Integer>> convertIntArrayToList(int[][] array) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();

        for (int[] row : array) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (int value : row) {
                rowList.add(value);
            }

            result.add(rowList);
        }

        return result;
    }

    private @ColorInt int getPrimaryColor() {
        return requireContext().getColor(com.google.android.material.R.color.material_dynamic_primary40);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }
}