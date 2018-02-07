package net.jiaoqsh.grpcx.discovery;

import io.grpc.ServiceDescriptor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ZookeeperAutoConfigurationTests.TestConfig.class, ZookeeperDiscoveryAutoConfiguration.class })
public class ZookeeperAutoConfigurationTests {

	@Autowired(required = false) CuratorFramework curator;

	@Autowired(required = false) ZookeeperDiscoveryClient discoveryClient;

	@Test
	public void should_successfully_inject_Curator_as_a_Spring_bean() {
		assertNotNull(this.curator);
	}

	@Test
	public void should_successfully_inject_DiscoveryClient_as_a_Spring_bean() {
		assertNotNull(this.discoveryClient);
	}

	@Test
	public void should_successfully_DiscoveryClient_addInstance() throws Exception {
		ServiceInstance<InstanceDetails> serviceInstance = this.discoveryClient.addInstance("test", ServiceDescriptor.newBuilder("test").build(), 6565);
		assertNotNull(serviceInstance);
	}

	@Test
	public void should_successfully_DiscoveryClient_getInstanceByRandomStrategy() throws Exception {
		ServiceInstance<InstanceDetails> serviceInstance = this.discoveryClient.getInstanceByRandomStrategy("test");
		assertNotNull(serviceInstance);
	}

	static class TestConfig {
		@Bean
		ZookeeperProperties zookeeperProperties(TestingServer testingServer) throws Exception {
			ZookeeperProperties properties = new ZookeeperProperties();
			properties.setConnectString(testingServer.getConnectString());
			return properties;
		}

		@Bean(destroyMethod = "close") TestingServer testingServer() throws Exception {
			return new TestingServer();
		}
	}
}
