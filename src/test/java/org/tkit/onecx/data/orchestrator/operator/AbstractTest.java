package org.tkit.onecx.data.orchestrator.operator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;

import io.quarkus.test.Mock;
import io.smallrye.config.SmallRyeConfig;

public abstract class AbstractTest {

    public static class ConfigProducer {

        @Inject
        Config config;

        @Produces
        @ApplicationScoped
        @Mock
        DataConfig config() {
            return config.unwrap(SmallRyeConfig.class).getConfigMapping(DataConfig.class);
        }
    }
}
