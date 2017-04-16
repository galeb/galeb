/**
 *
 */

package io.galeb.router.client.hostselectors.consistenthash;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHash<T> {

    /** The {@link HashAlgorithm Hash Algorithm} */
    private HashAlgorithm               hashAlgorithm;

    /** The number of replicas. */
    private int                         numberOfReplicas;

    /** The circle. */
    private final SortedMap<Integer, T> circle  = new TreeMap<>();

    /**
     * Instantiates a new consistent hash.
     *
     * @param hashAlgorithm The {@link HashAlgorithm Hash Algorithm}
     * @param numberOfReplicas the number of replicas
     * @param nodes nodes collection
     */
    public ConsistentHash(HashAlgorithm hashAlgorithm, int numberOfReplicas,
            Collection<T> nodes) {
        this.hashAlgorithm = hashAlgorithm;
        this.numberOfReplicas = numberOfReplicas;

        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * Adds a node.
     *
     * @param node a node
     */
    public void add(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.put(hashAlgorithm.hash(node.toString() + i).asInt(), node);
        }
    }

    /**
     * Removes a node.
     *
     * @param node a node
     */
    public void remove(T node) {
        for (int i = 0; i < numberOfReplicas; i++) {
            circle.remove(hashAlgorithm.hash(node.toString() + i).asInt());
        }
    }

    /**
     * Gets a node.
     *
     * @param key a key
     * @return a node from cicle
     */
    public T get(String key) {
        if (circle.isEmpty()) {
            return null;
        }

        int hash= hashAlgorithm.hash(key).asInt();

        if (!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }

    /**
     * Rebuild the cicle.
     *
     * @param hashAlgorithm The {@link HashAlgorithm Hash Algorithm}
     * @param numberOfReplicas the number of replicas
     * @param nodes nodes collection
     */
    public void rebuild(HashAlgorithm hashAlgorithm, Integer numberOfReplicas,
            Collection<T> nodes) {
        if (hashAlgorithm!=null) {
            this.hashAlgorithm = hashAlgorithm;
        }
        if (numberOfReplicas!=null) {
            this.numberOfReplicas = numberOfReplicas;
        }

        circle.clear();
        for (T node : nodes) {
            add(node);
        }
    }

}
