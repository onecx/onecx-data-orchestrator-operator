package org.tkit.onecx.data.orchestrator.operator.client;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.jwt.Claims;
import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.data.orchestrator.operator.Data;
import org.tkit.onecx.data.orchestrator.operator.DataConfig;
import org.tkit.onecx.data.orchestrator.operator.DataSpec;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DataService {

    private static final Logger log = LoggerFactory.getLogger(DataService.class);

    @Inject
    DataConfig config;

    public int updateData(Data data) {
        DataSpec spec = data.getSpec();
        var url = config.client().keys().get(spec.getKey());
        if (url == null) {
            log.warn("No URL defined for the key '{}', resource: {}", spec.getKey(), data.getMetadata().getName());
            throw new MissingKeyConfiguration(spec.getKey());
        }

        var token = createToken(data);
        var client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, config.client().connectionPoolSize())
                .property(QuarkusRestClientProperties.NAME, spec.getKey())
                .property(QuarkusRestClientProperties.SHARED, config.client().shared())
                .register(OidcClientRequestReactiveFilter.class)
                .clientHeadersFactory(new ReactiveClientHeadersFactory() {
                    @Override
                    public Uni<MultivaluedMap<String, String>> getHeaders(MultivaluedMap<String, String> incomingHeaders,
                            MultivaluedMap<String, String> clientOutgoingHeaders) {
                        MultivaluedMap<String, String> propagatedHeaders = new MultivaluedHashMap<>();
                        propagatedHeaders.putSingle(config.token().headerParam(), token);
                        return Uni.createFrom().item(propagatedHeaders);
                    }
                })
                .build(DataClientApi.class);

        try (var response = client.updateDate(spec.getData())) {
            log.info("Update data response {}", response.getStatus());
            return response.getStatus();
        }
    }

    private String createToken(Data data) {

        var userName = config.token().userName();
        var orgId = data.getSpec().getOrgId();

        JsonObjectBuilder claims = Json.createObjectBuilder();
        claims.add(Claims.preferred_username.name(), userName);
        claims.add(Claims.sub.name(), userName);
        if (orgId != null) {
            if (config.token().claimOrganizationParamArray()) {
                claims.add(config.token().claimOrganizationParam(), Json.createArrayBuilder().add(orgId));
            } else {
                claims.add(config.token().claimOrganizationParam(), orgId);
            }
        }
        return Jwt.claims(claims.build()).sign(KeyFactory.PRIVATE_KEY);
    }

    public static class MissingKeyConfiguration extends RuntimeException {

        public MissingKeyConfiguration(String key) {
            super("Missing configuration for key " + key);
        }
    }
}
