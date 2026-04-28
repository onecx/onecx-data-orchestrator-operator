package org.tkit.onecx.data.orchestrator.operator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataControllerFilterTest extends AbstractTest {

    @Test
    void testUpdateFilter() {
        var n = new Data();
        n.setSpec(null);

        var o = new Data();
        o.setStatus(null);

        var f = new DataController.UpdateFilter();
        assertThat(f.accept(n, o)).isTrue();
    }
}
