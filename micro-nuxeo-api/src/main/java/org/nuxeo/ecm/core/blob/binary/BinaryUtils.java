package org.nuxeo.ecm.core.blob.binary;

import com.google.common.collect.ImmutableMap;

public class BinaryUtils {
    public static final String MD5_DIGEST = "MD5";

    public static final String SHA1_DIGEST = "SHA-1";

    public static final String SHA256_DIGEST = "SHA-256";

    public static final int MD5_DIGEST_LENGTH = 32;

    public static final int SHA1_DIGEST_LENGTH = 40;

    public static final int SHA256_DIGEST_LENGTH = 64;

    private static ImmutableMap<Integer, String> DIGESTS_BY_LENGTH = new ImmutableMap.Builder<Integer, String>()//
            .put(MD5_DIGEST_LENGTH, MD5_DIGEST) //
            .put(SHA1_DIGEST_LENGTH, SHA1_DIGEST) //
            .put(SHA256_DIGEST_LENGTH, SHA256_DIGEST) //
            .build();

    public static String getDigestByLength(int length) {
        return DIGESTS_BY_LENGTH.get(length);
    }

}
