package org.grails.plugins.jaxrs

import grails.plugins.Plugin
import org.grails.plugins.jaxrs.artefact.ProviderArtefactHandler
import org.grails.plugins.jaxrs.artefact.ResourceArtefactHandler
import org.grails.plugins.jaxrs.core.JaxrsContext
import org.grails.plugins.jaxrs.core.JaxrsFilter
import org.grails.plugins.jaxrs.core.JaxrsListener
import org.grails.plugins.jaxrs.core.JaxrsUtil
import org.grails.plugins.jaxrs.generator.CodeGenerator
import org.grails.plugins.jaxrs.provider.*
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean
import org.springframework.core.Ordered

class JaxrsGrailsPlugin extends Plugin {
    /**
     * Version of the plugin.
     *
     * Note that the build.gradle file must be updated with this version as well.
     */
    def version = "0.12"

    /**
     * What version of Grails this plugin is intended for.
     */
    def grailsVersion = "3.0 > *"

    /**
     * Files to exclude from the plugin.
     *
     * Note that the build.gradle file must be updated with this list as well.
     */
    def pluginExcludes = [
        "grails-app/domain/*",
        "grails-app/providers/*",
        "grails-app/resources/*",
        "src/groovy/org/grails/jaxrs/test/*",
        "lib/*-sources.jar",
        "web-app/**"
    ]

    /**
     * Load order.
     */
    def loadAfter = ['controllers', 'services', 'spring-security-core']

    /**
     * Which files to watch for reloading.
     */
    def watchedResources = [
        "file:./grails-app/resources/**/*Resource.groovy",
        "file:./grails-app/providers/**/*Reader.groovy",
        "file:./grails-app/providers/**/*Writer.groovy",
        "file:./plugins/*/grails-app/resources/**/*Resource.groovy",
        "file:./plugins/*/grails-app/providers/**/*Reader.groovy",
        "file:./plugins/*/grails-app/providers/**/*Writer.groovy"
    ]

    /**
     * Plugin author.
     */
    def author = "Martin Krasser"

    /**
     * Author email address.
     */
    def authorEmail = "krasserm@googlemail.com"

    /**
     * Plugin title.
     */
    def title = "JSR 311 plugin"

    /**
     * Description of the plugin.
     */
    def description = """
A plugin that supports the development of RESTful web services based on the
Java API for RESTful Web Services (JSR 311: JAX-RS). It is targeted at
developers who want to structure the web service layer of an application in
a JSR 311 compatible way but still want to continue to use Grails' powerful
features such as GORM, automated XML and JSON marshalling, Grails services,
Grails filters and so on. This plugin is an alternative to Grails' built-in
mechanism for implementing  RESTful web services.

At the moment, plugin users may choose between Jersey and Restlet as JAX-RS
implementation. Both implementations are packaged with the plugin. Support for
Restlet was added in version 0.2 of the plugin in order to support deployments
on the Google App Engine. Other JAX-RS implementations such as RestEasy or
Apache Wink are likely to be added in upcoming versions of the plugin.
"""

    /**
     * Developers who have contributed to the development of the plugin.
     */
    def developers = [
        [name: 'Davide Cavestro', email: 'davide.cavestro@gmail.com'],
        [name: 'Noam Y. Tenne', email: 'noam@10ne.org'],
        [name: 'Bud Byrd', email: 'bud.byrd@gmail.com']
    ]

    /**
     * Documentation URL.
     */
    def documentation = 'https://github.com/krasserm/grails-jaxrs/wiki'

    /**
     * Issues URL.
     */
    def issueManagement = [url: 'https://github.com/krasserm/grails-jaxrs/issues']

    /**
     * Source control URL.
     */
    def scm = [url: 'https://github.com/krasserm/grails-jaxrs']

    /**
     * Adds the JaxrsContext and plugin- and application-specific JAX-RS
     * resource and provider classes to the application context.
     */
    Closure doWithSpring() {
        { ->
            jaxrsListener(ServletListenerRegistrationBean) {
                listener = bean(JaxrsListener)
                order = Ordered.LOWEST_PRECEDENCE
            }

            jaxrsFilter(FilterRegistrationBean) {
                filter = bean(JaxrsFilter)
                order = Ordered.HIGHEST_PRECEDENCE + 10
            }

            "${CodeGenerator.name}"(CodeGenerator)

            jaxrsContext(JaxrsContext) {
                jaxrsServletFactory = ref('jaxrsServletFactory')
            }

            "${JaxrsUtil.BEAN_NAME}"(JaxrsUtil) { bean ->
                bean.autowire = true
            }

            "${XMLWriter.name}"(XMLWriter)
            "${XMLReader.name}"(XMLReader)
            "${JSONWriter.name}"(JSONWriter)
            "${JSONReader.name}"(JSONReader)
            "${DomainObjectReader.name}"(DomainObjectReader)
            "${DomainObjectWriter.name}"(DomainObjectWriter)

            String requestedScope = getResourceScope(grailsApplication)

            grailsApplication.resourceClasses.each { rc ->
                "${rc.propertyName}"(rc.clazz) { bean ->
                    bean.scope = requestedScope
                    bean.autowire = true
                }
            }

            grailsApplication.providerClasses.each { pc ->
                "${pc.propertyName}"(pc.clazz) { bean ->
                    bean.scope = 'singleton'
                    bean.autowire = true
                }
            }
        }
    }

    /**
     * Reconfigures the JaxrsApplicationConfig with plugin- and application-specific
     * JAX-RS resource and provider classes. Configures the JaxrsContext
     * with the JAX-RS implementation to use. The name of the JAX-RS
     * implementation is obtained from the configuration property
     * <code>org.grails.jaxrs.provider.name</code>. Default value is
     * <code>jersey</code>.
     */
    void doWithApplicationContext() {
        JaxrsUtil jaxrsUtil = JaxrsUtil.getInstance(applicationContext)

        jaxrsUtil.setupJaxrsContext()
        jaxrsUtil.jaxrsContext.init()
    }

    /**
     * Updates application-specific JAX-RS resource and provider classes in
     * the application context.
     */
    void onChange(Map<String, Object> event) {
        if (!event.ctx) {
            return
        }

        // Determine the requested resource bean scope
        String requestedScope = getResourceScope(grailsApplication)

        if (grailsApplication.isArtefactOfType(ResourceArtefactHandler.TYPE, event.source)) {
            def resourceClass = grailsApplication.addArtefact(ResourceArtefactHandler.TYPE, event.source)
            beans {
                "${resourceClass.propertyName}"(resourceClass.clazz) { bean ->
                    bean.scope = requestedScope
                    bean.autowire = true
                }
            }.registerBeans(event.ctx)
        }
        else if (grailsApplication.isArtefactOfType(ProviderArtefactHandler.TYPE, event.source)) {
            def providerClass = grailsApplication.addArtefact(ProviderArtefactHandler.TYPE, event.source)
            beans {
                "${providerClass.propertyName}"(providerClass.clazz) { bean ->
                    bean.scope = 'singleton'
                    bean.autowire = true
                }
            }.registerBeans(event.ctx)
        }
        else {
            return
        }

        // Update the jaxrs context and reinitialize it
        JaxrsUtil.getInstance().setupJaxrsContext()
        JaxrsUtil.getInstance().jaxrsContext.restart();
    }

    /**
     * Returns the scope for all resource classes as requested by
     * the application configuration. Defaults to "prototype".
     *
     * @param application
     * @return
     */
    private String getResourceScope(application) {
        def scope = application.config.org.grails.jaxrs.resource.scope
        if (!scope) {
            scope = 'prototype'
        }
        return scope
    }
}
