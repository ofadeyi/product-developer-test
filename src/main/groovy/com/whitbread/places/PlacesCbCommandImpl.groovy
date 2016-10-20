package com.whitbread.places

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper
import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.document.RawJsonDocument
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.N1qlQueryResult
import com.couchbase.client.java.query.Statement
import com.google.inject.Inject
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import com.whitbread.database.CouchbaseService
import groovy.util.logging.Slf4j
import rx.Observable

import static com.couchbase.client.java.query.Select.select
import static com.couchbase.client.java.query.dsl.Expression.*

@Slf4j
class PlacesCbCommandImpl implements PlacesCbCommand {

    private static
    final HystrixCommandGroupKey hystrixCommandGroupKey = HystrixCommandGroupKey.Factory.asKey("cb-places")
    private final CouchbaseService couchbase;

    @Inject
    public PlacesCbCommandImpl(CouchbaseService couchbase) {
        this.couchbase = couchbase
    }

    Observable<AsyncN1qlQueryResult> retrieveAll() {
        return new HystrixObservableCommand<N1qlQueryResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("retrieveAll"))) {

            @Override
            protected Observable<AsyncN1qlQueryResult> construct() {
                AsyncBucket bucket = couchbase.getBucket()
                Statement query = select("*")
                        .from(i(bucket.name()))
                        .where(x("_type").eq(s(Place.TYPE)))
                log.info("Executing Query: {}", query)
                bucket.query(N1qlQuery.simple(query))
            }

            @Override
            protected String getCacheKey() {
                return "cb-places-retrieveAll"
            }
        }.toObservable()
    }

    Observable<AsyncN1qlQueryResult> retrieveById(final String id) {
        return new HystrixObservableCommand<N1qlQueryResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("retrieve"))) {

            @Override
            protected Observable<AsyncN1qlQueryResult> construct() {
                AsyncBucket bucket = couchbase.getBucket()
                Statement query = select("*")
                        .from(i(bucket.name())).as('places')
                        .where(x("_type").eq(s("${Place.TYPE}"))
                        .and("""META(places).id = '${Place.TYPE}::$id'"""))
                log.info("Executing Query: {}", query)
                bucket.query(N1qlQuery.simple(query))
            }

            @Override
            protected String getCacheKey() {
                return "cb-places-retrieve-$id"
            }
        }.toObservable()
    }

    Observable<AsyncN1qlQueryResult> retrieveByName(final String name) {
        return new HystrixObservableCommand<N1qlQueryResult>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("retrieve"))) {

            @Override
            protected Observable<AsyncN1qlQueryResult> construct() {
                AsyncBucket bucket = couchbase.getBucket()
                Statement query = select("*")
                        .from(i(bucket.name())).as('places')
                        .where(x("_type").eq(s("${Place.TYPE}"))
                        .and(x("name").eq(s(name))))
                log.info("Executing Query: {}", query)
                bucket.query(N1qlQuery.simple(query))
            }

            @Override
            protected String getCacheKey() {
                return "cb-places-retrieve-$name"
            }
        }.toObservable()
    }

    @Override
    Observable<RawJsonDocument> save(Place place, ObjectMapper mapper) {
        return new HystrixObservableCommand<RawJsonDocument>(
                HystrixObservableCommand.Setter.withGroupKey(hystrixCommandGroupKey).andCommandKey(HystrixCommandKey.Factory.asKey("save"))) {

            @Override
            protected rx.Observable<RawJsonDocument> construct() {
                AsyncBucket bucket = couchbase.getBucket()
                bucket.counter("${Place.TYPE}::id", 1, 1)
                        .map { counter ->
                    "${Place.TYPE}::" + counter.content();
                }
                .flatMap { id ->
                    Place withId = Place.withId(place, id)
                    String placeAsString = mapper.writeValueAsString(withId)
                    bucket.insert(RawJsonDocument.create(id, placeAsString))
                }
            }
        }.toObservable()
    }
}
