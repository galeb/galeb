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

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static com.google.common.hash.Hashing.md5;
import static com.google.common.hash.Hashing.murmur3_128;
import static com.google.common.hash.Hashing.sha256;
import static com.google.common.hash.Hashing.sipHash24;
import static org.assertj.core.api.Assertions.assertThat;

public class GuavaConsistentHashTest {

    private final Log logger = LogFactory.getLog(this.getClass());

    private final double numBackends = 10.0;

    @Test
    public void checkUniformDistribution() {
        final long samples = 100000L;
        final int rounds = 5;
        final double percentMarginOfError = 0.5;
        final int numKeys = 100;
        final Random random = new Random();

        for (int round = 0; round < rounds; round++) {
            logger.info(String.format("checkUniformDistribution - round %s: %d samples", round + 1, samples));

            for (final HashFunction hash: new HashFunction[]{md5(), murmur3_128(), sipHash24(), sha256()}) {
                long sum = 0L;
                final long initialTime = System.currentTimeMillis();
                for (Integer counter = 0; counter < samples; counter++) {
                    final int chosen = (int) (random.nextFloat() * (numKeys - Float.MIN_VALUE));
                    sum += Hashing.consistentHash(hash.hashInt(chosen), (int) numBackends);
                }

                final long finishTime = System.currentTimeMillis();

                final double result = (numBackends * (numBackends - 1) / 2.0) * (samples / numBackends);

                logger.info(String.format("-> checkUniformDistribution (%s): Time spent (ms): %d. NonUniformDistRatio (smaller is better): %.4f%%",
                        hash, finishTime - initialTime, Math.abs(100.0 * (result-sum) / result)));

                final double topLimit = sum * (1.0 + percentMarginOfError);
                final double bottomLimit = sum * (1.0 - percentMarginOfError);

                try {
                    assertThat(result).isGreaterThanOrEqualTo(bottomLimit).isLessThanOrEqualTo(topLimit);
                } catch (AssertionError e) {
                    logger.error("Error when testing " + hash);
                    throw e;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void checkIfChoiceIsConsistent() {
        int samples = 10000;
        int[] keys = new int[]{1 ,13};
        int[][] results = new int[][]{
                            // "numBackends" backends, two cases
                            new int[]{8, 0, 8, 4},
                            new int[]{0, 3, 5, 7},
                            // only one backend
                            new int[]{0, 0, 0, 0},
                            // two backends
                            new int[]{0, 0, 1, 0}
                          };
        final Map<HashFunction, Integer>[] hashs = new Map[]{
                ImmutableMap.of(md5(),         results[0][0],
                                sipHash24(),   results[0][1],
                                murmur3_128(), results[0][2],
                                sha256(),      results[0][3]),
                ImmutableMap.of(md5(),         results[1][0],
                                sipHash24(),   results[1][1],
                                murmur3_128(), results[1][2],
                                sha256(),      results[1][3]),
                ImmutableMap.of(md5(),         results[2][0],
                                sipHash24(),   results[2][1],
                                murmur3_128(), results[2][2],
                                sha256(),      results[2][3]),
                ImmutableMap.of(md5(),         results[3][0],
                                sipHash24(),   results[3][1],
                                murmur3_128(), results[3][2],
                                sha256(),      results[3][3])};
        IntStream.range(0, samples).forEach(x -> {
            hashs[x % 2].forEach((hash, result) ->
                assertThat(Hashing.consistentHash(hash.hashInt(keys[x % 2]), (int) numBackends)).isEqualTo(result));
            hashs[2].forEach((hash, result) ->
                    assertThat(Hashing.consistentHash(hash.hashInt(1), 1)).isEqualTo(result));
            hashs[3].forEach((hash, result) ->
                    assertThat(Hashing.consistentHash(hash.hashInt(1), 2)).isEqualTo(result));
        });
    }
}

