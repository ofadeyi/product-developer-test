package com.whitbread

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest
import ratpack.http.client.RequestSpec
import ratpack.test.ApplicationUnderTest
import ratpack.test.http.TestHttpClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ProductDevTestSpec extends Specification {

    @Shared
    @AutoCleanup
    ApplicationUnderTest aut = new GroovyRatpackMainApplicationUnderTest()

    @Delegate
    TestHttpClient client = aut.httpClient


    def "create places"() {
        given:
        def json = new JsonSlurper()

        when:
        requestSpec { RequestSpec requestSpec ->
            requestSpec.body.type("application/json")
            requestSpec.body.text(JsonOutput.toJson([name: "heathrow", latitude: 51.4673d, longitude: -0.4529d]))
        }
        post("api/places")

        then:
        def place = json.parseText(response.body.text)
        with(place) {
            _type == "place"
            name == "heathrow"
            latitude == 51.4673d
            longitude == -0.4529d
        }

        and:
        resetRequest()
        def readPlace = json.parseText(get("api/places/${place._id}").body.text)
        with(readPlace) {
            get("name") == "heathrow"
            get("latitude") == 51.4673d
            get("longitude") == -0.4529d
        }
    }

}
