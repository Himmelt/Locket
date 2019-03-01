package org.soraworld.locket.serializers;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.lang.reflect.Type;

public class TextSerializer extends TypeSerializer<Text, NodeBase> {
    public TextSerializer() throws SerializerException {
    }

    @NotNull
    public Text deserialize(@NotNull Type type, @NotNull NodeBase node) {
        return TextSerializers.FORMATTING_CODE.deserialize(node.toString());
    }

    @NotNull
    public NodeBase serialize(@NotNull Type type, @NotNull Text text, @NotNull Options options) {
        return new NodeBase(TextSerializers.FORMATTING_CODE.serialize(text));
    }
}
