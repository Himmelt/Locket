package org.soraworld.locket.config;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;

public class I18n {

    static HashMap<String, String> LANGUAGES;

    public static String format(String key) {
        if (LANGUAGES.containsKey(key)) return LANGUAGES.get(key);
        return key;
    }

    public static Text formatText(String key) {
        if (LANGUAGES.containsKey(key)) return TextSerializers.FORMATTING_CODE.deserialize(LANGUAGES.get(key));
        return Text.of(key);
    }

    public static Text formatText(String key, Object... args) {
        if (LANGUAGES.containsKey(key)) {
            return TextSerializers.FORMATTING_CODE.deserialize(String.format(LANGUAGES.get(key), args));
        }
        return Text.of(key);
    }
}
