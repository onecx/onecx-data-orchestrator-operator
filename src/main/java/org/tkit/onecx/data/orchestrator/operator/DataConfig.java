package org.tkit.onecx.data.orchestrator.operator;

import java.util.Map;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigDocFilename("onecx-data-orchestrator-operator.adoc")
@ConfigMapping(prefix = "onecx.data-orchestrator")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface DataConfig {

    /**
     * Digest algorithms.
     */
    @WithName("digest")
    @WithDefault("MD5")
    String digest();

    /**
     * Token configuration.
     */
    @WithDefault("token")
    TokenConfig token();

    /**
     * Client configuration.
     */
    @WithDefault("client")
    ConfigClient client();

    /**
     * Leader election configuration
     */
    @WithName("leader-election")
    LeaderElectionConfig leaderElectionConfig();

    /**
     * Client configuration.
     */
    interface ConfigClient {

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

    /**
     * Token configuration.
     */
    interface TokenConfig {

        /**
         * Username for rest call.
         */
        @WithName("user-name")
        @WithDefault("data-orchestrator-operator")
        String userName();

        /**
         * Token header parameter.
         */
        @WithName("header-param")
        @WithDefault("apm-principal-token")
        String headerParam();

        /**
         * Token claim organization parameter.
         */
        @WithName("claim-organization-param")
        @WithDefault("orgId")
        String claimOrganizationParam();

        /**
         * Token claim organization parameter array.
         */
        @WithName("claim-organization-param-array")
        @WithDefault("false")
        boolean claimOrganizationParamArray();

    }

    /**
     * Leader election config
     */
    interface LeaderElectionConfig {

        /**
         * Lease name
         */
        @WithName("lease-name")
        @WithDefault("onecx-data-orchestrator-operator-lease")
        String leaseName();
    }
}
