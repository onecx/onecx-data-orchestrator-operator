package org.tkit.onecx.product.store.slot.operator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.xml.bind.DatatypeConverter;

public class Digest {

    private final MessageDigest digest;

    public Digest() {
        digest = createDigest("MD5");
    }

    protected static MessageDigest createDigest(String name) {
        try {
            return MessageDigest.getInstance(name);
        } catch (NoSuchAlgorithmException e) {
            throw new DigestException(e);
        }
    }

    public String create(String data) throws RuntimeException {
        byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toUpperCase();
    }

    public static class DigestException extends RuntimeException {

        public DigestException(Throwable e) {
            super("Error create MD5 from the file content", e);
        }
    }
}
