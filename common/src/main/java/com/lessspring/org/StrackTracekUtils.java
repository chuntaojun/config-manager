/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lessspring.org;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class StrackTracekUtils {

    public static String stackTrace(final Throwable t) {
        if (t == null) {
            return "";
        }

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(); final PrintStream ps = new PrintStream(out)) {
            t.printStackTrace(ps);
            ps.flush();
            return new String(out.toByteArray());
        } catch (final IOException ignored) {
            // ignored
        }
        return "";
    }

}