/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.router.tests.hostselectors;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.google.common.hash.Hashing.md5;
import static com.google.common.hash.Hashing.murmur3_128;
import static com.google.common.hash.Hashing.sha256;
import static com.google.common.hash.Hashing.sipHash24;
import static org.assertj.core.api.Assertions.assertThat;

public class GuavaConsistentHashTest {

    private final Log logger = LogFactory.getLog(this.getClass());

    private int numBackends = 10;

    @Test
    public void checkUniformDistribution() {
        final long samples = 100000L;
        final int rounds = 5;
        final double percentMarginOfError = 0.5;
        final int numKeys = 100;

        for (int round=0; round < rounds; round++) {
            logger.info(String.format("checkUniformDistribution - round %s: %d samples", round + 1, samples));

            for (final HashFunction hash: new HashFunction[]{md5(), murmur3_128(), sipHash24(), sha256()}) {
                long sum = 0L;
                final long initialTime = System.currentTimeMillis();
                for (Integer counter = 0; counter < samples; counter++) {
                    final int chosen = (int) (Math.random() * (numKeys - Float.MIN_VALUE));
                    sum += Hashing.consistentHash(hash.hashInt(chosen), numBackends);
                }

                final long finishTime = System.currentTimeMillis();

                final double result = (numBackends * (numBackends - 1) / 2.0) * (samples / numBackends);

                logger.info(String.format("-> checkUniformDistribution (%s): Time spent (ms): %d. NonUniformDistRatio (smaller is better): %.4f%%",
                        hash, finishTime - initialTime, Math.abs(100.0 * (result-sum) / result)));

                final double topLimit = sum * (1.0 + percentMarginOfError);
                final double bottomLimit = sum * (1.0 - percentMarginOfError);

                assertThat(result).isGreaterThanOrEqualTo(bottomLimit).isLessThanOrEqualTo(topLimit);
            }
        }
    }

    @Test
    public void checkIfChoiceIsConsistent() {
        int samples = 10000;
        Map<HashFunction, Integer> hashs = new HashMap<>();
        hashs.put(md5(), 8);
        hashs.put(sipHash24(), 0);
        hashs.put(murmur3_128(), 8);
        hashs.put(sha256(), 4);
        IntStream.range(0, samples).forEach(x ->
                hashs.forEach((hash, result) ->
                        assertThat(Hashing.consistentHash(hash.hashInt(1), numBackends)).isEqualTo(result)));
    }

    @Test
    public void checkIfChoiceIsConsistentWithOnlyOneBackend() {
        int samples = 10000;
        Map<HashFunction, Integer> hashs = new HashMap<>();
        hashs.put(md5(), 0);
        hashs.put(sipHash24(), 0);
        hashs.put(murmur3_128(), 0);
        hashs.put(sha256(), 0);
        IntStream.range(0, samples).forEach(x ->
                hashs.forEach((hash, result) ->
                        assertThat(Hashing.consistentHash(hash.hashInt(1), 1)).isEqualTo(result)));
    }

    @Test
    public void checkIfChoiceIsConsistentWithTwoBackends() {
        int samples = 10000;
        Map<HashFunction, Integer> hashs = new HashMap<>();
        hashs.put(md5(), 0);
        hashs.put(sipHash24(), 0);
        hashs.put(murmur3_128(), 1);
        hashs.put(sha256(), 0);
        IntStream.range(0, samples).forEach(x ->
            hashs.forEach((hash, result) ->
                    assertThat(Hashing.consistentHash(hash.hashInt(1), 2)).isEqualTo(result)));

    }

}

