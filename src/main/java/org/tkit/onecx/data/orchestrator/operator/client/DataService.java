package org.tkit.onecx.data.orchestrator.operator.client;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.data.orchestrator.operator.Data;
import org.tkit.onecx.data.orchestrator.operator.DataSpec;

@ApplicationScoped
public class DataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    @Inject
    DataConfigClient config;

    public int updateData(Data data) {
        DataSpec spec = data.getSpec();
        var url = config.keys().get(spec.getKey());
        if (url == null) {
            log.warn("No URL defined for the key '{}', resource: {}", spec.getKey(), data.getMetadata().getName());
            throw new MissingKeyConfiguration(spec.getKey());
        }

        var client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, config.connectionPoolSize())
                .property(QuarkusRestClientProperties.NAME, spec.getKey())
                .property(QuarkusRestClientProperties.SHARED, config.shared())
                .build(DataClientApi.class);

        try (var response = client.updateDate(spec.getData())) {
            log.info("Update data response {}", response.getStatus());
            return response.getStatus();
        }
    }

    public static class MissingKeyConfiguration extends RuntimeException {

        public MissingKeyConfiguration(String key) {
            super("Missing configuration for key " + key);
        }
    }
}
