package com.winsun.fruitmix.mediaModule.model;

/**
 * Created by Administrator on 2017/5/5.
 */

public class Address {

    private String country;
    private String province;
    private String city;
    private String district;

    public Address(String country, String province, String city, String district) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
    }

    public String getCountry() {
        return country == null ? "" : country;
    }

    public String getProvince() {
        return province == null ? "" : province;
    }

    public String getCity() {
        return city == null ? "" : city;
    }

    public String getDistrict() {
        return district == null ? "" : district;
    }

    @Override
    public String toString() {
        return country + province + city + district;
    }
}
