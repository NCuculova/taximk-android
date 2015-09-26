package com.ncuculova.taximk;

/**
 * Created by ncuculova on 23.8.15.
 */
public class Taxi {

    private String key;

    private String name;

    private String description;

    private String address;

    private String phone;

    private String phone2;

    private String slug;

    private City city;



    private boolean isFav;

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setIsFav(boolean isFav) {
        this.isFav = isFav;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public static boolean phoneIsMobile(String number){
        return number.startsWith("07");
    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }
}
