/**
 *
 */

package io.galeb.router.consistenthash;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class HashAlgorithm {

    /**
     * The Enum HashType.
     */
    public enum HashType {

        /** It's not so bad, but is a little slow. */
        MD5,
        /** Slow. */
        //MURMUR3_128,
        /** Fast and reliable, but not so good for small keys. */
        MURMUR3_32,
        /** Super Fast, but with excessive collisions. Why this was released? */
        //GOOD_FAST_32,
        /** Unreliable. */
        //ADLER_32,
        /** Unreliable. */
        //CRC_32,
        /** Slow and Unreliable. */
        //SHA1,
        /** Reliable. Its a little slow, but not quite. */
        SHA256,
        /** Reliable, but very slow. */
        //SHA512,
        /** Fast and reliable. The best for small keys. */
        SIP24
    }

    /** The Constant HASH_TYPE_MAP. */
    private static final Map<String, HashType> HASH_TYPE_MAP = new HashMap<>();
    static {
        for (final HashType hash : EnumSet.allOf(HashType.class)) {
            HASH_TYPE_MAP.put(hash.toString(), hash);
        }
    }

    public static HashType hashTypeFromString(String hashTypeStr) {
        return HASH_TYPE_MAP.get(hashTypeStr);
    }

    /** The hash type. */
    private final HashType hashType;

    /** The hash code. */
    private HashCode hashCode;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s - hashType:%s", HashAlgorithm.class.getName(), hashType);
    }

    /**
     * Instantiates a new hash algorithm.
     *
     * @param hashType the hash type
     */
    public HashAlgorithm(HashType hashType) {
        this.hashType = hashType;
    }

    /**
     * Instantiates a new hash algorithm.
     *
     * @param hashTypeStr the hash type str
     */
    public HashAlgorithm(String hashTypeStr) {
        this.hashType = HASH_TYPE_MAP.containsKey(hashTypeStr) ? HashType.valueOf(hashTypeStr) : HashType.SIP24;
    }

    /**
     * Calc Hash.
     *
     * @param key the key
     * @return int hash
     */
    public HashAlgorithm hash(Object key) {
        HashFunction hashAlgorithm;
        switch (hashType) {
            case MD5:
                hashAlgorithm = Hashing.md5();
                break;
            case MURMUR3_32:
                hashAlgorithm = Hashing.murmur3_32();
                break;
            case SHA256:
                hashAlgorithm = Hashing.sha256();
                break;
            case SIP24:
                hashAlgorithm = Hashing.sipHash24();
                break;
            default:
                hashAlgorithm = Hashing.sipHash24();
                break;
        }
        if (key instanceof String) {
            hashCode = hashAlgorithm.newHasher().putString((String)key,Charsets.UTF_8).hash();
        } else if (key instanceof Long) {
            hashCode = hashAlgorithm.newHasher().putLong((Long)key).hash();
        } else {
            hashCode = hashAlgorithm.newHasher().hash();
        }
        return this;
    }

    /**
     * HashCode as int.
     *
     * @return the int
     */
    public int asInt() {
        return hashCode.asInt();
    }

    /**
     * HashCode as string.
     *
     * @return the string
     */
    public String asString() {
        return hashCode.toString();
    }

}

