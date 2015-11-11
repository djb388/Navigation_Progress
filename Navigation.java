package bytecodeNav;

/**
 * Navigation based on Jump Point Search (a flavor of A*) utilizing arrays of
 * bits to store the map in memory. This enables the use of efficient bit
 * manipulation to determine the number of trailing zeroes, leading zeroes,
 * trailing ones, and leading ones between any two points. The number of
 * trailing or leading zeroes represents the walkable distance from one point in
 * the direction of another point, and the number of trailing or leading ones
 * represents the number of non-walkable spaces from one point in the direction
 * to another point.
 *
 * Jump Point Search can be more efficient than vanilla A* when the cost of
 * creating, adding and removing nodes to a queue is high and the cost of
 * checking many spaces is low. In the case of Battlecode, it should be far more
 * efficient in terms of bytecode use.
 *
 * @author David
 */
 
import battlecode.common.*;

public class Navigation {

    public static void run(RobotController rc) {
    
        int[] start = new int[]{121, 121};   // Search from these coordinates
        int[] end = new int[]{133,133};     // Search to these coordinates

        Navigation n = new Navigation(128, 128, start[0], start[1]);

        // Populate two bit arrays based on a String representation of the map in the TestMaps class.
        map.mapX = TestMaps.getXMap();            // Bit array to represent rows.
        map.mapY = TestMaps.getYMap(map.mapX);  // Bit array to represent columns.

        int roundNum = Clock.getRoundNum();
        /*
        pathStart = getPath(start, end);// Search for a path from start to end.
        while (searching) {
            System.out.println("Bytecodes remaining: " + Clock.getBytecodesLeft());
            rc.yield();
            pathStart = resume();
        }
        
        pathNext = pathStart.mapNext;
        */
        while (nextPt[0] != end[0] && nextPt[1] != end[1]) {
            boolean moved = tryMove(end);
            if (moved) {
                
                System.out.println("nextPt: ("+ nextPt[0] +"," + nextPt[1]+")");
            }
            rc.yield();
        }
        /*
        if (pathStart != null) {
            JumpPoint path = new JumpPoint(pathStart.mapNext);
            int[] nextPt = new int[]{start[0],start[1]};
             
            while (path != null) {
                if (nextPt[0] == path.location[0] && nextPt[1] == path.location[1]) {
                    path = path.mapNext;
                }
                
                if (path == null) {
                    
                } else if (path.waypoint.length > 0) {
                    System.out.println("waypt: (" + path.waypoint[0] + ", " + path.waypoint[1] + ")");
                    nextPt = move(nextPt, directionTo(nextPt, path.location[0], path.location[1]));
                    if (nextPt[0] == path.waypoint[0] && nextPt[1] == path.waypoint[1]) {
                        path.waypoint = new int[]{};
                    }
                } else {
                    System.out.println("path:  (" + path.location[0] + ", " + path.location[1] + ")");
                    nextPt = move(nextPt, directionTo(nextPt, path.location[0], path.location[1]));
                }
            }
        }
        */
        
        
        //int pathLength = 0;
        //JumpPoint testPath = new JumpPoint(n.pathStart);
        //while(n.currentLoc.move(n.traversePath())) {
            //System.out.println("Current location: (" + n.currentLoc.location[0] + ", " + n.currentLoc.location[1] + ")");
        //    pathLength++;
        //}
        
        //System.out.println("Search started at round " + roundNum + "; path length: " + pathLength);
        // Print out the map with the jump points of the path marked by their directions of search.
        for (int i = 119; i < 137; i++) {
            for (int j = 119; j < 137; j++) {
                if ((((map.mapX[i][(j) / 64] >>> (63 - (j % 64))) & 1L) == 1)
                        && (((map.mapY[j][(i) / 64] >>> (63 - (i % 64))) & 1L) == 1)) {
                    System.out.print("X ");
                } else {
                    boolean notVisited = true;
                    if (pathStart != null) {
                        JumpPoint path = new JumpPoint(pathStart);

                        while (path != null) {
                            if (path.location[0] == j && path.location[1] == i) {
                                if (path.direction == 0) {
                                    System.out.print("F ");
                                } else if (path.mapLast == null) {
                                    System.out.print("S ");
                                } else {
                                    System.out.print(path.direction + " ");
                                }
                                notVisited = false;
                                break;
                            } else {
                                path = path.mapNext;
                            }
                        }
                    }
                    if (notVisited) {
                        System.out.print("- ");
                    }
                }
            }
            System.out.println();
        }
        
        

    }

    static Map map;    // Map object to hold the bit arrays and the origin.
    static Heap heap;  // Priority heap for the current search.
    static JumpPoint pathStart, pathNext, backToStart, returnTo;
    static int[] nextPt;
    static boolean searching, reachedGoal, pathChanged;
    public static final int bytecodeLimit = 2000;

    /**
     * Constructor. Origin will be shifted to the center of a 256x256 map to
     * prevent indices from going out of bounds for maps up to 128x128 bits
     * regardless of where the actual center of the map is.
     *
     * @param x coordinate of the origin of the map.
     * @param y coordinate of the origin of the map.
     * @param myX current location's x-coordinate.
     * @param myY current location's y-coordinate.
     */
    public Navigation(int x, int y, int myX, int myY) {
        map = new Map(x, y);
        nextPt = new int[]{myX,myY};
        reachedGoal = false;
        searching = false;
        pathChanged = false;
        pathStart = null;
        pathNext = null;
        returnTo = null;
        backToStart = null;
        heap = null;
    }
    
    public static boolean tryMove(int[] goal) {
        boolean moved;
        if (searching && heap.goal[0] == goal[0] && heap.goal[1] == goal[1]) {
            // Continue a previous search
            JumpPoint path = getPath(null,null);
            
            JumpPoint nextNew = path.mapNext;
            JumpPoint nextOld = pathStart.mapNext;
            if (nextNew != null) {
                while (nextNew.equals(nextOld) && !nextNew.equals(pathNext)) {
                    if (nextNew.mapNext == null) {
                        break;
                    }
                    nextNew = nextNew.mapNext;
                    nextOld = nextOld.mapNext;
                }
            }
            /**
             * Redo this, add a global "path" JumpPoint variable and modify it as a linear graph from current location to current path end.
             */
            if (nextNew != null && nextNew.equals(pathNext)) {
                // Path already traversed is also part of the new path.
                pathNext = nextNew;
                pathStart = path;
            } else if (nextNew != null) {
                nextNew = nextNew.mapLast;
            }
            pathStart = path;
            pathNext = path.mapNext;
            
        } else if (!reachedGoal) {
            pathStart = getPath(nextPt,goal);
            pathNext = pathStart.mapNext;
        }
        
        if (pathStart != null && pathNext != null) {
            if (pathNext.equals(nextPt)) {
                pathNext = pathNext.mapNext;
            }
            
            if (pathNext == null) {
                moved = false;
            } else if (pathNext.wx >= 0) {
                nextPt = move(nextPt, directionTo(nextPt, pathNext.x, pathNext.y));
                if (nextPt[0] == pathNext.wx && nextPt[1] == pathNext.wy) {
                    pathNext.wx = -1;
                }
                moved = true;
            } else {
                nextPt = move(nextPt, directionTo(nextPt, pathNext.x, pathNext.y));
                moved = true;
            }
        } else {
           moved = false;
        }
        
        return moved;
    }
    
    public static int directionTo(int[] location, int x, int y) {
        int nextDirection;
        x = location[0] - x;
        y = location[1] - y;
        boolean dx = x <= 0;
        boolean dy = y <= 0;
        if(dx && dy) {
            if (x == 0) {
                if (y == 0) {
                    nextDirection = -1;
                } else {
                    nextDirection = 2;
                }
            } else if (y == 0) {
                nextDirection = 4;
            } else {
                nextDirection = 3;
            }
        } else if (dx) {
            if (x == 0) {
                nextDirection = 0;
            } else {
                nextDirection = 1;
            }
        } else if (dy) {
            if (y == 0) {
                nextDirection = 6;
            } else {
                nextDirection = 5;
            }
        } else {
            nextDirection = 7;
        }
        return nextDirection;
    }
    
    public static int[] move(int[] location, int direction) {
        switch(direction) {
            case 0:
                location[1]--;
                break;
            case 1:
                location[0]++;
                location[1]--;
                break;
            case 2:
                location[0]++;
                break;
            case 3:
                location[0]++;
                location[1]++;
                break;
            case 4:
                location[1]++;
                break;
            case 5:
                location[0]--;
                location[1]++;
                break;
            case 6:
                location[0]--;
                break;
            case 7:
                location[0]--;
                location[1]--;
                break;
        }
        return location;
    }
    
    /**
     * Resume the previous search.
     *
     * @return
     */
    public static JumpPoint resume() {
        return getPath(null, null);
    }

    /**
     * Search for a path from point a to point b.
     *
     * @param a start of the path.
     * @param b end of the path.
     * @return the JumpPoint object at the start of this path, or null if there
     * is no path.
     */
    public static JumpPoint getPath(int[] a, int[] b) {
        searching = true;
        if (a != null) {
            heap = new Heap(a, b);
            reachedGoal = false;
            pathStart = null;
            pathNext = null;
            returnTo = null;
            backToStart = null;
        }

        while (heap.size != 0) {
            JumpPoint next = heap.remove();
            // current position on the diagonal of the next Node
            int[] location = new int[]{next.x, next.y};

            // distances from location to next void (x = step[0], y = step[1])
            int[] step = new int[2];

            // previous distances (x-1 = lastStep[0], y-1 = lastStep[1])
            int[] lastStep;

            int diagonalDistance = 0;

            switch (next.direction) {    // determine the direction of movement

                case 1: // direction = NE

                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]{
                        NavTools.distanceRight(location[0], location[1], map.mapX),
                        NavTools.distanceLeft(location[1], location[0], map.mapY)
                    };

                    try {   // Prevent location from going out of map's bounds
                        while (lastStep[0] != 0 && Clock.getBytecodesLeft() > bytecodeLimit && diagonalDistance < 10) { // while the current location is walkable
                            int time1 = Clock.getBytecodesLeft();
                            // check if the goal is directly reachable from this location
                            if (heap.goal[0] == location[0]) {
                                if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (heap.goal[1] == location[1]) {
                                if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            step[0] = NavTools.distanceRight(++location[0], --location[1], map.mapX);
                            step[1] = NavTools.distanceLeft(location[1], location[0], map.mapY);

                            // difference between last and current distances
                            int dX = step[0] - lastStep[0]; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(location[0] + lastStep[0] - 1, location[1] + 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    int distance = voids + lastStep[0] - 2;
                                    // new direction is SE
                                    heap.insert(new JumpPoint(location[0] + distance, location[1], location[0], location[1], next, 3, distance + diagonalDistance + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(location[0] + step[0], location[1], map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + step[0] - 1;
                                    heap.insert(new JumpPoint(location[0] + distance, location[1] + 1, location[0] - 1, location[1] + 1, next, 1, distance + diagonalDistance + next.distance));
                                }
                            }

                            int dY = step[1] - lastStep[1]; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(location[1] - lastStep[1] + 1, location[0] - 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE, need two waypoints
                                    int distance = voids + lastStep[1] - 2;
                                    heap.insert(new JumpPoint(location[0], location[1] - distance, location[0], location[1], next, 7, distance + diagonalDistance + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(location[1] - step[1], location[0], map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW, need two waypoints
                                    int distance = voids + step[1] - 1;
                                    heap.insert(new JumpPoint(location[0] - 1, location[1] - distance, location[0] - 1, location[1] + 1, next, 1, distance + diagonalDistance + next.distance));
                                }
                            }

                            lastStep[0] = step[0];
                            lastStep[1] = step[1];
                            diagonalDistance++;
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }

                case 3: // direction = SE
                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]{
                        NavTools.distanceRight(location[0], location[1], map.mapX),
                        NavTools.distanceRight(location[1], location[0], map.mapY)
                    };

                    try {   // Prevent location from going out of map's bounds
                        while (lastStep[0] != 0 && Clock.getBytecodesLeft() > bytecodeLimit && diagonalDistance < 10) { // while the current location is walkable
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (heap.goal[0] == location[0]) {
                                if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (heap.goal[1] == location[1]) {
                                if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            step[0] = NavTools.distanceRight(++location[0], ++location[1], map.mapX);
                            step[1] = NavTools.distanceRight(location[1], location[0], map.mapY);

                            // difference between last and current distances
                            int dX = step[0] - lastStep[0]; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(location[0] + lastStep[0] - 1, location[1] - 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStep[0] - 2;
                                    heap.insert(new JumpPoint(location[0] + distance, location[1], location[0], location[1], next, 1, distance + diagonalDistance + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of X
                                int voids = NavTools.distanceVoidRight(location[0] + step[0], location[1], map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + step[0] - 1;
                                    heap.insert(new JumpPoint(location[0] + distance, location[1] - 1, location[0] - 1, location[1] - 1, next, 3, distance + diagonalDistance + next.distance));
                                }
                            }

                            int dY = step[1] - lastStep[1]; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(location[1] + lastStep[1] - 1, location[0] - 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStep[1] - 2;
                                    heap.insert(new JumpPoint(location[0], location[1] + distance, location[0], location[1], next, 5, distance + diagonalDistance + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(location[1] + step[1], location[0], map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW
                                    int distance = voids + step[1] - 1;
                                    heap.insert(new JumpPoint(location[0] - 1, location[1] + distance, location[0] - 1, location[1] - 1, next, 3, distance + diagonalDistance + next.distance));
                                }
                            }

                            lastStep[0] = step[0];
                            lastStep[1] = step[1];
                            diagonalDistance++;
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }

                case 5: // direction = SW
                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]{
                        NavTools.distanceLeft(location[0], location[1], map.mapX),
                        NavTools.distanceRight(location[1], location[0], map.mapY)
                    };

                    try {   // Prevent location from going out of map's bounds
                        while (lastStep[0] != 0 && Clock.getBytecodesLeft() > bytecodeLimit && diagonalDistance < 10) { // while the current location is walkable
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (heap.goal[0] == location[0]) {
                                if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (heap.goal[1] == location[1]) {
                                if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            step[0] = NavTools.distanceLeft(--location[0], ++location[1], map.mapX);
                            step[1] = NavTools.distanceRight(location[1], location[0], map.mapY);

                            // difference between last and current distances
                            int dX = step[0] - lastStep[0]; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(location[0] - lastStep[0] + 1, location[1] - 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is NW
                                    int distance = voids + lastStep[0] - 2;
                                    heap.insert(new JumpPoint(location[0] - distance, location[1], location[0], location[1], next, 7, distance + diagonalDistance + next.distance));
                                }

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(location[0] - step[0], location[1], map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + step[0] - 1;
                                    heap.insert(new JumpPoint(location[0] - distance, location[1] - 1, location[0] + 1, location[1] - 1, next, 5, distance + diagonalDistance + next.distance));
                                }
                            }

                            int dY = step[1] - lastStep[1]; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(location[1] + lastStep[1] - 1, location[0] + 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE, need two waypoints
                                    int distance = voids + lastStep[1] - 2;
                                    heap.insert(new JumpPoint(location[0], location[1] + distance, location[0], location[1], next, 3, distance + diagonalDistance + next.distance));
                                }

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location + distanceRight of Y
                                int voids = NavTools.distanceVoidRight(location[1] + step[1], location[0], map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW, need two waypoints
                                    int distance = voids + step[1] - 1;
                                    heap.insert(new JumpPoint(location[0] + 1, location[1] + distance, location[0] + 1, location[1] - 1, next, 5, distance + diagonalDistance + next.distance));
                                }
                            }

                            lastStep[0] = step[0];
                            lastStep[1] = step[1];
                            diagonalDistance++;
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }

                default: // direction = NW
                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]{
                        NavTools.distanceLeft(location[0], location[1], map.mapX),
                        NavTools.distanceLeft(location[1], location[0], map.mapY)
                    };
                    try {   // Prevent location from going out of map's bounds
                        while (lastStep[0] != 0 && Clock.getBytecodesLeft() > bytecodeLimit && diagonalDistance < 10) { // while the current location is walkable
                            int time1 = Clock.getBytecodesLeft();

                            // check if the goal is directly reachable from this location
                            if (heap.goal[0] == location[0]) {
                                if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            } else if (heap.goal[1] == location[1]) {
                                if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                    next.mapNext = new JumpPoint(heap.goal[0], heap.goal[1], location[0], location[1], next, 0, 0, 0);
                                    next.mapNext.mapLast = next;
                                    searching = false;
                                    reachedGoal = true;
                                    return heap.retrace(next);
                                }
                            }

                            // step = distances to x and y voids & properly increment/decrement location
                            step[0] = NavTools.distanceLeft(--location[0], --location[1], map.mapX);
                            step[1] = NavTools.distanceLeft(location[1], location[0], map.mapY);

                            // difference between last and current distances
                            int dX = step[0] - lastStep[0]; // should be -1

                            if (dX >= 0) { // if x difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(location[0] - lastStep[0] + 1, location[1] + 1, map.mapX);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dX - voids >= -1) {
                                    // new direction is NW
                                    int distance = voids + lastStep[0] - 2;
                                    heap.insert(new JumpPoint(location[0] - distance, location[1], location[0], location[1], next, 5, distance + diagonalDistance + next.distance));

                            } else if (dX < -1) { // if x difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of X
                                int voids = NavTools.distanceVoidLeft(location[0] - step[0], location[1], map.mapX);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dX + voids < 0) {
                                    // new direction is NE
                                    int distance = voids + step[0] - 1;
                                    heap.insert(new JumpPoint(location[0] - distance, location[1] + 1, location[0] + 1, location[1] + 1, next, 7, distance + diagonalDistance + next.distance));
                            }

                            int dY = step[1] - lastStep[1]; // should be -1

                            if (dY >= 0) { // if y difference is greater than -1

                                // check the number of consecutive voids at previous location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(location[1] - lastStep[1] + 1, location[0] + 1, map.mapY);

                                // if the new difference is -1 or greater, add a new Node to the queue
                                if (dY - voids >= -1) {
                                    // new direction is SE
                                    int distance = voids + lastStep[1] - 2;
                                    heap.insert(new JumpPoint(location[0], location[1] - distance, location[0], location[1], next, 1, distance + diagonalDistance + next.distance));

                            } else if (dY < -1) { // if y difference is less than -1

                                // check the number of consecutive voids at current location - distanceLeft of Y
                                int voids = NavTools.distanceVoidLeft(location[1] - step[1], location[0], map.mapY);

                                // if the new difference is less than 0, add a new Node to the queue
                                if (dY + voids < 0) {
                                    // new direction is NW
                                    int distance = voids + step[1] - 1;
                                    heap.insert(new JumpPoint(location[0] + 1, location[1] - distance, location[0] + 1, location[1] + 1,  next, 7, distance + diagonalDistance + next.distance));
                            }

                            lastStep[0] = step[0];
                            lastStep[1] = step[1];
                            diagonalDistance++;
                            
                            time1 -= Clock.getBytecodesLeft();
                            System.out.println(time1);
                        }
                        break;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        break;
                    }
            }
            
                    
            if (lastStep[0] != 0) {
                heap.insert(new JumpPoint(location[0],location[1], -1, -1, next, next.direction,diagonalDistance+next.distance));
                if (Clock.getBytecodesLeft() < bytecodeLimit + 500) {
                    JumpPoint out = heap.retrace(next);
                    
                    return out;
                }
            }
            
        }
        searching = false;
        return null;
    }
}
