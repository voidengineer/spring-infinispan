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
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * {@link FactoryBean} that exposes an Infinispan {@link EmbeddedCacheManager}
 * instance, configured from a specified config location.
 *
 * <p>If no config location is specified, an EmbeddedCacheManager will be created
 * without a configuration file (that is, default Infinispan initialization
 * - as defined in the Infinispan docs - will apply).
 *
 * <p>Setting up a separate InfinispanEmbeddedCacheManagerFactoryBean is also advisable
 * when using {@link InfinispanCacheFactoryBean}, as it cares for proper shutdown of the
 * EmbeddedCacheManager. InfinispanEmbeddedCacheManagerFactoryBean is also necessary for
 * loading Infinispan configuration from a non-default config location.
 *
 * <p>Note: Requires Infinispan 4.1 or higher
 *
 * @author <a href="soeren.chittka@gmail.com">Sören Chittka</a>
 * @since 0.1
 * @see InfinispanCacheFactoryBean
 * @see org.infinispan.manager.EmbeddedCacheManager
 */
public class InfinispanEmbeddedCacheManagerFactoryBean implements FactoryBean<EmbeddedCacheManager>, InitializingBean, DisposableBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private Resource configLocation;

    private EmbeddedCacheManager embeddedCacheManager;

    /**
     * Set the location of the Infinispan config file.
     * @see DefaultCacheManager#DefaultCacheManager(java.io.InputStream)
     */
    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void afterPropertiesSet() throws Exception {
        this.logger.info("Initializing Infinispan EmbeddedCacheManager");
        if (this.configLocation != null) {
            this.embeddedCacheManager = new DefaultCacheManager(this.configLocation.getInputStream());
        } else {
            this.embeddedCacheManager = new DefaultCacheManager();
        }
        this.embeddedCacheManager.start();
    }

    public EmbeddedCacheManager getObject() throws Exception {
        return this.embeddedCacheManager;
    }

    public Class<? extends EmbeddedCacheManager> getObjectType() {
        return this.embeddedCacheManager != null ? this.embeddedCacheManager.getClass() : EmbeddedCacheManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        this.logger.info("Shutting down Infinispan EmbeddedCacheManager");
        this.embeddedCacheManager.stop();
    }
}
