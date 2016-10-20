package com.whitbread.places

import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper
import com.couchbase.client.java.document.RawJsonDocument
import com.couchbase.client.java.query.AsyncN1qlQueryResult
import rx.Observable

interface PlacesCbCommand {
    Observable<AsyncN1qlQueryResult> retrieveAll()
    Observable<AsyncN1qlQueryResult> retrieveById(final String id)
    Observable<AsyncN1qlQueryResult> retrieveByName(final String name)
    Observable<RawJsonDocument> save(final Place place, ObjectMapper mapper)
}
