package org.soraworld.locket.serializers;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;

import java.lang.reflect.Type;

public class ChatTypeSerializer extends TypeSerializer<ChatType, NodeBase> {
    public ChatTypeSerializer() throws SerializerException {
    }

    @NotNull
    public ChatType deserialize(@NotNull Type type, @NotNull NodeBase node) {
        String value = node.getString();
        if (value.equalsIgnoreCase("action_bar")) return ChatTypes.ACTION_BAR;
        if (value.equalsIgnoreCase("system")) return ChatTypes.SYSTEM;
        return ChatTypes.CHAT;
    }

    @NotNull
    public NodeBase serialize(@NotNull Type type, @NotNull ChatType chatType, @NotNull Options options) {
        String value = chatType == ChatTypes.ACTION_BAR ? "action_bar" : chatType == ChatTypes.SYSTEM ? "system" : "chat";
        return new NodeBase(options, value);
    }
}
