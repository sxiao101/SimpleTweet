package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Entity {
    public String mediaUrl;

    public Entity(){}

    public static Entity fromJson(JSONObject jsonObject) throws JSONException {

        Entity entity = new Entity();
        entity.mediaUrl = jsonObject.getJSONObject("media").getString("media_url_https");
        return entity;
    }
}

