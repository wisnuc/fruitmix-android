package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.mediaModule.model.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/5.
 */

public class RemoteMediaAddressParser implements RemoteDatasParser<Address> {

    @Override
    public List<Address> parse(String json) throws JSONException {

        List<Address> addresses = new ArrayList<>();

        JSONObject rootObject = new JSONObject(json);

        if (rootObject.has("regeocodes")) {

            JSONArray regeocodes = rootObject.getJSONArray("regeocodes");

            for (int i = 0; i < regeocodes.length(); i++) {
                Address address = parseRegeocode(regeocodes.getJSONObject(i));

                addresses.add(address);
            }

        } else {

            JSONObject regeocode = rootObject.getJSONObject("regeocode");

            Address address = parseRegeocode(regeocode);

            addresses.add(address);
        }

        return addresses;
    }

    private Address parseRegeocode(JSONObject regeocode) throws JSONException {

        JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

        String country = addressComponent.getString("country");
        String province = addressComponent.getString("province");
        Object cityObject = addressComponent.get("city");

        String city = "";
        if (cityObject instanceof String) {
            city = (String) cityObject;
        }

        String district = addressComponent.getString("district");

        return new Address(country, province, city, district);
    }

}
