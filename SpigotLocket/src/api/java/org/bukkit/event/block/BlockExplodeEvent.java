package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.util.List;

/**
 * The type Block explode event.
 *
 * @author Himmelt
 */
public abstract class BlockExplodeEvent extends Event {
    /**
     * Block list list.
     *
     * @return the list
     */
    public abstract List<Block> blockList();
}
