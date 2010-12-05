Spring Framework support for Infinispan
=======================================

This project provides basic support to configure Infinispans EmbeddedCacheManager and Cache
based on Spring Frameworks FactoryBeans.

InfinispanEmbeddedCacheManagerFactoryBean exposes an EmbeddedCacheManager. 
If no configuration location is set, the default initialization will be used.

InfinispanCacheFactoryBean exposes a Cache. An EmbeddedCacheManager can be set, which will be used to create the named Cache. 
If no EmbeddedCacheManager is provided, a default one will be created.

Currently, the only way to configure the Cache-instances is to create an InfinispanEmbeddedCacheManagerFactoryBean, 
provide the location of an Infinispan configuration file, which defines the named cache, inject the created EmbeddedCacheManager 
into an InfinispanCacheFactoryBean and retrieve the configured Cache-instance from this.
