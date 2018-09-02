package org.soraworld.locket.serializers;

import org.soraworld.hocon.node.Node;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public class ChatTypeSerializer implements TypeSerializer<ChatType> {
    public ChatType deserialize(@Nonnull Type type, @Nonnull Node node) {
        if (node instanceof NodeBase) {
            String value = ((NodeBase) node).getString();
            if (value != null) {
                if (value.equalsIgnoreCase("action_bar")) return ChatTypes.ACTION_BAR;
                if (value.equalsIgnoreCase("system")) return ChatTypes.SYSTEM;
            }
        }
        return ChatTypes.CHAT;
    }

    public Node serialize(@Nonnull Type type, ChatType chatType, @Nonnull Options options) {
        String value = chatType == ChatTypes.ACTION_BAR ? "action_bar" : chatType == ChatTypes.SYSTEM ? "system" : "chat";
        return new NodeBase(options, value, false);
    }

    @Nonnull
    public Type getRegType() {
        return ChatType.class;
    }
}
