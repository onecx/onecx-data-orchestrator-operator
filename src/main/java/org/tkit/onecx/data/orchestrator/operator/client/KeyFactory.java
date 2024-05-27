package org.tkit.onecx.data.orchestrator.operator.client;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import io.smallrye.jwt.util.KeyUtils;

class KeyFactory {

    static PrivateKey PRIVATE_KEY = createKey();

    static PrivateKey createKey() {
        return createKey(new KeyFactory());
    }

    static PrivateKey createKey(KeyFactory kf) {
        try {
            return kf.createPrivateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    PrivateKey createPrivateKey() throws NoSuchAlgorithmException {
        return KeyUtils.generateKeyPair(2048).getPrivate();
    }

}
