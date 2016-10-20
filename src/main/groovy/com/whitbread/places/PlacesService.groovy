package com.whitbread.places

import rx.Observable

interface PlacesService {

    Observable<Place> retrieveAll()
    Observable<Place> retrieveById(String id)
    Observable<Place> retrieveByName(String name)
    Observable<Place> create(final Place place)

    Observable<String> search(String location)
}