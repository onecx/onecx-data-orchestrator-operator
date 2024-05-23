package org.tkit.onecx.product.store.slot.operator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DigestTest {

    @Test
    void wrongAlgoDigest() {
        Assertions.assertThrows(Digest.DigestException.class, () -> Digest.createDigest("WRONG_NAME"));
    }
}
