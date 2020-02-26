package com.conf.org.constant;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 * @Created at 2019-12-01 16:19
 */
public enum ConfigType {
    JSON("json"),

    PROPERTIES("properties"),

    YAML("yaml"),

    YML("yml"),

    XML("xml"),

    TEXT("text")
    ;


    private String type;

    ConfigType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
