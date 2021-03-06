package com.lessspring.org.common.parser;

import com.lessspring.org.constant.ConfigType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.server.utils.GsonUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class JsonParserTest {

	private String json = "";

	private ConfigInfo configInfo;

	@Before
	public void before() {
		configInfo = ConfigInfo.builder().content(json).type(ConfigType.JSON.getType())
				.build();
	}

	@Test
	public void json_parser_test() {
		JsonParser parser = new JsonParser();
		Map<String, Object> map = parser.toMap(configInfo);
		System.out.println(GsonUtils.toJson(map));
	}

}