package com.example.ncuculova.taxinadica;

import java.text.Collator;
import java.util.Locale;

/**
 * Created by ncuculova on 25.8.15.
 */
public class City implements Comparable<City>{

    private String name;

    private String slug;

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City city = (City) o;

        return !(id != null ? !id.equals(city.id) : city.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id : 0;
    }

    @Override
    public int compareTo(City city) {
        Locale mk = new Locale("mk_MK");
        Collator collator = Collator.getInstance(mk);
        return collator.compare(name, city.name);
    }
}
