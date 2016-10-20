package com.whitbread.places

import rx.Observable


interface FoursquareCommand {
    Observable<String> exploreVenueByGeoRequest(final double latitude, double longitude)
    Observable<String> exploreVenueByNameRequest(final String location)
}