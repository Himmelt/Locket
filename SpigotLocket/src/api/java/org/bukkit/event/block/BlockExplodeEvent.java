package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.List;

/**
 * @author Himmelt
 */
public abstract class BlockExplodeEvent extends Event {
    public abstract List<Block> blockList();
}
