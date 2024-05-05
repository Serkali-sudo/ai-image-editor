package com.serhat.aieditor.drawing;

import android.graphics.Path;

public class Stroke {

    public int colour;
    public float strokeWidth;
    public Path path;

    public Stroke(int colour, float width, Path path) {
        this.colour = colour;
        this.strokeWidth = width;
        this.path = path;
    }

}