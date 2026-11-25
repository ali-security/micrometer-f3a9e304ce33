/*
 * Copyright 2026 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.jersey.server;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultJerseyObservationConvention}.
 */
class DefaultJerseyObservationConventionTests {

    private final DefaultJerseyObservationConvention convention = new DefaultJerseyObservationConvention(
            "http.server.requests");

    @Test
    void nullCarrierShouldResultInNullContextualName() {
        assertThat(convention.getContextualName(context(null))).isNull();
    }

    @Test
    void knownMethodShouldBeUsedInContextualName() {
        assertThat(convention.getContextualName(context("GET"))).isEqualTo("HTTP GET");
    }

    @Test
    void unknownMethodShouldResultInUnknownContextualName() {
        assertThat(convention.getContextualName(context("WELL"))).isEqualTo("HTTP UNKNOWN");
    }

    @Test
    void attackerControlledMethodsShouldNotInflateContextualNameCardinality() {
        for (String method : new String[] { "get", "ATTACK-0", "ATTACK-1", "", "GET /foo HTTP/1.1" }) {
            assertThat(convention.getContextualName(context(method))).isEqualTo("HTTP UNKNOWN");
        }
    }

    private JerseyContext context(String method) {
        RequestEvent event = mock(RequestEvent.class);
        if (method != null) {
            ContainerRequest request = mock(ContainerRequest.class);
            when(request.getMethod()).thenReturn(method);
            when(event.getContainerRequest()).thenReturn(request);
        }
        return new JerseyContext(event);
    }

}
