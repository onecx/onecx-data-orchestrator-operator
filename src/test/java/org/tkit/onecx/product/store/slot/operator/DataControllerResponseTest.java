package org.tkit.onecx.product.store.slot.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tkit.onecx.product.store.slot.operator.client.DataService;

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
        assertThat(result.getResource().getStatus()).isNotNull();
        assertThat(result.getResource().getStatus().getStatus()).isNotNull().isEqualTo(DataStatus.Status.UNDEFINED);

    }
}
