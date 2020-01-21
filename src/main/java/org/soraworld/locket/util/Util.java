package org.soraworld.locket.util;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Himmelt
 */
public final class Util {

    private static final char TRUE_COLOR_CHAR = '\u00A7';
    private static final String TRUE_COLOR_STRING = "\u00A7";
    public static final Pattern HIDE_UUID = Pattern.compile("(\u00A7[0-9a-f]){32}");

    public static Optional<UUID> parseUuid(String text) {
        Matcher matcher = HIDE_UUID.matcher(text);
        if (matcher.find()) {
            String hex = matcher.group().replace(TRUE_COLOR_STRING, "");
            if (hex.length() == 32) {
                long most = Long.parseUnsignedLong(hex.substring(0, 16), 16);
                long least = Long.parseUnsignedLong(hex.substring(16), 16);
                return Optional.of(new UUID(most, least));
            }
        }
        return Optional.empty();
    }

    public static String hideUuid(UUID uuid) {
        String text = uuid.toString().replace("-", "");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(TRUE_COLOR_CHAR).append(text.charAt(i));
        }
        return builder.toString();
    }
}
