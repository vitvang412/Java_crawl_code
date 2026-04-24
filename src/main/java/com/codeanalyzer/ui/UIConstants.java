package com.codeanalyzer.ui;

import java.awt.*;

public final class UIConstants {
    private UIConstants() {}

    // -- Cam nhat chu dao --
    public static final Color PRIMARY       = new Color(0xE8874A);
    public static final Color PRIMARY_DARK  = new Color(0xC96B30);
    public static final Color PRIMARY_LIGHT = new Color(0xF5C4A1);
    public static final Color ACCENT        = new Color(0x4A90A4);
    public static final Color ACCENT_DARK   = new Color(0x377080);

    // -- Trang thai --
    public static final Color SUCCESS       = new Color(0x4A8C5C);
    public static final Color WARNING       = new Color(0xD4922A);
    public static final Color DANGER        = new Color(0xC44444);

    // -- Nen / text --
    public static final Color BACKGROUND    = new Color(0xFDF6F0);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TEXT          = new Color(0x3D2C1E);
    public static final Color TEXT_MUTED    = new Color(0x8B7B6E);
    public static final Color BORDER        = new Color(0xE8DDD4);

    // -- Log area --
    public static final Color CONSOLE_BG    = new Color(0x2C1E14);
    public static final Color CONSOLE_FG    = new Color(0xF0D9C6);

    // -- Fonts --
    public static final Font HEADER     = new Font("SansSerif", Font.BOLD,  18);
    public static final Font SUBHEADER  = new Font("SansSerif", Font.BOLD,  13);
    public static final Font MAIN       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font SMALL      = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font MONO       = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    // -- Legacy aliases --
    public static final Color PRIMARY_COLOR    = PRIMARY;
    public static final Color ACCENT_COLOR     = ACCENT;
    public static final Color BACKGROUND_COLOR = BACKGROUND;
    public static final Color TEXT_COLOR       = TEXT;
    public static final Font  MAIN_FONT        = MAIN;
    public static final Font  HEADER_FONT      = HEADER;

    // -- Kich thuoc --
    public static final int PADDING      = 12;
    public static final int BIG_PADDING  = 20;
}
