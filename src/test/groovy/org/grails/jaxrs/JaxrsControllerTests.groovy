package org.grails.plugins.jaxrs

import grails.test.mixin.TestFor
import org.grails.plugins.jaxrs.JaxrsController

/*
 * Copyright 2009 - 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.grails.plugins.jaxrs.web.JaxrsUtils
import org.grails.plugins.jaxrs.web.UnitTestEnvironment
import spock.lang.Specification

/**
 * @author Martin Krasser
 */
@TestFor(JaxrsController)
class JaxrsControllerTests extends Specification {

    static environment = new UnitTestEnvironment()

    void setup() {
        controller.jaxrsContext = environment.jaxrsContext
    }

    void testGetTest() {
        controller.request.method = 'GET'
        JaxrsUtils.setRequestUriAttribute(controller.request, '/test')
        controller.handle()
        assertEquals('/test', controller.response.contentAsString.trim())
    }
}

