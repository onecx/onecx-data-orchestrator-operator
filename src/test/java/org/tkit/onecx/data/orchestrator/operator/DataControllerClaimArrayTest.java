package org.tkit.onecx.data.orchestrator.operator;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.config.SmallRyeConfig;

@QuarkusTest
class DataControllerClaimArrayTest extends AbstractTest {

    @Inject
    Operator operator;

    @Inject
    KubernetesClient client;

    @InjectMock
    DataConfig dataConfig;

    @Inject
    Config config;

    @BeforeAll
    public static void init() {
        Awaitility.setDefaultPollDelay(2, SECONDS);
        Awaitility.setDefaultPollInterval(2, SECONDS);
        Awaitility.setDefaultTimeout(5, SECONDS);
    }

    @BeforeEach
    void beforeEach() {
        var tmp = config.unwrap(SmallRyeConfig.class).getConfigMapping(DataConfig.class);
        Mockito.when(dataConfig.digest()).thenReturn(tmp.digest());

        var dt = Mockito.mock(DataConfig.TokenConfig.class);
        Mockito.when(dt.userName()).thenReturn(tmp.token().userName());
        Mockito.when(dt.headerParam()).thenReturn(tmp.token().headerParam());
        Mockito.when(dt.claimOrganizationParamArray()).thenReturn(true);
        Mockito.when(dt.claimOrganizationParam()).thenReturn(tmp.token().claimOrganizationParam());

        Mockito.when(dataConfig.token()).thenReturn(dt);

        Mockito.when(dataConfig.client()).thenReturn(tmp.client());
    }

    @Test
    void tesTokenClaimArrayParamTest() {

        String name = "test-array";
        DataSpec spec = new DataSpec();
        spec.setKey("workspace");
        spec.setDescription("description");
        spec.setData("{\"items\":[]}");
        spec.setOrgId("default");

        operator.start();

        Data data = new Data();
        data.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(client.getNamespace()).build());
        data.setSpec(spec);

        client.resource(data).serverSideApply();

        await().pollDelay(2, SECONDS).untilAsserted(() -> {
            DataStatus mfeStatus = client.resource(data).get().getStatus();
            assertThat(mfeStatus).isNotNull();
            assertThat(mfeStatus.getStatus()).isNotNull().isEqualTo(DataStatus.Status.CREATED);
        });
    }
}
