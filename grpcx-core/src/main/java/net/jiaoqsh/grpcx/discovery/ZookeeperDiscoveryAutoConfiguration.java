package net.jiaoqsh.grpcx.discovery;

import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ZookeeperDiscoveryAutoConfiguration {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperDiscoveryAutoConfiguration.class);

	@Autowired(required = false)
	private EnsembleProvider ensembleProvider;

	@Bean
	@ConditionalOnMissingBean
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework(RetryPolicy retryPolicy, ZookeeperProperties properties) throws Exception {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		if (this.ensembleProvider != null) {
			builder.ensembleProvider(this.ensembleProvider);
		} else {
			builder.connectString(properties.getConnectString());
		}
		CuratorFramework curator = builder.retryPolicy(retryPolicy).build();
		curator.start();
		log.debug("blocking until connected to zookeeper for {} {}", properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
		curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
		log.debug("connected to zookeeper");
		return curator;
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public ZookeeperDiscoveryClient zookeeperDiscoveryClient(CuratorFramework curator) throws Exception {
		return new ZookeeperDiscoveryClient(curator);
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(),
				properties.getMaxRetries(),
				properties.getMaxSleepMs());
	}


	@Configuration
	@ConditionalOnClass(Endpoint.class)
	protected static class ZookeeperHealthConfig {
		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("zookeeper")
		public ZookeeperHealthIndicator zookeeperHealthIndicator(CuratorFramework curator) {
			return new ZookeeperHealthIndicator(curator);
		}
	}
}
