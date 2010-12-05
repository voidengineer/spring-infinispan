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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * {@link FactoryBean} that creates an Infinispan {@link Cache} instance representing
 * a named cache within an Infinispan {@link EmbeddedCacheManager}
 *
 * <p>Note: Requires Infinispan 4.1 or higher
 *
 * @author <a href="soeren.chittka@gmail.com">Sören Chittka</a>
 *
 * @since 0.1
 * @see #setEmbeddedCacheManager(EmbeddedCacheManager)
 * @see InfinispanEmbeddedCacheManagerFactoryBean
 * @see Cache
 */
public class InfinispanCacheFactoryBean implements FactoryBean<Cache<?, ?>>, BeanNameAware, InitializingBean {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private EmbeddedCacheManager embeddedCacheManager;

    private String cacheName;

    private String beanName;

    private Cache<?, ?> cache;

    /**
     * Set an EmbeddedCacheManager from which to retrieve a named Cache instance.
     * If no EmbeddedCacheManager is set, a {@link DefaultCacheManager} will be created
     * based on Infinispan default initialization.
     *
     * <p>Note that in particular for persistent (Cache Store/Loader) caches, it is
     * advisable to properly handle the shutdown of the EmbeddedCacheManager: Set up
     * a separate InfinispanEmbeddedCacheManagerFactoryBean and pass a reference to
     * this bean property.
     * <p>A separate InfinispanEmbeddedCacheManagerFactoryBean is also necessary for loading
     * Infinispan configuration from a non-default config location.
     *
     * @see InfinispanEmbeddedCacheManagerFactoryBean
     * @see DefaultCacheManager#DefaultCacheManager()
     */
    public void setEmbeddedCacheManager(final EmbeddedCacheManager embeddedCacheManager) {
        this.embeddedCacheManager = embeddedCacheManager;
    }

    /**
     * Set a name for which to retrieve a named cache instance.
     * Default is the bean name of this InfinispanCacheFactoryBean.
     */
    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public void setBeanName(final String beanName) {
        this.beanName = beanName;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.cacheName == null) {
            this.cacheName = this.beanName;
        }

        if (this.embeddedCacheManager == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using default Infinispan DefaultCacheManager for cache '" + this.cacheName + "'");
            }
            this.embeddedCacheManager = new DefaultCacheManager();
        }
        this.cache = this.embeddedCacheManager.getCache(this.cacheName);
    }

    public Cache<?, ?> getObject() throws Exception {
        return this.cache;
    }

    public Class<?> getObjectType() {
        return this.cache == null ? Cache.class : this.cache.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
