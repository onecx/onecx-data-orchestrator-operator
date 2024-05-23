package org.tkit.onecx.data.orchestrator.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("onecx.tkit.org")
public class Data extends CustomResource<DataSpec, DataStatus> implements Namespaced {
}
