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

/**
 * <b>Provides simple consistent hash solution.</b>
 * <p>
 * Consistent hashing is based on mapping each object to a point on the
 * edge of a circle (or equivalently, mapping each object to a real angle).
 * <p>
 * This package provides classes who maps each available node to many
 * pseudo-randomly distributed points on the edge of the same circle.
 */
package io.galeb.router.client.hostselectors.consistenthash;