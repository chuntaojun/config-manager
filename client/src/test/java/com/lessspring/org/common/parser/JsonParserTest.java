package com.lessspring.org.common.parser;

import com.lessspring.org.constant.ConfigType;
import com.lessspring.org.model.dto.ConfigInfo;
import com.lessspring.org.utils.GsonUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class JsonParserTest {

    private String json = "{\"code\":0,\"msg\":\"\",\"data\":{\"list\":[{\"yzUid\":8104649508,\"createdAt\":\"2019-11-07 13:16:47\",\"updatedAt\":\"2019-11-07 13:16:47\",\"validateStatus\":2,\"validateStatusText\":\"\\\\u65e0\\\\u9700\\\\u9a8c\\\\u8bc1\",\"cardId\":0,\"cardName\":\"-\",\"address\":\"\",\"areaCode\":null,\"area\":\"-\",\"gender\":\"\\\\u7537\",\"weixin\":\"\",\"name\":\"\\\\u6d4b\\\\u8bd5\\\\u5ba2\\\\u6237\\\\u5bfc\\\\u5165\\\\u652f\\\\u6301\\\\u8fd0\\\\u8425\",\"mobile\":\"18012121212\",\"points\":0,\"prepaidBalance\":0,\"identity_type\":0,\"external_card_no\":\"\"},{\"yzUid\":8104649609,\"createdAt\":\"2019-11-07 13:16:47\",\"updatedAt\":\"2019-11-07 13:16:47\",\"validateStatus\":2,\"validateStatusText\":\"\\\\u65e0\\\\u9700\\\\u9a8c\\\\u8bc1\",\"cardId\":0,\"cardName\":\"-\",\"address\":\"\",\"areaCode\":null,\"area\":\"-\",\"gender\":\"\\\\u7537\",\"weixin\":\"\",\"name\":\"\\\\u5ba2\\\\u6237\\\\u5bfc\\\\u5165\\\\u652f\\\\u6301\\\\u8fd0\\\\u8425\",\"mobile\":\"18909090909\",\"points\":0,\"prepaidBalance\":0,\"identity_type\":0,\"external_card_no\":\"\"},{\"yzUid\":691599564,\"createdAt\":\"2019-11-07 13:16:47\",\"updatedAt\":\"2019-11-07 13:16:47\",\"validateStatus\":2,\"validateStatusText\":\"\\\\u65e0\\\\u9700\\\\u9a8c\\\\u8bc1\",\"cardId\":0,\"cardName\":\"-\",\"address\":\"\",\"areaCode\":null,\"area\":\"-\",\"gender\":\"\\\\u7537\",\"weixin\":\"18868878525\",\"name\":\"\\\\u730e\\\\u9e70188688\",\"mobile\":\"18868878525\",\"points\":1234,\"prepaidBalance\":0,\"identity_type\":0,\"external_card_no\":\"\"},{\"yzUid\":8104649614,\"createdAt\":\"2019-11-07 13:16:47\",\"updatedAt\":\"2019-11-07 13:16:47\",\"validateStatus\":2,\"validateStatusText\":\"\\\\u65e0\\\\u9700\\\\u9a8c\\\\u8bc1\",\"cardId\":0,\"cardName\":\"-\",\"address\":\"\",\"areaCode\":null,\"area\":\"-\",\"gender\":\"\\\\u7537\",\"weixin\":\"\",\"name\":\"\\\\u5bfc\\\\u5165\\\\u7684\\\\u7b2c\\\\u56db\\\\u4e2a\\\\u4eba\",\"mobile\":\"18123238980\",\"points\":0,\"prepaidBalance\":0,\"identity_type\":0,\"external_card_no\":\"\"}],\"page\":1,\"pageSize\":10,\"total\":4}}";

    private ConfigInfo configInfo;

    @Before
    public void before() {
        configInfo = ConfigInfo.builder()
                .content(json)
                .type(ConfigType.JSON.getType())
                .build();
    }

    @Test
    public void json_parser_test() {
        JsonParser parser = new JsonParser();
        Map<String, Object> map = parser.toMap(configInfo);
        System.out.println(GsonUtils.toJson(map));
    }

}