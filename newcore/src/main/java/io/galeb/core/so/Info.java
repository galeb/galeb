/*
 * Copyright (c) 2014-2018 Globo.com - ATeam
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

package io.galeb.core.so;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Info {

    public static int getSOBacklog() {
        int tcp_max_syn_backlog = 1000;
        try {
            if (System.getProperty("os.name", "UNDEF").toLowerCase().startsWith("linux")) {
                final Path tcp_max_syn_backlog_file = Paths.get("/proc/sys/net/ipv4/tcp_max_syn_backlog");
                tcp_max_syn_backlog = Integer.parseInt(new String(Files.readAllBytes(tcp_max_syn_backlog_file), Charset.defaultCharset()));
            }
        } catch (IOException ignore) {}
        return tcp_max_syn_backlog;
    }

    public static int getPid() {
        final POSIX posix = POSIXFactory.getNativePOSIX();
        return posix.getpid();
    }
}
