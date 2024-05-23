package org.tkit.onecx.product.store.slot.operator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.xml.bind.DatatypeConverter;

import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.config.SmallRyeConfig;

public class CheckSum {

    private final MessageDigest digest;

    public CheckSum() {
        var config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class).getConfigMapping(DataConfig.class);
        digest = createDigest(config.digest());
    }

    protected static MessageDigest createDigest(String name) {
        try {
            return MessageDigest.getInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new CheckSumException(e);
        }
    }

    public String createCheckSum(String data) throws RuntimeException {
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toUpperCase();
    }

    public static class CheckSumException extends RuntimeException {

        public CheckSumException(Throwable e) {
            super("Error create check-sum from the data content", e);
        }
    }
}
