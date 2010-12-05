/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.voidengineer.springframework.cache.infinispan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

public class InfinispanSupportTest {

    @Test
    public void createEmbeddedCacheManagerWithoutConfigurationLocation() throws Exception {
        final InfinispanEmbeddedCacheManagerFactoryBean factory = new InfinispanEmbeddedCacheManagerFactoryBean();
        assertThat(factory.getObjectType().equals(EmbeddedCacheManager.class), is(true));
        assertThat(factory.isSingleton(), is(true));

        factory.afterPropertiesSet();
        try {
            final EmbeddedCacheManager embeddedCacheManager = factory.getObject();
            assertThat("Loaded EmbeddedCacheManager with no caches", embeddedCacheManager.getCacheNames().isEmpty());

            final Cache<Object, Object> cache = embeddedCacheManager.getCache("testCache1");
            validateNamedCache(cache, -1);
        } finally {
            factory.destroy();
        }
    }

    @Test
    public void createEmbeddedCacheManagerWithConfigurationLocation() throws Exception {
        final InfinispanEmbeddedCacheManagerFactoryBean factory = new InfinispanEmbeddedCacheManagerFactoryBean();
        factory.setConfigLocation(new ClassPathResource("test-infinispan-config.xml", this.getClass()));

        factory.afterPropertiesSet();
        try {
            final EmbeddedCacheManager embeddedCacheManager = factory.getObject();
            assertThat(embeddedCacheManager.getClusterName(), is(equalTo("Test-Infinispan-Cluster")));
            assertThat(embeddedCacheManager.getCacheNames().size(), is(equalTo(1)));
            assertThat(embeddedCacheManager.getCacheNames(), contains("testCache1"));

            final Cache<?, ?> cache = embeddedCacheManager.getCache("testCache1");
            validateNamedCache(cache, 1000);
        } finally {
            factory.destroy();
        }
    }

    private final void validateNamedCache(final Cache<?, ?> cache, final int expectedEvictionMaxEntries) {
        assertThat(cache, is(not(nullValue())));
        assertThat("Empty cache after initialization", cache.entrySet().isEmpty());
        assertThat(cache.getConfiguration().getEvictionMaxEntries(), is(equalTo(expectedEvictionMaxEntries)));
    }

    @Test
    public void createCacheWithoutEmbeddedCacheManagerConfiguration() throws Exception {
        performInfinispanCacheFactoryTests(false);
    }

    @Test
    public void createCacheWithEmbeddedCacheManagerConfiguration() throws Exception {
        performInfinispanCacheFactoryTests(true);
    }

    private final void performInfinispanCacheFactoryTests(final boolean useEmbeddedCacheManager) throws Exception {
        InfinispanEmbeddedCacheManagerFactoryBean cacheManagerFb = null;
        try {
            final InfinispanCacheFactoryBean cacheFb = new InfinispanCacheFactoryBean();
            assertThat(cacheFb.getObjectType().equals(Cache.class), is(true));
            assertThat(cacheFb.isSingleton(), is(true));
            if (useEmbeddedCacheManager) {
                cacheManagerFb = new InfinispanEmbeddedCacheManagerFactoryBean();
                cacheManagerFb.setConfigLocation(new ClassPathResource("test-infinispan-config.xml", this.getClass()));
                cacheManagerFb.afterPropertiesSet();
                cacheFb.setEmbeddedCacheManager(cacheManagerFb.getObject());
            }
            cacheFb.setCacheName("testCache1");
            cacheFb.afterPropertiesSet();

            final Cache<?, ?> cache = cacheFb.getObject();
            final Configuration cacheConfig = cache.getConfiguration();
            assertThat(cache.getName(), is(equalTo("testCache1")));
            if (useEmbeddedCacheManager) {
                assertThat(cacheConfig.getEvictionMaxEntries(), is(equalTo(1000)));
            } else {
                assertThat(cacheConfig.getEvictionMaxEntries(), is(equalTo(-1)));
            }
        } finally {
            if (useEmbeddedCacheManager && cacheManagerFb != null) {
                cacheManagerFb.destroy();
            }
        }
    }
}
