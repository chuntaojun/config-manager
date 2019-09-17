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
package com.lessspring.org.utils;

import com.lessspring.org.PathUtils;
import lombok.extern.slf4j.Slf4j;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
@Slf4j
public final class DiskUtils {

    public static String readFile(String path, String fileName) {
        String finalPath = PathUtils.finalPath(path);
        File file = openFile(finalPath, fileName);
        if (file.exists()) {
            try {
                BufferedSource bufferedSource = Okio.buffer(Okio.source(file));
                return bufferedSource.readByteString().string(StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("[DiskUtils readFile has Error] : {}", e.getMessage());
            }
        }
        return StringUtils.EMPTY;
    }

    public static boolean writeFile(String path, String fileName, byte[] content) {
        String finalPath = PathUtils.finalPath(path);
        File file = openFile(finalPath, fileName, true);
        try {
            BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
            bufferedSink.write(content);
            return true;
        } catch (IOException e) {
            log.error("[DiskUtils writeFile has Error] : {}", e.getMessage());
        }
        log.error("[DiskUtils writeFile has Error] : File does not exist");
        return false;
    }

    public static boolean deleteFile(String path, String fileName) {
        String finalPath = PathUtils.finalPath(path);
        File file = openFile(finalPath, fileName);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    private static File openFile(String path, String fileName) {
        return openFile(path, fileName, false);
    }

    private static File openFile(String path, String fileName, boolean rewrite) {
        File directory = new File(path);
        if (directory.isDirectory()) {
            boolean mkdirs = directory.mkdirs();
            log.debug("[DiskUtils openFile mkdirs] : result is : {}", mkdirs);
        }
        File file = new File(path, fileName);
        try {
            boolean create = true;
            if (file.exists()) {
                if (rewrite) {
                    file.delete();
                } else {
                    create = false;
                }
            }
            if (create) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

}
