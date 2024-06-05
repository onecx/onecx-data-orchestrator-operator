package org.tkit.onecx.data.orchestrator.operator.client;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import org.tkit.quarkus.log.rs.RestClientLogInterceptor;
import org.tkit.quarkus.log.rs.RestClientPayloadInterceptor;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.jwt.util.KeyUtils;

@RegisterForReflection(targets = {
        RestClientLogInterceptor.class,
        RestClientPayloadInterceptor.class,
})
class KeyFactory {

    static final PrivateKey PRIVATE_KEY = createKey();

    static PrivateKey createKey() {
        return createKey(new KeyFactory());
    }

    static PrivateKey createKey(KeyFactory kf) {
        try {
            return kf.createPrivateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new KeyFactoryException(ex);
        }
    }

    PrivateKey createPrivateKey() throws NoSuchAlgorithmException {
        return KeyUtils.generateKeyPair(2048).getPrivate();
    }

    public static class KeyFactoryException extends RuntimeException {

        public KeyFactoryException(Throwable ex) {
            super(ex);
        }
    }

}
