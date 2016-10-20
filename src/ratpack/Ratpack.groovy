import com.whitbread.config.FoursquareConfig
import com.whitbread.config.ServiceConfig
import com.whitbread.database.CouchbaseConfig
import com.whitbread.database.CouchbaseModule
import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json

ratpack {
    serverConfig {
        yaml "application.yaml"
        sysProps()
        env()
    }

    bindings {
        bindInstance(ServiceConfig, serverConfig.get('/service', ServiceConfig))
        bindInstance(FoursquareConfig, serverConfig.get('/foursquare', FoursquareConfig))

        moduleConfig(CouchbaseModule, serverConfig.get('/couchbase', CouchbaseConfig))
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
    }
}
