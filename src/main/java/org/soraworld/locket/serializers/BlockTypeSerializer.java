package org.soraworld.locket.serializers;

import org.jetbrains.annotations.NotNull;
import org.soraworld.hocon.exception.HoconException;
import org.soraworld.hocon.exception.SerializerException;
import org.soraworld.hocon.node.NodeBase;
import org.soraworld.hocon.node.Options;
import org.soraworld.hocon.serializer.TypeSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;

import java.lang.reflect.Type;

public class BlockTypeSerializer extends TypeSerializer<BlockType, NodeBase> {
    public BlockTypeSerializer() throws SerializerException {
    }

    @NotNull
    public BlockType deserialize(@NotNull Type type, @NotNull NodeBase node) throws HoconException {
        BlockType blockType = Sponge.getRegistry().getType(BlockType.class, node.toString()).orElse(null);
        if (blockType == null) throw new SerializerException("Value:" + node.toString() + " CANT deserialize to valid BlockType !!");
        return blockType;
    }

    @NotNull
    public NodeBase serialize(@NotNull Type type, @NotNull BlockType blockType, @NotNull Options options) {
        return new NodeBase(options, blockType.getId());
    }
}
