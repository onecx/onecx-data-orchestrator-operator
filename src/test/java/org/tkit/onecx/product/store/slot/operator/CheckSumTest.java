package org.tkit.onecx.product.store.slot.operator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CheckSumTest {

    @Test
    void wrongAlgoDigest() {
        Assertions.assertThrows(CheckSum.CheckSumException.class, () -> CheckSum.createDigest("WRONG_NAME"));
    }
}
