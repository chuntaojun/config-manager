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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @since 0.0.1
 */
public final class InetUtils {

	private static boolean useOnlySiteLocalInterface = false;

	private static boolean preferHostnameOverIp = false;

	public static InetSocketAddress ALL_IP = new InetSocketAddress("0.0.0.0", 0);

	private static List<String> preferredNetworks = new CopyOnWriteArrayList<>();

	private static List<String> ignoredInterfaces = new CopyOnWriteArrayList<String>();

	private static Supplier<String> selfIpSupplier = () -> {
		String selfIp;
		useOnlySiteLocalInterface = Boolean
				.valueOf(PropertyUtils.getProperty(Constant.USE_ONLY_SITE_INTERFACES));

		List<String> networks = PropertyUtils
				.getPropertyList(Constant.PREFERRED_NETWORKS);
		for (String preferred : networks) {
			preferredNetworks.add(preferred);
		}

		List<String> interfaces = PropertyUtils
				.getPropertyList(Constant.IGNORED_INTERFACES);
		for (String ignored : interfaces) {
			ignoredInterfaces.add(ignored);
		}

		String configManagerIp = System.getProperty(Constant.CONFIG_MANAGER_SERVER_IP);
		if (StringUtils.isBlank(configManagerIp)) {
			configManagerIp = PropertyUtils.getProperty(Constant.IP_ADDRESS);
		}

		if (!StringUtils.isBlank(configManagerIp) && !isIP(configManagerIp)) {
			throw new RuntimeException("nacos address " + configManagerIp + " is not ip");
		}

		selfIp = configManagerIp;

		if (StringUtils.isBlank(selfIp)) {
			preferHostnameOverIp = Boolean
					.getBoolean(Constant.SYSTEM_PREFER_HOSTNAME_OVER_IP);

			if (!preferHostnameOverIp) {
				preferHostnameOverIp = Boolean.parseBoolean(
						PropertyUtils.getProperty(Constant.PREFER_HOSTNAME_OVER_IP));
			}

			if (preferHostnameOverIp) {
				InetAddress inetAddress = null;
				try {
					inetAddress = InetAddress.getLocalHost();
				}
				catch (UnknownHostException e) {
				}
				if (inetAddress.getHostName()
						.equals(inetAddress.getCanonicalHostName())) {
					selfIp = inetAddress.getHostName();
				}
				else {
					selfIp = inetAddress.getCanonicalHostName();
				}
			}
			else {
				selfIp = findFirstNonLoopbackAddress().getHostAddress();
			}
		}
		return selfIp;
	};

	public static String getSelfIp() {
		return selfIpSupplier.get();
	}

	public static InetAddress findFirstNonLoopbackAddress() {
		InetAddress result = null;

		try {
			int lowest = Integer.MAX_VALUE;
			for (Enumeration<NetworkInterface> nics = NetworkInterface
					.getNetworkInterfaces(); nics.hasMoreElements();) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp()) {
					if (ifc.getIndex() < lowest || result == null) {
						lowest = ifc.getIndex();
					}
					else if (result != null) {
						continue;
					}

					if (!ignoreInterface(ifc.getDisplayName())) {
						for (Enumeration<InetAddress> addrs = ifc
								.getInetAddresses(); addrs.hasMoreElements();) {
							InetAddress address = addrs.nextElement();
							if (address instanceof Inet4Address
									&& !address.isLoopbackAddress()
									&& isPreferredAddress(address)) {
								result = address;
							}
						}
					}
				}
			}
		}
		catch (IOException ex) {
		}

		if (result != null) {
			return result;
		}

		try {
			return InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
		}

		return null;
	}

	public static boolean isPreferredAddress(InetAddress address) {
		if (useOnlySiteLocalInterface) {
			final boolean siteLocalAddress = address.isSiteLocalAddress();
			if (!siteLocalAddress) {
			}
			return siteLocalAddress;
		}
		if (preferredNetworks.isEmpty()) {
			return true;
		}
		for (String regex : preferredNetworks) {
			final String hostAddress = address.getHostAddress();
			if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
				return true;
			}
		}

		return false;
	}

	public static boolean ignoreInterface(String interfaceName) {
		for (String regex : ignoredInterfaces) {
			if (interfaceName.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isIP(String str) {
		String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
		String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num + "$";
		return match(regex, str);
	}

	public static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}
}