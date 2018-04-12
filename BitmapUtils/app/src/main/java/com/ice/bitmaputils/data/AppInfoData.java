package com.ice.bitmaputils.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by minwalker on 2018/3/10.
 */

public class AppInfoData implements Serializable{
    private String name;
    private String url;
    private String descri;
    private String version;
    private String app_url;
    private String pk_name;
    private String app_size;
    private float app_rate;
    private int app_type;
    private ArrayList<String> img_list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescri() {
        return descri;
    }

    public void setDescri(String descri) {
        this.descri = descri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppUrl() {
        return app_url;
    }

    public void setAppUrl(String app_url) {
        this.app_url = app_url;
    }

    public String getPackageName() {
        return pk_name;
    }

    public void setPackageName(String pk_name) {
        this.pk_name = pk_name;
    }

    public void setAppSize(String app_size) {
        this.app_size = app_size;
    }

    public String getAppSize() {
        return app_size;
    }

    public void setAppRate(float app_rate){
        this.app_rate = app_rate;
    }

    public int getAppTyep() {
        return app_type;
    }

    public void setAppType(int app_type){
        this.app_type = app_type;
    }

    public float getAppRate(){
        return app_rate;
    }

    public void setImageList(ArrayList<String> img_list) {
        this.img_list = img_list;
    }

    public ArrayList<String> getImageList() {
        return img_list;
    }
}
