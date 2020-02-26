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
package com.conf.org;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class SerializerUtils {

	private static final SerializerUtils INSTANCE = new SerializerUtils();

	public static SerializerUtils getInstance() {
		return INSTANCE;
	}

	private KryoPool kryoPool;

	private SerializerUtils() {
		kryoPool = new KryoPool.Builder(new KryoFactory()).softReferences().build();
	}

	private class KryoFactory implements com.esotericsoftware.kryo.pool.KryoFactory {

		@Override
		public Kryo create() {
			Kryo kryo = new Kryo();
			kryo.setRegistrationRequired(false);
			kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
					new org.objenesis.strategy.StdInstantiatorStrategy()));
			return kryo;
		}

	}

	public <T> byte[] serialize(T data) {
		return kryoPool.run(kryo -> {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			Output output = new Output(byteArrayOutputStream);
			kryo.writeClassAndObject(output, data);
			output.close();
			return byteArrayOutputStream.toByteArray();
		});
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(byte[] data, Class<T> clazz) {
		return kryoPool.run(kryo -> {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			Input input = new Input(byteArrayInputStream);
			return (T) kryo.readClassAndObject(input);
		});
	}

}
