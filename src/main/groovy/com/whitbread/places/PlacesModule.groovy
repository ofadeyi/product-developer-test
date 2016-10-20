package com.whitbread.places

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class PlacesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PlacesCbCommand).to(PlacesCbCommandImpl).in(Scopes.SINGLETON);
        bind(PlacesService).to(PlacesServiceImpl).in(Scopes.SINGLETON);
        bind(PlacesEndpoint).in(Scopes.SINGLETON);
        bind(FoursquareCommand).to(FoursquareCommandImpl).in(Scopes.SINGLETON);
    }
}
