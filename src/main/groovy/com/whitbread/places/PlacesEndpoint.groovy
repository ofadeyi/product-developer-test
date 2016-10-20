package com.whitbread.places

import groovy.json.JsonSlurper
import io.netty.handler.codec.http.HttpResponseStatus
import ratpack.groovy.handling.GroovyChainAction
import ratpack.rx.RxRatpack
import rx.Observable

import javax.inject.Inject

import static ratpack.jackson.Jackson.fromJson
import static ratpack.jackson.Jackson.json

class PlacesEndpoint extends GroovyChainAction {

    PlacesService placesService

    @Inject
    PlacesEndpoint(PlacesService placesService) {
        this.placesService = placesService;
    }

    @Override
    void execute() throws Exception {

        path('search') {
            def query = request.queryParams.get("q")
            byMethod {
                get {
                    RxRatpack.bindExec(placesService.search(query))
                            .single()
                            .subscribe(
                            { String fsResponse ->
                                def jsonObject = new JsonSlurper().parseText(fsResponse)
                                def groups = jsonObject?.response?.groups
                                def venues =
                                        groups?.stream()
                                                .filter { element -> element["name"] == "recommended" }
                                                .flatMap { element ->
                                            element["items"]?.stream()
                                                    .map { item -> item["venue"] }
                                        }
                                        .collect()
                                render json(venues)
                            },
                            { Throwable e ->
                                clientError HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                            })
                }
            }
        }

        path(":id") {
            def placeId = pathTokens["id"]
            byMethod {
                get {
                    RxRatpack.bindExec(placesService.retrieveById(placeId))
                            .single()
                            .onErrorResumeNext { error ->
                        if (error instanceof NoSuchElementException) {
                            Observable.just(null)
                        }
                    }
                            .subscribe({ Place place ->
                        if (place != null) {
                            response.status(HttpResponseStatus.OK.code())
                            render json(place)
                        } else {
                            clientError HttpResponseStatus.NOT_FOUND.code()
                        }
                    }, { Throwable e ->
                        clientError HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    })
                }
            }
        }

        all {
            byMethod {
                get {
                    RxRatpack.bindExec(placesService.retrieveAll())
                            .toList()
                            .subscribe({ List<Place> places ->
                        if (places.isEmpty()) {
                            response.status(HttpResponseStatus.NO_CONTENT.code())
                            render ""
                        } else {
                            response.status(HttpResponseStatus.OK.code())
                            render json(places)
                        }
                    }, { Throwable e ->
                        clientError HttpResponseStatus.INTERNAL_SERVER_ERROR.code()
                    })
                }

                post {
                    parse(fromJson(Place)).
                            observe().
                            flatMap { newPlace ->
                                RxRatpack.bindExec(placesService.create(newPlace))
                            }.
                            single().
                            subscribe ({ savedPlace ->
                                response.status(HttpResponseStatus.CREATED.code())
                                response.headers.add('location', "/api/places/${savedPlace._id}")
                                render json(savedPlace)
                            }, { Throwable e ->
                                clientError HttpResponseStatus.BAD_REQUEST.code()
                            })
                }
            }
        }
    }
}
