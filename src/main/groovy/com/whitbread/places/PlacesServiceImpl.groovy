package com.whitbread.places

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper
import com.couchbase.client.java.document.RawJsonDocument
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.AsyncN1qlQueryRow
import com.google.inject.Inject
import ratpack.rx.RxRatpack
import rx.Observable

class PlacesServiceImpl implements PlacesService {

    private final PlacesCbCommand cbCommand
    private final FoursquareCommand foursquareCommand
    private final ObjectMapper mapper

    @Inject
    public PlacesServiceImpl(PlacesCbCommand cbCommand, FoursquareCommand foursquareCommand, ObjectMapper mapper) {
        this.cbCommand = cbCommand
        this.foursquareCommand = foursquareCommand
        this.mapper = mapper
    }

    @Override
    Observable<Place> retrieveAll() {
        cbCommand.retrieveAll().flatMap { AsyncN1qlQueryResult cbResult ->
            cbResult.rows()
                    .map { AsyncN1qlQueryRow cbRow ->
                def id = cbRow.value().getObject("default").getString("_id").replaceFirst("${Place.TYPE}::", "")
                Place cbPlace = mapper.readValue(cbRow.value().getObject('default').toString(), Place)
                Place.withId(cbPlace, id)
            }
        }
    }

    @Override
    Observable<Place> retrieveById(String id) {
        cbCommand.retrieveById(id).flatMap { AsyncN1qlQueryResult cbResult ->
            cbResult.rows()
                    .map { AsyncN1qlQueryRow cbRow ->
                Place cbPlace = mapper.readValue(cbRow.value().getObject('places').toString(), Place)
                Place.withId(cbPlace, id)
            }
        }
    }

    @Override
    Observable<Place> retrieveByName(String name) {
        cbCommand.retrieveByName(name).flatMap { AsyncN1qlQueryResult cbResult ->
            cbResult.rows()
                    .map { AsyncN1qlQueryRow cbRow ->
                def id = cbRow.value().getObject("places").getString("_id").replaceFirst("${Place.TYPE}::", "")
                Place cbPlace = mapper.readValue(cbRow.value().getObject('places').toString(), Place)
                Place.withId(cbPlace, id)
            }
        }
    }

    @Override
    Observable<Place> create(final Place place) {
        RxRatpack
                .bindExec(retrieveByName(place.name))
                .single()
                .doOnCompleted { throw new IllegalArgumentException("Place already exist")}
                .onErrorResumeNext { error ->
            if (error instanceof NoSuchElementException) {
                cbCommand.save(place, mapper)
                        .map { RawJsonDocument doc ->
                    def id = doc.id().replaceFirst("${Place.TYPE}::", "")
                    Place cbPlace = mapper.readValue(doc.content(), Place)
                    Place.withId(cbPlace, id)
                }
            }
        }
    }

    @Override
    Observable<String> search(String location) {
        RxRatpack
                .bindExec(retrieveByName(location))
                .single()
                .flatMap { place ->
            foursquareCommand.exploreVenueByGeoRequest(place.latitude, place.longitude)
        }
        .onErrorResumeNext { error ->
            if (error instanceof NoSuchElementException) {
                foursquareCommand.exploreVenueByNameRequest(location)
            }
        }
    }
}

