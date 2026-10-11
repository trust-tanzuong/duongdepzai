package com.example.myapplication;

public class PhoneModel {
    private String name;
    private int imageResId;
    private boolean isChecked;

    public PhoneModel(String name, int imageResId, boolean isChecked) {
        this.name = name;
        this.imageResId = imageResId;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}