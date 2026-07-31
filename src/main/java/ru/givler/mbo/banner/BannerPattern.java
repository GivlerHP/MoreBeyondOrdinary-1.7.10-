package ru.givler.mbo.banner;

public enum BannerPattern {
    BASE("base", "b"),
    SQUARE_BOTTOM_LEFT("square_bottom_left", "bl"),
    SQUARE_BOTTOM_RIGHT("square_bottom_right", "br"),
    SQUARE_TOP_LEFT("square_top_left", "tl"),
    SQUARE_TOP_RIGHT("square_top_right", "tr"),
    STRIPE_BOTTOM("stripe_bottom", "bs"),
    STRIPE_TOP("stripe_top", "ts"),
    STRIPE_LEFT("stripe_left", "ls"),
    STRIPE_RIGHT("stripe_right", "rs"),
    STRIPE_CENTER("stripe_center", "cs"),
    STRIPE_MIDDLE("stripe_middle", "ms"),
    STRIPE_DOWNRIGHT("stripe_downright", "drs"),
    STRIPE_DOWNLEFT("stripe_downleft", "dls"),
    STRIPE_SMALL("small_stripes", "ss"),
    CROSS("cross", "cr"),
    STRAIGHT_CROSS("straight_cross", "sc"),
    TRIANGLE_BOTTOM("triangle_bottom", "bt"),
    TRIANGLE_TOP("triangle_top", "tt"),
    TRIANGLES_BOTTOM("triangles_bottom", "bts"),
    TRIANGLES_TOP("triangles_top", "tts"),
    DIAGONAL_LEFT("diagonal_left", "ld"),
    DIAGONAL_RIGHT("diagonal_up_right", "rd"),
    DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud"),
    DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud"),
    CIRCLE("circle", "mc"),
    RHOMBUS("rhombus", "mr"),
    HALF_VERTICAL("half_vertical", "vh"),
    HALF_HORIZONTAL("half_horizontal", "hh"),
    HALF_VERTICAL_RIGHT("half_vertical_right", "vhr"),
    HALF_HORIZONTAL_BOTTOM("half_horizontal_bottom", "hhb"),
    BORDER("border", "bo"),
    CURLY_BORDER("curly_border", "cbo"),
    CREEPER("creeper", "cre"),
    GRADIENT("gradient", "gra"),
    GRADIENT_UP("gradient_up", "gru"),
    BRICKS("bricks", "bri"),
    SKULL("skull", "sku"),
    FLOWER("flower", "flo"),
    MOJANG("mojang", "moj");

    public final String texture;
    public final String id;

    BannerPattern(String texture, String id) {
        this.texture = texture;
        this.id = id;
    }

    public static BannerPattern byId(String id) {
        for (BannerPattern pattern : values()) if (pattern.id.equals(id)) return pattern;
        return null;
    }
}
