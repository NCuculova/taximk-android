package com.ncuculova.taximk;

/**
 * Created by ncuculova on 25.8.15.
 */
public class City {

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

}
