package com.mualab.org.user.data.model;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dharmraj on 28/2/18.
 */

public class MediaUri implements Serializable{
    public String uri;
    public int mediaType;
    public boolean isFromGallery;
    public List<String> uriList = new ArrayList<>();

    public void addUri(String uri){
        /*if(uriList==null)
            uriList = new ArrayList<>();*/
        uriList.add(uri);
        this.uri = uri;
    }

    public void addUri(List<String> uriList){
        if(uriList!=null){
            this.uriList.addAll(uriList);
            this.uri = uriList.get(uriList.size()-1);
        }
    }

    public void addAll(HashMap<String, Uri> map){
        if(uriList!=null){
            this.uriList.addAll(map.keySet());
            this.uri = uriList.get(map.size()-1);
        }
    }

}
