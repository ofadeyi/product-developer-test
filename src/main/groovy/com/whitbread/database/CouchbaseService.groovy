package com.whitbread.database

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.CouchbaseAsyncCluster
import groovy.util.logging.Slf4j
import ratpack.rx.RxRatpack
import ratpack.service.Service
import ratpack.service.StartEvent
import ratpack.service.StopEvent
import rx.functions.Actions

@Slf4j
class CouchbaseService implements Service {

    private CouchbaseAsyncCluster cluster;
    private CouchbaseConfig config;
    private AsyncBucket bucket;

    CouchbaseService(CouchbaseConfig config) {
        this.config = config;
    }

    @Override
    void onStart(StartEvent event) throws Exception {
        log.info 'Starting Ratpack application'
        log.info "Initializing RX"
        RxRatpack.initialize()

        //use that to bootstrap the Cluster
        this.cluster = CouchbaseAsyncCluster.create(this.config.seedNodes);

        cluster.openBucket(config.bucketName, config.bucketPassword)
                .doOnNext { openedBucket -> log.info "Bucket opened  ${openedBucket.name()}" }
                .doOnNext { openedBucket -> bucket = openedBucket }
                .subscribe { Actions.empty() }
    }

    @Override
    void onStop(StopEvent event) throws Exception {
        cluster.disconnect()
                .doOnNext { isDisconnectedCleanly -> log.info "Disconnected Cluster (cleaned threads: ${isDisconnectedCleanly})" }
                .subscribe();
    }

    AsyncBucket getBucket() {
        return bucket;
    }
}
