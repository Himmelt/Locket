package org.soraworld.locket.task;

import org.bukkit.block.Block;
import org.soraworld.locket.api.LocketAPI;

import java.util.List;

public class DoorToggleTask implements Runnable {

    private List<Block> doors;

    public DoorToggleTask(List<Block> doors_) {
        doors = doors_;
    }

    @Override
    public void run() {
        //LocketteAPI.toggleDoor(doorBottom, open);
        doors.stream().filter(LocketAPI::isDoubleDoorBlock).forEach(door -> {
            Block doorBottom = LocketAPI.getBottomDoorBlock(door);
            //LocketteAPI.toggleDoor(doorBottom, open);
            LocketAPI.toggleDoor(doorBottom);
        });
    }

}
