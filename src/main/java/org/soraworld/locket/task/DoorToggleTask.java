package org.soraworld.locket.task;

import org.soraworld.locket.api.LocketAPI;
import org.spongepowered.api.world.Location;

import java.util.List;

public class DoorToggleTask implements Runnable {

    private List<Location> doors;

    public DoorToggleTask(List<Location> doors_) {
        doors = doors_;
    }

    @Override
    public void run() {
        //LocketteAPI.toggleDoor(doorBottom, open);
        doors.stream().filter(LocketAPI::isDoubleDoorBlock).forEach(door -> {
            Location doorBottom = LocketAPI.getBottomDoorBlock(door);
            //LocketteAPI.toggleDoor(doorBottom, open);
            LocketAPI.toggleDoor(doorBottom);
        });
    }

}
