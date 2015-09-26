package com.ncuculova.taximk;

import java.util.List;
import java.util.Set;

/**
 * Created by ncuculova on 26.8.15.
 */
public class Data {

    private List<Taxi> taxis;

    private Set<City> cities;

    public List<Taxi> getTaxis() {
        return taxis;
    }

    public void setTaxis(List<Taxi> taxis) {
        this.taxis = taxis;
    }

    public Set<City> getCities() {
        return cities;
    }

    public void setCities(Set<City> cities) {
        this.cities = cities;
    }
}
