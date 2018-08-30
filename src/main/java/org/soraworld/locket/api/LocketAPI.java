package org.soraworld.locket.api;

import org.slf4j.Logger;
import org.soraworld.locket.config.LocketManager;
import org.soraworld.locket.constant.Perms;
import org.soraworld.locket.core.WrappedPlayer;
import org.soraworld.locket.data.LockSignData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocketAPI {




    public static LockSignData parseSign(Sign tile) {
        return null;
    }

    public static Text formatText(String line_2) {
        return null;
    }
}
