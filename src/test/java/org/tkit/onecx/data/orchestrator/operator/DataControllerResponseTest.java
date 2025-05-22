package org.tkit.onecx.data.orchestrator.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.tkit.onecx.data.orchestrator.operator.DataController.createDigest;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.data.orchestrator.operator.client.DataService;

import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataControllerResponseTest extends AbstractTest {

    @InjectMock
    DataService dataService;

    @Inject
    DataController controller;

    @BeforeEach
    void beforeAll() {
        Mockito.when(dataService.updateData(any())).thenReturn(404);
    }

    @Test
    void wrongAlgoDigestTest() {
        Assertions.assertThrows(DataController.CheckSumException.class, () -> createDigest("WRONG_NAME"));
    }

    @Test
    void testWrongResponse() throws Exception {

        var s = new DataSpec();
        s.setProductName("product");
        s.setAppId("m1");
        s.setKey("key1");
        s.setData("");

        var m = new Data();
        m.setSpec(s);

        UpdateControl<Data> result = controller.reconcile(m, null);
        assertThat(result).isNotNull();
        assertThat(result.getResource()).isNotNull();
        assertThat(result.getResource()).isPresent();
        assertThat(result.getResource().get().getStatus()).isNotNull();
        assertThat(result.getResource().get().getStatus().getStatus()).isNotNull().isEqualTo(DataStatus.Status.UNDEFINED);

    }
}
