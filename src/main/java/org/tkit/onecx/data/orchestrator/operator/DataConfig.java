package org.tkit.onecx.data.orchestrator.operator;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
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

}
