package org.soraworld.locket.data;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Serializers {

    public static class ChatTypeSerializer implements TypeSerializer<ChatType> {
        @Override
        public ChatType deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return "action-bar".equals(value.getString("chat").toLowerCase()) ? ChatTypes.ACTION_BAR : ChatTypes.CHAT;
        }

        @Override
        public void serialize(TypeToken<?> type, ChatType obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.equals(ChatTypes.ACTION_BAR) ? "action-bar" : "chat");
        }
    }

    public static class TextSerializer implements TypeSerializer<Text> {

        @Override
        public Text deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            return TextSerializers.FORMATTING_CODE.deserialize(value.getString());
        }

        @Override
        public void serialize(TypeToken<?> type, Text obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(TextSerializers.FORMATTING_CODE.serialize(obj));
        }
    }
}
