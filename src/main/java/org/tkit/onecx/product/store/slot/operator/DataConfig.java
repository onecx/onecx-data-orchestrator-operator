package org.tkit.onecx.product.store.slot.operator;

import io.quarkus.runtime.annotations.ConfigDocFilename;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.quarkus.runtime.annotations.StaticInitSafe;
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
