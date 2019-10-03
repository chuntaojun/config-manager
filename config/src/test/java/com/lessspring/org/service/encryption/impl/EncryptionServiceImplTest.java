package com.lessspring.org.service.encryption.impl;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

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
public class EncryptionServiceImplTest {

    private EncryptionServiceImpl.PlaceholderProcessor processor = new EncryptionServiceImpl.PlaceholderProcessor();

    private String token = "liaochuntao";

    // properties

    private String originTxt = "" +
            "my.name=ENC{LIAOCHUNTAO}\n" +
            "my.age=22\n" +
            "my.love=ENC{YUJIAWEI}\n" +
            "";

    private String finalTxt = "" +
            "my.name=LIAOCHUNTAO\n" +
            "my.age=22\n" +
            "my.love=YUJIAWEI\n" +
            "";

    @Test
    public void handle() {
        String encr = processor.encryption(originTxt, token);
        System.out.println(encr);
        String decr = processor.decryption(encr, token);
        System.out.println(decr);
        Assert.assertEquals(finalTxt, processor.decryption(decr, token));
    }

}