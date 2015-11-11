package navigation;

/**
 * Queue designed for Jump Point Search.
 * @author david
 */
public class Queue {

    Node front;             // Head of the queue.
    int[] goal;             // Coordinates of the goal.
    int[] visited;
    boolean firstDiagonal;  // true iff pathStart is the first diagonal.
    
    /**
     * Constructor
     * @param start start coordinates.
     * @param goal end coordinates.
     */
    public Queue(int[] start, int[] goal) {
        visited = new int[65536];
        firstDiagonal = true;
        this.goal = new int[2];
        this.goal[0] = goal[0];
        this.goal[1] = goal[1];
        int score = Math.max(Math.abs(goal[0] - start[0]), Math.abs(goal[1] - start[1]));
        front = new Node(start, score);
        Node next = new Node(start, score);
        Node next2 = new Node(start, score);
        Node next3 = new Node(start, score);
        visited[start[1]*256+start[0]] = -1;
        switch (front.directionTo(goal)) {
            case 1:
                front.direction = 1;
                next.direction = 7;
                next2.direction = 3;
                next3.direction = 5;
                break;
            case 3:
                front.direction = 1;
                next.direction = 3;
                next2.direction = 7;
                next3.direction = 5;
                break;
            case 5:
                front.direction = 3;
                next.direction = 1;
                next2.direction = 5;
                next3.direction = 7;
                break;
            case 7:
                front.direction = 3;
                next.direction = 5;
                next2.direction = 1;
                next3.direction = 7;
                break;
            case 9:
                front.direction = 5;
                next.direction = 3;
                next2.direction = 7;
                next3.direction = 1;
                break;
            case 11:
                front.direction = 5;
                next.direction = 7;
                next2.direction = 3;
                next3.direction = 1;
                break;
            case 13:
                front.direction = 7;
                next.direction = 5;
                next2.direction = 1;
                next3.direction = 3;
                break;
            default:    // case 15
                front.direction = 7;
                next.direction = 1;
                next2.direction = 5;
                next3.direction = 3;
                break;
        }
        next2.queueNext = next3;
        next.queueNext = next2;
        front.queueNext = next;
    }

    // heuristic function, Chebyshev distance.
    public int heuristic(int[] a) {
        return Math.max(Math.abs(goal[0] - a[0]), Math.abs(goal[1] - a[1]));
    }
    
    /**
     * Remove the Node at the front of this Queue
     * @return a copy of the front Node object.
     */
    public Node pop() {
        Node top;
        if (front == null) {
            top = null;
        } else if (front.mapLast == null) {
            top = front;
            front = front.queueNext;
            firstDiagonal = false;
        } else {
            top = front;
            front = front.queueNext;
        }
        return top;
    }

    /**
     * Calculate heuristic and add a Node to the queue.
     * @param newNode Cartesian coordinates of the new node.
     * @param direction directionality of the new Node's diagonal.
     * @param waypoint Coordinates of the point between the last and new Nodes.
     * @param lastNode Previous node on the path.
     */
    public void offer(int[] newNode, int direction, int[] waypoint, Node lastNode) {
        int index = newNode[1]*256+newNode[0];  // linear index of newNode coordinates
        int visitedDirection = visited[index];
        
        if (visitedDirection == 0) {
            
            visited[index] = direction;
        
            // Create a new node.
            Node node = new Node(newNode, waypoint, lastNode, direction,
                    Math.max(Math.abs(goal[0]-newNode[0]),Math.abs(goal[1]-newNode[1])));
            
            // Add the new node to the queue.
            if (front == null) {
                front = node;
            } else {
                Node last = front;
                Node next = last.queueNext;
                while (next != null && next.score < node.score) {
                    last = next;
                    next = next.queueNext;
                }
                node.queueNext = next;
                last.queueNext = node;
            }
            
        } else if (visitedDirection != direction && visitedDirection > 0) {
            
            visited[index] = -1;
            
            // Create a new node.
            Node node = new Node(newNode, waypoint, lastNode, direction,
                    Math.max(Math.abs(goal[0]-newNode[0]),Math.abs(goal[1]-newNode[1])));
            
            // Add the new node to the queue.
            if (front == null) {
                front = node;
            } else {
                Node last = front;
                Node next = last.queueNext;
                while (next != null && next.score < node.score) {
                    last = next;
                    next = next.queueNext;
                }
                node.queueNext = next;
                last.queueNext = node;
            }
        }
    }
    
    public Node retrace(Node node) {
        Node next = node.mapLast;
        while(next != null) {
            next.mapNext = node;
            node = next;
            next = next.mapLast;
        }
        return new Node(node);
    }
}
