package com.whitbread.places

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Canonical

@Canonical(excludes = ["_id","_type"])
@JsonIgnoreProperties(ignoreUnknown=true)
class Place {
    public static final String TYPE = 'place'

    String name
    double latitude
    double longitude
    final String _type = TYPE
    String _id;

    static Place withId(Place place, String id){
        Place saved = new Place(place.name, place.latitude, place.longitude)
        saved._id = id
        saved
    }
}
