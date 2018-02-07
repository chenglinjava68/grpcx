package net.jiaoqsh.grpcx.discovery;

import io.grpc.ServiceDescriptor;
import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("details")
public class InstanceDetails {

    private String description;

    private ServiceDescriptor serviceDescriptor;

    public InstanceDetails() {
        this("");
    }

    public InstanceDetails(String description) {
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(ServiceDescriptor serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }
}
