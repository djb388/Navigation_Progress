package navigation;

/**
 * Navigation based on Jump Point Search (a flavor of A*) utilizing arrays of 
 * bits to store the map in memory. This enables the use of efficient bit 
 * manipulation to determine the number of trailing zeroes, leading zeroes,
 * trailing ones, and leading ones between any two points. The number of 
 * trailing or leading zeroes represents the walkable distance from one point
 * in the direction of another point, and the number of trailing or leading ones
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
public class Navigation {
    public static void main(String[] args) {
        
        Navigation n = new Navigation(128,128);
   
        int[] start = new int[]{123,128};
        int[] end = new int[]{133,133};
        
        
        n.map.mapX = TestMaps.getXMap();
        n.map.mapY = TestMaps.getYMap(n.map.mapX);
        
        //Node pathStart = n.getPath(start, end);
        JumpPoint pathStart = n.getPath(start, end);
        
        for(int i = 119; i < 137; i++) {
            for(int j = 119; j < 137; j++) {
                if ((((n.map.mapX[i][(j)/64] >>> (63-(j%64))) & 1L) == 1)
                        && (((n.map.mapY[j][(i)/64] >>> (63-(i%64))) & 1L) == 1)) {
                    System.out.print("X ");
                } else {
                    boolean notVisited = true;
                    if (pathStart != null) {
                        //Node path = new Node(pathStart);
                        JumpPoint path = new JumpPoint(pathStart);
                        
                        while(path != null) {
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
    
    Map map;
    Heap heap;
    
    public Navigation(int x, int y) {
        map = new Map(x,y);
    }
    
    /**
     * Pause the current search and return a guess as to the direction of movement.
     * @return start of the path currently being explored.
     */
    public JumpPoint pause() {
        return heap.retrace(heap.points[1]);
    }
    
    /**
     * Resume the previous search.
     * @return 
     */
    public JumpPoint resume() {
        return getPath(null,null);
    }
    
    /**
     * Search an optimal best path from point a to point b.
     * @param a start of the path.
     * @param b end of the path.
     * @return the JumpPoint object at the start of this path, or null if there is no path.
     */
    public JumpPoint getPath(int[] a, int[] b) {
        
        if (a != null) {
            heap = new Heap(a,b);
        }
        
        while(heap.size != 0) {
            JumpPoint next = heap.remove();
            // current position on the diagonal of the next Node
            int[] location = new int[] { next.location[0], next.location[1] };
            
            // distances from location to next void (x = step[0], y = step[1])
            int[] step = new int[2];
            
            // previous distances
            int[] lastStep;
            
            int diagonalDistance = 0;
            
            switch(next.direction) {    // determine the direction of movement
                
                case 1: // direction = NE
                    
                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]
                    {
                        NavTools.distanceRight(location[0], location[1], map.mapX),
                        NavTools.distanceLeft(location[1], location[0], map.mapY)
                    };
                    
                    while (lastStep[0] != 0) { // while the current location is walkable
                        
                        // check if the goal is directly reachable from this location
                        if (heap.goal[0] == location[0]) {
                            if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        } else if (heap.goal[1] == location[1]) {
                            if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        }
                        
                        // step = distances to x and y voids & properly increment/decrement location
                        step[0] = NavTools.distanceRight(++location[0], --location[1], map.mapX);
                        step[1] = NavTools.distanceLeft(location[1], location[0], map.mapY);
                            
                        // difference between last and current distances
                        int dX = step[0] - lastStep[0]; // should be -1
                            
                        if (dX >= 0) { // if x difference is greater than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidRight(location[0]+lastStep[0]-1, location[1]+1, map.mapX);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dX - voids >= -1) {
                                int distance = voids+lastStep[0]-2;
                                // new direction is SE
                                heap.insert(new int[] {
                                            location[0]+distance,
                                            location[1]
                                        }, 
                                        3,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dX < -1) { // if x difference is less than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidRight(location[0]+step[0], location[1], map.mapX);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dX + voids < 0) {
                                // new direction is NE
                                int distance = voids+step[0]-1;
                                heap.insert(new int[] {
                                            location[0]+distance,
                                            location[1]+1
                                        }, 
                                        1,
                                        new int[] {
                                            location[0]-1,
                                            location[1]+1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        int dY = step[1] - lastStep[1]; // should be -1
                        
                        if (dY >= 0) { // if y difference is greater than -1
                                
                            // check the number of consecutive voids
                            int voids = NavTools.distanceVoidLeft(location[1]-lastStep[1]+1, location[0]-1, map.mapY);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dY - voids >= -1) {
                                // new direction is SE, need two waypoints
                                int distance = voids+lastStep[1]-2;
                                heap.insert(new int[] {
                                            location[0],
                                            location[1]-distance //-voids-lastStep[1]+2
                                        }, 
                                        7,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        },
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dY < -1) { // if y difference is less than -1
                                
                            // check the number of consecutive voids at
                            int voids = NavTools.distanceVoidLeft(location[1]-step[1], location[0], map.mapY);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dY + voids < 0) {
                                // new direction is NW, need two waypoints
                                int distance = voids+step[1]-1;
                                heap.insert(new int[] {
                                            location[0]-1,
                                            location[1]-distance //-voids-step[1]+1
                                        }, 
                                        1,
                                        new int[] {
                                            location[0]-1,
                                            location[1]+1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        lastStep[0] = step[0];
                        lastStep[1] = step[1];
                        diagonalDistance++;
                    }
                    break;
                    
                case 3: // direction = SE
                     // initialize as the distance from this node to x and y voids
                    lastStep = new int[]
                    {
                        NavTools.distanceRight(location[0], location[1], map.mapX),
                        NavTools.distanceRight(location[1], location[0], map.mapY)
                    };
                    
                    while (lastStep[0] != 0) { // while the current location is walkable
                        
                        // check if the goal is directly reachable from this location
                        if (heap.goal[0] == location[0]) {
                            if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        } else if (heap.goal[1] == location[1]) {
                            if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        }
                        
                        // step = distances to x and y voids & properly increment/decrement location
                        step[0] = NavTools.distanceRight(++location[0], ++location[1], map.mapX);
                        step[1] = NavTools.distanceRight(location[1], location[0], map.mapY);
                            
                        // difference between last and current distances
                        int dX = step[0] - lastStep[0]; // should be -1
                        
                        if (dX >= 0) { // if x difference is greater than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidRight(location[0]+lastStep[0]-1, location[1]-1, map.mapX);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dX - voids >= -1) {
                                // new direction is SE
                                int distance = voids+lastStep[0]-2;
                                heap.insert(new int[] {
                                            location[0]+distance,
                                            location[1]
                                        }, 
                                        1,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dX < -1) { // if x difference is less than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidRight(location[0]+step[0], location[1], map.mapX);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dX + voids < 0) {
                                // new direction is NE
                                int distance = voids+step[0]-1;
                                heap.insert(new int[] {
                                            location[0]+distance,
                                            location[1]-1
                                        }, 
                                        3,
                                        new int[] {
                                            location[0]-1,
                                            location[1]-1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        int dY = step[1] - lastStep[1]; // should be -1
                        
                        if (dY >= 0) { // if y difference is greater than -1
                                
                            // check the number of consecutive voids
                            int voids = NavTools.distanceVoidRight(location[1]+lastStep[1]-1, location[0]-1, map.mapY);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dY - voids >= -1) {
                                // new direction is SE
                                int distance = voids+lastStep[1]-2;
                                heap.insert(new int[] {
                                            location[0],
                                            location[1]+distance
                                        }, 
                                        5,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        },
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dY < -1) { // if y difference is less than -1
                                
                            // check the number of consecutive voids at
                            int voids = NavTools.distanceVoidRight(location[1]+step[1], location[0], map.mapY);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dY + voids < 0) {
                                // new direction is NW
                                int distance = voids+step[1]-1;
                                heap.insert(new int[] {
                                            location[0]-1,
                                            location[1]+distance
                                        }, 
                                        3,
                                        new int[] {
                                            location[0]-1,
                                            location[1]-1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        lastStep[0] = step[0];
                        lastStep[1] = step[1];
                        diagonalDistance++;
                    }
                    break;
                    
                case 5: // direction = SW
                     // initialize as the distance from this node to x and y voids
                    lastStep = new int[]
                    {
                        NavTools.distanceLeft(location[0], location[1], map.mapX),
                        NavTools.distanceRight(location[1], location[0], map.mapY)
                    };
                    
                    while (lastStep[0] != 0) { // while the current location is walkable
                        
                        // check if the goal is directly reachable from this location
                        if (heap.goal[0] == location[0]) {
                            if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        } else if (heap.goal[1] == location[1]) {
                            if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        }
                        
                        // step = distances to x and y voids & properly increment/decrement location
                        step[0] = NavTools.distanceLeft(--location[0], ++location[1], map.mapX);
                        step[1] = NavTools.distanceRight(location[1], location[0], map.mapY);
                            
                        // difference between last and current distances
                        int dX = step[0] - lastStep[0]; // should be -1
                            
                        if (dX >= 0) { // if x difference is greater than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidLeft(location[0]-lastStep[0]+1, location[1]-1, map.mapX);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dX - voids >= -1) {
                                // new direction is NW
                                int distance = voids+lastStep[0]-2;
                                heap.insert(new int[] {
                                            location[0]-distance,//-voids-lastStep[0]+2,
                                            location[1]
                                        }, 
                                        7,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dX < -1) { // if x difference is less than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidLeft(location[0]-step[0], location[1], map.mapX);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dX + voids < 0) {
                                // new direction is NE
                                int distance = voids+step[0]-1;
                                heap.insert(new int[] {
                                            location[0]-distance,//-voids-step[0]+1,
                                            location[1]-1
                                        }, 
                                        5,
                                        new int[] {
                                            location[0]+1,
                                            location[1]-1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        int dY = step[1] - lastStep[1]; // should be -1
                        
                        if (dY >= 0) { // if y difference is greater than -1
                                
                            // check the number of consecutive voids
                            int voids = NavTools.distanceVoidRight(location[1]+lastStep[1]-1, location[0]+1, map.mapY);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dY - voids >= -1) {
                                // new direction is SE, need two waypoints
                                int distance = voids+lastStep[1]-2;
                                heap.insert(new int[] {
                                            location[0],
                                            location[1]+distance
                                        }, 
                                        3,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        },
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dY < -1) { // if y difference is less than -1
                                
                            // check the number of consecutive voids at
                            int voids = NavTools.distanceVoidRight(location[1]+step[1], location[0], map.mapY);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dY + voids < 0) {
                                // new direction is NW, need two waypoints
                                int distance = voids+step[1]-1;
                                heap.insert(new int[] {
                                            location[0]+1,
                                            location[1]+distance
                                        }, 
                                        5,
                                        new int[] {
                                            location[0]+1,
                                            location[1]-1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        lastStep[0] = step[0];
                        lastStep[1] = step[1];
                        diagonalDistance++;
                    }
                    break;
                    
                default: // direction = NW
                    // initialize as the distance from this node to x and y voids
                    lastStep = new int[]
                    {
                        NavTools.distanceLeft(location[0], location[1], map.mapX),
                        NavTools.distanceLeft(location[1], location[0], map.mapY)
                    };
                    
                    while (lastStep[0] != 0) { // while the current location is walkable
                        
                        // check if the goal is directly reachable from this location
                        if (heap.goal[0] == location[0]) {
                            if (heap.goal[1] <= location[1] && heap.goal[1] >= location[1] - NavTools.distanceLeft(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[1] >= location[1] && heap.goal[1] <= location[1] + NavTools.distanceRight(location[1], location[0], map.mapY)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        } else if (heap.goal[1] == location[1]) {
                            if (heap.goal[0] <= location[0] && heap.goal[0] >= location[0] - NavTools.distanceLeft(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            } else if (heap.goal[0] >= location[0] && heap.goal[0] <= location[0] + NavTools.distanceRight(location[0], location[1], map.mapX)) {
                                next.mapNext = new JumpPoint(heap.goal,location,next,0,0,0);
                                next.mapNext.mapLast = next;
                                return heap.retrace(next);
                            }
                        }
                        
                        // step = distances to x and y voids & properly increment/decrement location
                        step[0] = NavTools.distanceLeft(--location[0], --location[1], map.mapX);
                        step[1] = NavTools.distanceLeft(location[1], location[0], map.mapY);
                            
                        // difference between last and current distances
                        int dX = step[0] - lastStep[0]; // should be -1
                            
                        if (dX >= 0) { // if x difference is greater than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidLeft(location[0]-lastStep[0]+1, location[1]+1, map.mapX);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dX - voids >= -1) {
                                // new direction is NW
                                int distance = voids+lastStep[0]-2;
                                heap.insert(new int[] {
                                            location[0]-distance,//-voids-lastStep[0]+2,
                                            location[1]
                                        }, 
                                        5,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dX < -1) { // if x difference is less than -1
                                
                            // check the number of consecutive voids at x-1+distanceRight, y-1
                            int voids = NavTools.distanceVoidLeft(location[0]-step[0], location[1], map.mapX);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dX + voids < 0) {
                                // new direction is NE
                                int distance = voids+step[0]-1;
                                heap.insert(new int[] {
                                            location[0]-distance,//-voids-step[0]+1,
                                            location[1]+1
                                        }, 
                                        7,
                                        new int[] {
                                            location[0]+1,
                                            location[1]+1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        int dY = step[1] - lastStep[1]; // should be -1
                        
                        if (dY >= 0) { // if y difference is greater than -1
                                
                            // check the number of consecutive voids
                            int voids = NavTools.distanceVoidLeft(location[1]-lastStep[1]+1, location[0]+1, map.mapY);
                            
                            // if the new difference is greater than 0, add a new Node to the queue
                            if (dY - voids >= -1) {
                                // new direction is SE
                                int distance = voids+lastStep[1]-2;
                                heap.insert(new int[] {
                                            location[0],
                                            location[1]-distance//-voids-lastStep[1]+2
                                        }, 
                                        1,
                                        new int[] {
                                            location[0],
                                            location[1]
                                        },
                                        next,
                                        distance+diagonalDistance);
                            }
                            
                        } else if (dY < -1) { // if y difference is less than -1
                                
                            // check the number of consecutive voids at
                            int voids = NavTools.distanceVoidLeft(location[1]-step[1], location[0], map.mapY);
                            
                            // if the new difference is greater than or equal to 0, add a new Node to the queue
                            if (dY + voids < 0) {
                                // new direction is NW
                                int distance = voids+step[1]-1;
                                heap.insert(new int[] {
                                            location[0]+1,
                                            location[1]-distance//-voids-step[1]+1
                                        }, 
                                        7,
                                        new int[] {
                                            location[0]+1,
                                            location[1]+1
                                        }, 
                                        next,
                                        distance+diagonalDistance);
                            }   
                        }
                        
                        lastStep[0] = step[0];
                        lastStep[1] = step[1];
                        diagonalDistance++;
                    }
                    break;
            }
        }
        return null;
    }
}
