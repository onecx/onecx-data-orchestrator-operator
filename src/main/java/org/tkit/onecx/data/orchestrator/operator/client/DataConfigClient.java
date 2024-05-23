package org.tkit.onecx.data.orchestrator.operator.client;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigDocFilename("onecx-data-orchestrator-operator.adoc")
@ConfigMapping(prefix = "onecx.data-orchestrator.client")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface DataConfigClient {

    /**
     * Set to true to share the HTTP client between REST clients.
     */
    @WithName("shared")
    @WithDefault("true")
    boolean shared();

    /**
     * The size of the rest client connection pool.
     */
    @WithName("connection-pool-size")
    @WithDefault("30")
    int connectionPoolSize();

    /**
     * Clients key configuration
     */
    @WithName("key")
    Map<String, String> keys();
}
