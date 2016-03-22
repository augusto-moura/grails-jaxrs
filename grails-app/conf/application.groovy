grails {
    profile = 'web'
    codegen {
        defaultPackage = 'org.grails.plugins.jaxrs'
    }
}

info {
    app {
        name = '@info.app.name@'
        version = '@info.app.version@'
        grailsVersion = '@info.app.grailsVersion@'
    }
}

spring {
    groovy {
        template['check-template-location'] = false
    }
}

grails {
    mime {
        disable {
            accept {
                header {
                    userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
                }
            }
        }

        types {
            all = '*/*'
            atom = 'application/atom+xml'
            css = 'text/css'
            csv = 'text/csv'
            form = 'application/x-www-form-urlencoded'
            html = ['text/html', 'application/xhtml+xml']
            js = 'text/javascript'
            json = ['application/json', 'text/json']
            multipartForm = 'multipart/form-data'
            rss = 'application/rss+xml'
            text = 'text/plain'
            hal = ['application/hal+json', 'application/hal+xml']
            xml = ['text/xml', 'application/xml']
        }
    }
    urlmapping {
        cache {
            maxsize = 1000
        }
    }
    controllers {
        defaultScope = 'singleton'
    }
    converters {
        encoding = 'UTF-8'
    }
    views {
        'default' { codec = 'html' }//THIS WAS THE SOURCE OF ERROR
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml'
            codecs {
                expression = 'html'
                scriptlets = 'html'
                taglib = 'none'
                staticparts = 'none'
            }
        }
    }
}


org.grails.jaxrs.url.mappings = ['']



