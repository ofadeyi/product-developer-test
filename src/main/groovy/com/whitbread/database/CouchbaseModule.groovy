package com.whitbread.database

import com.google.inject.Provides
import com.google.inject.Singleton
import ratpack.guice.ConfigurableModule


class CouchbaseModule extends ConfigurableModule<CouchbaseConfig>{
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public CouchbaseService couchbaseService(CouchbaseConfig config) {
        return new CouchbaseService(config);
    }
}
