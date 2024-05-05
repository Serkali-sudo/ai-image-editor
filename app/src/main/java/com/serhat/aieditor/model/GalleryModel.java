package com.serhat.aieditor.model;

import com.serhat.aieditor.Utils;

import java.io.File;

public class GalleryModel {

    public String prompt;
    public String negative_prompt;
    public int width;
    public int height;
    public String path;
    public String originalPath;
    public String seed;
    public long id;
    public String addedDate;
    public boolean isSelected = false;

    public GalleryModel() {

    }


    public GalleryModel(long id, String prompt, String negative_prompt,
                        int width, int height, String path, String originalPath, String seed, String addedDate) {
        this.id = id;
        this.prompt = prompt;
        this.negative_prompt = negative_prompt;
        this.width = width;
        this.height = height;
        this.path = path;
        this.originalPath = originalPath;
        this.seed = seed;
        this.addedDate = addedDate;
    }

    public void deleteFromDisk() {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Utils.e("GalleryModel", e.getMessage());
        }
    }

}
