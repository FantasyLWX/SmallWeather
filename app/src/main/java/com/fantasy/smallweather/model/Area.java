package com.fantasy.smallweather.model;

/**
 * @author Fantasy
 * @version 1.0, 2016/8/25.
 */
public class Area {
    private int id;
    private String areaName;
    private String areaCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}
