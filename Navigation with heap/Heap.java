package navigation;

/**
 * Priority heap for jump point search.
 * @author david
 */
public class Heap {
    
    JumpPoint[] points;     // Array used to store this heap's nodes.
    int[] goal;             // Coordinates of the goal.
    int[] visited;          // Record of locations that have been visited.
    
    int size;               // Size of this heap.
    
    /**
     * Constructor. Creates four JumpPoint objects and prioritizes them based on
     * their directions of search relative to the goal. A 16-direction compass is
     * used to do this prioritization, which is then reduced to the 8-direction
     * compass used is all other areas of the Navigation package.
     * @param start start coordinates.
     * @param g end coordinates.
     */
    public Heap(int[] start, int[] g) {
        visited = new int[65536];
        points = new JumpPoint[65536];
        goal = new int[] { g[0], g[1] };
        
        int score = Math.max(Math.abs(g[0] - start[0]), Math.abs(g[1] - start[1]));
        JumpPoint front = new JumpPoint(start, score);
        JumpPoint next = new JumpPoint(start, score+1);
        JumpPoint next2 = new JumpPoint(start, score+2);
        JumpPoint next3 = new JumpPoint(start, score+3);
        
        visited[start[1]*256+start[0]] = -1;
        
        switch (front.directionTo(g)) {
            case 1:     // NNE
                front.direction = 1;
                next.direction = 7;
                next2.direction = 3;
                next3.direction = 5;
                break;
            case 3:     // ENE
                front.direction = 1;
                next.direction = 3;
                next2.direction = 7;
                next3.direction = 5;
                break;
            case 5:     // ESE
                front.direction = 3;
                next.direction = 1;
                next2.direction = 5;
                next3.direction = 7;
                break;
            case 7:     // SSE
                front.direction = 3;
                next.direction = 5;
                next2.direction = 1;
                next3.direction = 7;
                break;
            case 9:     // SSW
                front.direction = 5;
                next.direction = 3;
                next2.direction = 7;
                next3.direction = 1;
                break;
            case 11:    // WSW
                front.direction = 5;
                next.direction = 7;
                next2.direction = 3;
                next3.direction = 1;
                break;
            case 13:    // WNW
                front.direction = 7;
                next.direction = 5;
                next2.direction = 1;
                next3.direction = 3;
                break;
            default:    // NNW
                front.direction = 7;
                next.direction = 1;
                next2.direction = 5;
                next3.direction = 3;
                break;
        }
        
        points[4] = front;
        points[3] = next;
        points[2] = next2;
        points[1] = next3;
        
        size = 4;
    }
    
    /**
     * Remove the jump point at the top of the priority heap
     * @return the jump point on the heap with the lowest f(n) = h(n) + g(n)
     */
    public JumpPoint remove() {
        JumpPoint top = points[1];
        points[1] = points[size];
        points[size--] = null;
        
        for(int i = 2; i < size-1; i*=2) {
            int rightIndex = i+1;
            int index = i/2;
            
            JumpPoint left = points[i];
            JumpPoint right = points[rightIndex];
            JumpPoint current = points[index];
            
            if (left.score < right.score && left.score < current.score) {
                points[i] = current;
                points[index] = left;
            } else if (right.score < current.score) {
                points[rightIndex] = current;
                points[index] = right;
                i++;
            } else {
                break;
            }
        }
        
        return top;
    }
    
    /**
     * Create a new jump point
     * @param newNode   coordinates of the new jump point
     * @param direction directionality of search from the new jump point
     * @param waypoint  point between last jump point and the new jump point
     * @param lastJP    the last jump point
     * @param distance the distance traveled to get to the new jump point
     */
    public void insert(int[] newNode, int direction, int[] waypoint, JumpPoint lastJP, int distance) {
        int index = newNode[1]*256+newNode[0];  // linear index of newNode coordinates
        int visitedDirection = visited[index];
        distance += lastJP.distance;
        
        if (visitedDirection == 0) {
            points[++size] = new JumpPoint(newNode, waypoint, lastJP, direction, distance,
                    Math.max(Math.abs(goal[0]-newNode[0]),Math.abs(goal[1]-newNode[1]))+distance);
            int next = size-1;
            for (int i = next; i > 2; i=next) {
                next /= 2;
                if (points[i].score < points[next].score) {
                    JumpPoint temp = points[next];
                    points[next] = points[i];
                    points[i] = temp;
                } else {
                    break;
                }
            }
            
        } else if (visitedDirection != direction && visitedDirection > 0) {
            visited[index] = -1;
            points[++size] = new JumpPoint(newNode, waypoint, lastJP, direction, distance,
                    Math.max(Math.abs(goal[0]-newNode[0]),Math.abs(goal[1]-newNode[1]))+distance);
            int next = size-1;
            for (int i = next; i > 2; i=next) {
                next /= 2;
                if (points[i].score < points[next].score) {
                    JumpPoint temp = points[next];
                    points[next] = points[i];
                    points[i] = temp;
                } else {
                    break;
                }
            }
        }
    }
    
    /**
     * Retrace the path from any given jump point to the start.
     * Links nodes to create a bi-directional graph from the starting jump point
     * to the given jump point.
     * @param jp Jump point at the end of the path
     * @return Jump point at the start of the path
     */
    public JumpPoint retrace(JumpPoint jp) {
        JumpPoint next = jp.mapLast;
        while(next != null) {
            next.mapNext = jp;
            jp = next;
            next = next.mapLast;
        }
        return new JumpPoint(jp);
    }
}
