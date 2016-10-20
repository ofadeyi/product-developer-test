package com.whitbread.places

import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import com.whitbread.config.FoursquareConfig
import groovy.util.logging.Slf4j
import ratpack.http.client.HttpClient
import ratpack.http.client.ReceivedResponse
import rx.Observable

import static ratpack.rx.RxRatpack.observe

@Slf4j
class FoursquareCommandImpl implements FoursquareCommand {

    private final FoursquareConfig config
    private final HttpClient httpClient
    final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("foursquare")

    @Inject
    public FoursquareCommandImpl(FoursquareConfig config, HttpClient httpClient) {
        this.config = config
        this.httpClient = httpClient
    }

    public Observable<String> exploreVenueByNameRequest(final String location) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("exploreVenueByNameRequest"))) {

            @Override
            protected Observable<String> construct() {
                def uri = "${config.host}/${config.exploreEndpoint}?near=$location&limit=10&client_id=${config.clientId}&client_secret=${config.clientSecret}&v=${config.version}".toURI()
                log.info("Calling the Foursquare API with: {}", uri)

                observe(httpClient.get(uri)).map { ReceivedResponse response ->
                    response.body.text
                }
            }

            @Override
            protected Observable<String> resumeWithFallback() {
                return Observable.just('{"data" : "service not available"}')
            }

            @Override
            protected String getCacheKey() {
                return "http-foursquare-$location"
            }
        }.toObservable()
    }

    public Observable<String> exploreVenueByGeoRequest(final double latitude, double longitude) {
        return new HystrixObservableCommand<String>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("exploreVenueByGeoRequest"))) {


            @Override
            protected Observable<String> construct() {
                def uri = "${config.host}/${config.exploreEndpoint}?ll=$latitude,$longitude&range=500&limit=10&client_id=${config.clientId}&client_secret=${config.clientSecret}&v=${config.version}".toURI()
                log.info("Calling the Foursquare API with: {}", uri)

                observe(httpClient.get(uri)).map { ReceivedResponse response ->
                    response.body.text
                }
            }

            @Override
            protected Observable<String> resumeWithFallback() {
                return Observable.just('{"data" : "service not available"}')
            }

            @Override
            protected String getCacheKey() {
                return "http-foursquare-$latitude-$longitude"
            }
        }.toObservable()
    }
}
