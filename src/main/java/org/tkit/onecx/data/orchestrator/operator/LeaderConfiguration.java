package org.tkit.onecx.data.orchestrator.operator;

import jakarta.inject.Singleton;

import io.javaoperatorsdk.operator.api.config.LeaderElectionConfiguration;

@Singleton
public class LeaderConfiguration extends LeaderElectionConfiguration {

    public LeaderConfiguration(DataConfig config) {
        super(config.leaderElectionConfig().leaseName());
    }
}
