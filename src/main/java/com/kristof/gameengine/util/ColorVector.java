package com.kristof.gameengine.util;

public class ColorVector {
    private final float[] coords;

    public static final ColorVector NULL_COLOR = new ColorVector();
    public static final ColorVector WHITE = new ColorVector(1, 1, 1, 1);
    public static final ColorVector BLACK = new ColorVector(0, 0, 0, 1);
    public static final ColorVector RED = new ColorVector(1, 0, 0, 1);
    public static final ColorVector GREEN = new ColorVector(0, 1, 0, 1);
    public static final ColorVector BLUE = new ColorVector(0, 0, 1, 1);
    public static final ColorVector CYAN = new ColorVector(0, 1, 1, 1);
    public static final ColorVector CYAN_BLUE = new ColorVector(0, 0.7f, 1, 1);
    public static final ColorVector MAGENTA = new ColorVector(1, 0, 1, 1);
    public static final ColorVector PURPLE = new ColorVector(0.5f, 0, 1, 1);
    public static final ColorVector YELLOW = new ColorVector(1, 1, 0, 1);
    public static final ColorVector ORANGE = new ColorVector(1, (float) (1. / 3.), 0, 1);
    public static final ColorVector GOLD = new ColorVector(1, 0.843f, 0, 1);    // (255, 215, 0)
    public static final float TRANS_ALPHA = 0.4f;
    public static final ColorVector TRANS_WHITE = new ColorVector(1, 1, 1, TRANS_ALPHA);
    public static final ColorVector TRANS_BLACK = new ColorVector(0, 0, 0, TRANS_ALPHA);
    public static final ColorVector TRANS_RED = new ColorVector(1, 0, 0, TRANS_ALPHA);
    public static final ColorVector TRANS_GREEN = new ColorVector(0, 1, 0, TRANS_ALPHA);
    public static final ColorVector TRANS_BLUE = new ColorVector(0, 0, 1, TRANS_ALPHA);
    public static final ColorVector TRANS_LIGHT_BLUE = new ColorVector(0.2f, 0.5f, 1f, TRANS_ALPHA);
    public static final ColorVector TRANS_CYAN = new ColorVector(0, 1, 1, TRANS_ALPHA);
    public static final ColorVector TRANS_MAGENTA = new ColorVector(1, 0, 1, TRANS_ALPHA);
    public static final ColorVector TRANS_YELLOW = new ColorVector(1, 1, 0, TRANS_ALPHA);
    public static final ColorVector TRANS_ORANGE = new ColorVector(1, (float) (1. / 3.), 0, TRANS_ALPHA);
    public static final ColorVector TRANS_GOLD = new ColorVector(1, 0.843f, 0, TRANS_ALPHA);    // (255, 215, 0)
    public static final ColorVector TRANS_LIGHT_GOLD = new ColorVector(1, 0.92157f, 0, TRANS_ALPHA);    // (255, 235, 0)
    public static final ColorVector ANDROID_GREEN = new ColorVector(0.63671875f, 0.76953125f, 0.22265625f, 1.0f);
    public static final ColorVector NEXUS_BLUE = new ColorVector(0.2f, 0.709803922f, 0.898039216f, 1.0f);

    public ColorVector() {
        coords = new float[4];
        coords[0] = coords[1] = coords[2] = coords[3] = 0;
    }

    public ColorVector(float r, float g, float b, float a) {
        coords = new float[4];
        coords[0] = r;
        coords[1] = g;
        coords[2] = b;
        coords[3] = a;
    }

    public ColorVector(float r, float g, float b) {
        coords = new float[4];
        coords[0] = r;
        coords[1] = g;
        coords[2] = b;
        coords[3] = 1f;
    }

    public ColorVector(float[] coords) {
        this.coords = new float[4];
        System.arraycopy(coords, 0, this.coords, 0, 4);
    }

    public ColorVector(ColorVector cvr) {
        coords = new float[4];
        for (int i = 0; i < 4; i++) {
            coords[i] = cvr.getCoord(i);
        }
    }

    public ColorVector(ColorVector cvr, float alpha) {
        coords = new float[4];
        for (int i = 0; i < 3; i++) {
            coords[i] = cvr.getCoord(i);
        }
        coords[3] = alpha;
    }

    public ColorVector getCopy() {
        return (new ColorVector(this));
    }

    public float getR() {
        return coords[0];
    }

    public float getG() {
        return coords[1];
    }

    public float getB() {
        return coords[2];
    }

    public float getA() {
        return coords[3];
    }

    public float getCoord(int dimIndex) {
        return switch (dimIndex) {
            case 0 -> coords[0];
            case 1 -> coords[1];
            case 2 -> coords[2];
            case 3 -> coords[3];
            default -> -1;
        };
    }

    public float[] getArray() {
        final float[] ret = new float[4];
        System.arraycopy(coords, 0, ret, 0, 4);
        return ret;
    }

    public ColorVector getWithIntensity(float intensity) {
        if (intensity > 0 && intensity < 1f) {
            ColorVector rvr = new ColorVector();
            for (int i = 0; i < coords.length; i++) {
                rvr.setCoord(i, coords[i] * intensity);
            }
            return rvr;
        } else {
            return new ColorVector(this);
        }
    }

    public ColorVector getRounded(int decimals) {
        final ColorVector rvr = new ColorVector();
        final float rd = (float) Math.pow(10, decimals);
        for (int i = 0; i < coords.length; i++) {
            rvr.setCoord(i, Math.round(coords[i] * rd) / rd);
        }
        return rvr;
    }

    public void setR(float r) {
        coords[0] = r;
    }

    public void setG(float g) {
        coords[1] = g;
    }

    public void setB(float b) {
        coords[2] = b;
    }

    public void setA(float a) {
        coords[3] = a;
    }

    public void setCoord(int i, float c) {
        if (i < 4 && i >= 0) coords[i] = c;
    }

    public void normalizeRGB() {
        final Vector3fExt sClr = new Vector3fExt(coords[0], coords[1], coords[2]);
        sClr.normalize();
        coords[0] = sClr.getX();
        coords[1] = sClr.getY();
        coords[2] = sClr.getZ();
    }

    public static ColorVector randomColor() {
        ColorVector rClr = new ColorVector((float) Math.random(), (float) Math.random(), (float) Math.random(), 1f);
        rClr.normalizeRGB();
        return rClr;
    }

    public String toString() {
        return "(" + coords[0] + "," + coords[1] + "," + coords[2] + "," + coords[2] + ")";
    }

    public void multiplyRgbIntensity(float sc) {
        for (int i = 0; i < 3; i++) {
            coords[i] *= sc;
        }
    }
}
