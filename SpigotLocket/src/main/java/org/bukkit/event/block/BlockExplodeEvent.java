package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Himmelt
 */
public class BlockExplodeEvent extends Event {
    private List<Block> blocks = new ArrayList<>();
    private static final HandlerList handlers = new HandlerList();

    public List<Block> blockList() {
        return blocks;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
