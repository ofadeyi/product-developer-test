import com.whitbread.config.ServiceConfig
import static ratpack.groovy.Groovy.ratpack

ratpack {
    serverConfig {
        yaml "application.yaml"
        sysProps()
        env()
    }

    bindings {
        bindInstance(ServiceConfig, serverConfig.get('/service', ServiceConfig))

  }

    handlers {
        get { ServiceConfig config ->
            render "Welcome to ${config.message}"
        }

    }
}
