import com.whitbread.config.FoursquareConfig
import com.whitbread.config.ServiceConfig
import com.whitbread.database.CouchbaseConfig
import com.whitbread.database.CouchbaseModule
import com.whitbread.places.PlacesEndpoint
import com.whitbread.places.PlacesModule
import ratpack.hystrix.HystrixModule

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
    serverConfig {
        yaml "application.yaml"
        sysProps()
        env()

        require("/service", ServiceConfig)
        require("/foursquare", FoursquareConfig)
    }

    bindings {
        moduleConfig(CouchbaseModule, serverConfig.get('/couchbase', CouchbaseConfig))
        module new HystrixModule().sse()
        module PlacesModule
    }

    handlers {
        get { ServiceConfig config ->
            render "Welcome to ${config.message}"
        }

        prefix('config') {
            path('couchbase') { CouchbaseConfig config ->
                byMethod {
                    get {
                        render json(config)
                    }
                }
            }
            path('foursquare') { FoursquareConfig config ->
                byMethod {
                    get {
                        render json(config)
                    }
                }
            }
        }

        prefix('api/places'){
            all chain(registry.get(PlacesEndpoint))
        }
    }
}
