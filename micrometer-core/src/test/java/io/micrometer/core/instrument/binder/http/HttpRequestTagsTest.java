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
package io.micrometer.core.instrument.binder.http;

import io.micrometer.core.instrument.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HttpRequestTags}.
 */
class HttpRequestTagsTest {

    private static final Tag METHOD_UNKNOWN = Tag.of("method", "UNKNOWN");

    @Test
    void nullRequestShouldContributeUnknownMethodTag() {
        assertThat(HttpRequestTags.method((javax.servlet.http.HttpServletRequest) null)).isEqualTo(METHOD_UNKNOWN);
        assertThat(HttpRequestTags.method((jakarta.servlet.http.HttpServletRequest) null)).isEqualTo(METHOD_UNKNOWN);
    }

    @Test
    void unknownMethodShouldContributeUnknownMethodTagValue() {
        assertThat(HttpRequestTags.method(mockJavaxRequest("WELL"))).isEqualTo(METHOD_UNKNOWN);
        assertThat(HttpRequestTags.method(mockJakartaRequest("WELL"))).isEqualTo(METHOD_UNKNOWN);
    }

    @Test
    void requestShouldContributeMethodTag() {
        Tag httpGet = Tag.of("method", "GET");
        assertThat(HttpRequestTags.method(mockJavaxRequest("GET"))).isEqualTo(httpGet);
        assertThat(HttpRequestTags.method(mockJakartaRequest("GET"))).isEqualTo(httpGet);
    }

    @Test
    void standardMethodsShouldContributeMethodTag() {
        for (String method : new String[] { "GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE",
                "PATCH" }) {
            Tag expected = Tag.of("method", method);
            assertThat(HttpRequestTags.method(mockJavaxRequest(method))).isEqualTo(expected);
            assertThat(HttpRequestTags.method(mockJakartaRequest(method))).isEqualTo(expected);
        }
    }

    @Test
    void attackerControlledMethodsShouldNotInflateTagCardinality() {
        // an attacker sending arbitrary request methods must not be able to create
        // an unbounded number of distinct `method` tag values
        for (String method : new String[] { "get", "Get", "ATTACK-0", "ATTACK-1", "", "GET /foo HTTP/1.1" }) {
            assertThat(HttpRequestTags.method(mockJavaxRequest(method))).isEqualTo(METHOD_UNKNOWN);
            assertThat(HttpRequestTags.method(mockJakartaRequest(method))).isEqualTo(METHOD_UNKNOWN);
        }
    }

    private javax.servlet.http.HttpServletRequest mockJavaxRequest(String method) {
        javax.servlet.http.HttpServletRequest request = mock(javax.servlet.http.HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        return request;
    }

    private jakarta.servlet.http.HttpServletRequest mockJakartaRequest(String method) {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        return request;
    }

}
