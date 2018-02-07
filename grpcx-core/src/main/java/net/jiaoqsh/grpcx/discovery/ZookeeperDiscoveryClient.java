package net.jiaoqsh.grpcx.discovery;

import com.google.common.collect.Maps;
import io.grpc.ServiceDescriptor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import sun.net.util.IPAddressUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * @file: ZookeeperDiscoveryClient
 * @author: jiaoqsh
 * @since: 2018/02/06
 */
public class ZookeeperDiscoveryClient implements Closeable {

    private static final String PATH = "/grpc";

    private CuratorFramework client;
    private ServiceDiscovery<InstanceDetails> serviceDiscovery;
    private Map<String, ServiceProvider<InstanceDetails>> providers;

    public ZookeeperDiscoveryClient(CuratorFramework client) throws Exception {
        this.client = client;
        providers = Maps.newHashMap();
        initServiceDiscovery();
    }

    protected void initServiceDiscovery() throws Exception {
        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(client).basePath(PATH).build();
        serviceDiscovery.start();
    }

    public ServiceInstance<InstanceDetails> addInstance(String serviceName, ServiceDescriptor descriptor, int port) throws Exception {
        InstanceDetails instanceDetails = new InstanceDetails(descriptor.toString());
        ServiceInstance<InstanceDetails> thisInstance = ServiceInstance.<InstanceDetails>builder()
                .address(InetAddress.getLocalHost().getHostAddress())
                .name(serviceName)
                .payload(instanceDetails)
                .port(port)
                .build();

        // if you mark your payload class with @JsonRootName the provided JsonInstanceSerializer will work
        JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer(InstanceDetails.class);

        serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
                .client(client)
                .basePath(PATH)
                .serializer(serializer)
                .thisInstance(thisInstance)
                .build();
        serviceDiscovery.start();
        return thisInstance;
    }

    public ServiceInstance<InstanceDetails> getInstanceByRandomStrategy(String serviceName) throws Exception {
        ServiceProvider<InstanceDetails> provider = providers.get(serviceName);
        if (provider == null) {
            provider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceName).providerStrategy(new RandomStrategy()).build();
            providers.put(serviceName, provider);
            provider.start();

            Thread.sleep(2500); // give the provider time to warm up - in a real application you wouldn't need to do this
        }
        ServiceInstance<InstanceDetails> instance = provider.getInstance();
        return instance;
    }

    @Override
    public void close() throws IOException {
        for (ServiceProvider<InstanceDetails> cache : providers.values()) {
            CloseableUtils.closeQuietly(cache);
        }
        CloseableUtils.closeQuietly(serviceDiscovery);
    }
}
