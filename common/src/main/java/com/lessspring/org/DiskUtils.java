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

import com.lessspring.org.server.utils.ByteUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class DiskUtils {

	private static Logger logger = Logger.getLogger("com.lessspring.org.DiskUtils");

	private final static String NO_SPACE_CN = "设备上没有空间";
	private final static String NO_SPACE_EN = "No space left on device";
	private final static String DISK_QUATA_CN = "超出磁盘限额";
	private final static String DISK_QUATA_EN = "Disk quota exceeded";

	private static final Charset charset = StandardCharsets.UTF_8;
	private static final CharsetDecoder decoder = charset.newDecoder();

	// Just for test

	public static Logger getLogger() {
		return logger;
	}

	public static String readFile(String path, String fileName) {
		String finalPath = PathUtils.finalPath(path);
		File file = openFile(finalPath, fileName);
		if (file.exists()) {
			return readFile(file);
		}
		return null;
	}

	public static String readFile(InputStream is) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			StringBuilder textBuilder = new StringBuilder();
			String lineTxt = null;
			while ((lineTxt = reader.readLine()) != null) {
				textBuilder.append(lineTxt);
			}
			return textBuilder.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	public static String readFile(File file) {
		try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
			StringBuilder text = new StringBuilder();
			ByteBuffer buffer = ByteBuffer.allocate(4096);
			CharBuffer charBuffer = CharBuffer.allocate(4096);
			while (fileChannel.read(buffer) != -1) {
				buffer.flip();
				decoder.decode(buffer, charBuffer, false);
				charBuffer.flip();
				while (charBuffer.hasRemaining()) {
					text.append(charBuffer.get());
				}
				buffer.clear();
				charBuffer.clear();
			}
			return text.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	public static byte[] readFileBytes(String path, String fileName) {
		String finalPath = PathUtils.finalPath(path);
		File file = openFile(finalPath, fileName);
		if (file.exists()) {
			String result = readFile(file);
			if (result != null) {
				return ByteUtils.toBytes(result);
			}
		}
		return null;
	}

	public static boolean writeFile(String path, String fileName, byte[] content) {
		String finalPath = PathUtils.finalPath(path);
		File file = openFile(finalPath, fileName, true);
		try (FileChannel fileChannel = new FileOutputStream(file).getChannel()) {
			ByteBuffer buffer = ByteBuffer.wrap(content);
			fileChannel.write(buffer);
			return true;
		}
		catch (IOException ioe) {
			if (ioe.getMessage() != null) {
				String errMsg = ioe.getMessage();
				if (NO_SPACE_CN.equals(errMsg) || NO_SPACE_EN.equals(errMsg)
						|| errMsg.contains(DISK_QUATA_CN)
						|| errMsg.contains(DISK_QUATA_EN)) {
					logger.warning("磁盘满，自杀退出");
					System.exit(0);
				}
			}
		}
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

	public static boolean deleteDir(String path) {
		try {
			FileUtils.deleteDirectory(new File(path));
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	public static File openFile(String path, String fileName) {
		return openFile(path, fileName, false);
	}

	public static File openFile(String path, String fileName, boolean rewrite) {
		File directory = new File(path);
		boolean mkdirs = true;
		if (!directory.exists()) {
			mkdirs = directory.mkdirs();
		}
		if (!mkdirs) {
			logger.warning("[DiskUtils] can't create directory");
			return null;
		}
		File file = new File(path, fileName);
		try {
			boolean create = true;
			if (!file.exists()) {
				file.createNewFile();
			}
			if (file.exists()) {
				if (rewrite) {
					file.delete();
				}
				else {
					create = false;
				}
			}
			if (create) {
				file.createNewFile();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	public static void compressDirectoryToZipFile(final String rootDir,
			final String sourceDir, final ZipOutputStream zos,
			final WritableByteChannel channel) throws IOException {
		final String dir = Paths.get(rootDir, sourceDir).toString();
		final File[] files = new File(dir).listFiles();
		assert files != null;
		for (final File file : files) {
			if (file.isDirectory()) {
				compressDirectoryToZipFile(rootDir,
						Paths.get(sourceDir, file.getName()).toString(), zos, channel);
			}
			else {
				zos.putNextEntry(
						new ZipEntry(Paths.get(sourceDir, file.getName()).toString()));
				try (final FileInputStream in = new FileInputStream(
						Paths.get(rootDir, sourceDir, file.getName()).toString())) {
					FileChannel fileChannel = in.getChannel();
					fileChannel.transferTo(0, fileChannel.size(), channel);
				}
			}
		}
	}

	public static void unzipFile(final String sourceFile, final String outputDir)
			throws IOException {
		try (final ZipInputStream zis = new ZipInputStream(
				new FileInputStream(sourceFile))) {
			ReadableByteChannel channel = Channels.newChannel(zis);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				final String fileName = zipEntry.getName();
				final File entryFile = new File(outputDir + File.separator + fileName);
				FileUtils.forceMkdir(entryFile.getParentFile());
				try (final FileOutputStream fos = new FileOutputStream(entryFile)) {
					FileChannel fileChannel = fos.getChannel();
					fileChannel.transferFrom(channel, 0, zipEntry.getSize());
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
	}

}
