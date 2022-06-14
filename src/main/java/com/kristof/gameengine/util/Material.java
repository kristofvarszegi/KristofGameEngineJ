package com.kristof.gameengine.util;

public class Material {    // TODO merge mass, color etc. here
    public static final Material BLACK_BODY = new Material(0, 0, 0);
    public static final Material CHROME = new Material(0.4f, 0.774597f, 60f);
    public static final Material SILVER = new Material(0.50754f, 0.508273f, 40f);
    public static final Material PEARL = new Material(0.829f, 0.296648f, 8.8f);
    public static final Material PLASTIC = new Material(0.55f, 0.7f, 25f);
    public static final Material EMISSIVE = new Material(0, 0, 1f, 1f);

    public static final float DEFAULT_MASS_DENSITY = (float) Math.pow(10, 15);

    protected float diffuseFactor;
    protected float specularFactor;
    protected float shininessFactor;
    protected float emissiveFactor;
    protected float massDensity;

    public Material(float diffuse, float specular, float shininess) {
        diffuseFactor = diffuse;
        specularFactor = specular;
        shininessFactor = shininess;
        emissiveFactor = 0;
    }

    public Material(float diffuse, float specular, float shininess, float emissive) {
        diffuseFactor = diffuse;
        specularFactor = specular;
        shininessFactor = shininess;
        emissiveFactor = emissive;
    }

    public Material(Material material) {
        diffuseFactor = material.getDiffuse();
        specularFactor = material.getSpecular();
        shininessFactor = material.getShininess();
        emissiveFactor = material.getEmissive();
        massDensity = material.getMassDensity();
    }

    public float getDiffuse() {
        return diffuseFactor;
    }

    public float getSpecular() {
        return specularFactor;
    }

    public float getShininess() {
        return shininessFactor;
    }

    public float getEmissive() {
        return emissiveFactor;
    }

    public float getMassDensity() {
        return DEFAULT_MASS_DENSITY;
    }

    public void setEmissiveFactor(float emissiveFactor) {
        this.emissiveFactor = emissiveFactor;
    }

    @Override
    public String toString() {
        return ("diff: " + diffuseFactor + ", spec: " + specularFactor + ", shin: " + shininessFactor + ", emis: "
                + emissiveFactor);
    }
}
