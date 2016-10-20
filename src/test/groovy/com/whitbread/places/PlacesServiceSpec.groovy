package com.whitbread.places

import ratpack.jackson.JsonRender
import ratpack.rx.RxRatpack
import spock.lang.Specification

import static ratpack.groovy.test.handling.GroovyRequestFixture.handle


class PlacesServiceSpec extends Specification {

    def setup() {
        RxRatpack.initialize()
    }

    def "will return all places"() {
        given: "two places inside the database"
        def heathrow = new Place(name: "heathrow", latitude: 51.4673d, longitude: -0.4529d)
        def gatwick = new Place(name: "gatwick", latitude: 51.16667d, longitude: -0.18333d)
        def savedHeathrow = Place.withId(heathrow, "1")
        def savedGatwick = Place.withId(gatwick, "2")

        and:
        rx.Observable<Place> retrieveAllObs = rx.Observable.from([savedHeathrow, savedGatwick])

        and:
        def placesService = Mock(PlacesService)
        placesService.retrieveAll() >> retrieveAllObs

        when: "a GET to api/places is received"
        def result = handle(new PlacesEndpoint(placesService)) {
            method "get"
            header "Accept", "application/json"
        }

        then: "should return two places"
        with(result) {
            rendered(JsonRender).object == [savedHeathrow, savedGatwick]
            rendered(JsonRender).object[0]._id == "1"
            rendered(JsonRender).object[1]._id == "2"
        }
    }

    def "will store the in the database"() {
        given: "a new place"
        def heathrow = new Place(name: "heathrow", latitude: 51.4673d, longitude: -0.4529d)
        def savedHeathrow = Place.withId(heathrow, "1")

        and:
        rx.Observable<Place> createObs = rx.Observable.just(savedHeathrow)

        and:
        def placesService = Mock(PlacesService)
        placesService.create(heathrow) >> createObs

        when: "a POST to api/places is received"
        def result = handle(new PlacesEndpoint(placesService)) {
            body """{"name":"heathrow", "latitude": 51.4673 , "longitude": -0.4529}""", "application/json"
            method "post"
            header "Accept", "application/json"
        }

        then: "should return the location to the saved place"
        with(result) {
            rendered(JsonRender).object == savedHeathrow
        }
    }

    def "will retrieve a place"() {
        given: "an id"
        def heathrowId = 1
        def heathrow = new Place(name: "heathrow", latitude: 51.4673d, longitude: -0.4529d)
        def savedHeathrow = Place.withId(heathrow, "1")

        and:
        rx.Observable<Place> retrieveById = rx.Observable.just(savedHeathrow)

        and:
        def placesService = Mock(PlacesService)
        placesService.retrieveById(_) >> retrieveById

        when: "a GET to api/places/:id is received"
        def result = handle(new PlacesEndpoint(placesService)) {
            uri "$heathrowId"
            method "get"
            header "Accept", "application/json"
        }

        then: "should return a place"
        with(result) {
            status.code == 200
            rendered(JsonRender).object._id == savedHeathrow._id
        }
    }

    def "will return a 404 "() {
        given: "an invalid id"
        def heathrowId = 1
        def heathrow = new Place(name: "heathrow", latitude: 51.4673d, longitude: -0.4529d)
        def savedHeathrow = Place.withId(heathrow, "1")

        and:
        rx.Observable<Place> retrieveById = rx.Observable.just(null)

        and:
        def placesService = Mock(PlacesService)
        placesService.retrieveById(_) >> retrieveById

        when: "a GET to api/places/:id is received"
        def result = handle(new PlacesEndpoint(placesService)) {
            uri "$heathrowId"
            method "get"
            header "Accept", "application/json"
        }

        then: "should return a 404 NO FOUND"
        with(result) {
            status.code == 404
        }
    }
}
