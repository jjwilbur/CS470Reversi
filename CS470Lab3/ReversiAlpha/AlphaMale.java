package ReversiAlpha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

class AlphaMale {

    public Socket s;
	public BufferedReader sin;
	public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int boardState;
    int state[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;
    
    int validMoves[] = new int[64];
    int numValidMoves;
    int myGlobMove;
    int minDepth;
    
    // main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
    public AlphaMale(int _me, String host) {
        me = _me;
        initClient(host);

        int myMove;
        
        while (true) {
            //System.out.println("Read");
            readMessage();
            
            if (turn == me) {
                //System.out.println("Move");
                int maxDepth = 12;
                minDepth = maxDepth;
                alphabeta(state, round, minDepth, -64, 64, true);
                //myMove = generator.nextInt(numValidMoves);        // select a move randomly

                String sel = (myGlobMove / 8) + "\n" + (myGlobMove % 8);
                
                //System.out.println("Selection: " + (myGlobMove / 8) + ", " + (myGlobMove % 8));
                System.out.println("You reached depth = " + (maxDepth - minDepth));
                sout.println(sel);
            }
        }
        //while (turn == me) {
        //    System.out.println("My turn");
            
            //readMessage();
        //}
    }


    private int alphabeta(int node[][], int curRound, int depth, int alpha, int beta, boolean maximizingPlayer){
        if(depth < minDepth){
            minDepth = depth;
        }
        if (depth == 0) // may want to think about this
            return heuristic(node);
        if (maximizingPlayer){
            int v = Integer.MIN_VALUE;
            //need to flip others find code
            int moves[] = getValidMoves(curRound, node);
            int heuristics[] = new int[32];
            int selectedMove = 0;
            for(int i = 0; i < 32;i++){
                if(moves[i] > 0){
                    int childState[][] = node.clone();
                    childState[moves[i] / 8][ moves[i] % 8] = me;
                    changeColors(childState, moves[i] / 8, moves[i] % 8, me - 1);
                    int childValue = alphabeta(childState, curRound + 1, depth - 1, alpha, beta, false);
                    heuristics[i] = childValue;
                    if(childValue > v){
                        selectedMove = i;
                    }
                    v = Math.max(v, childValue);
                    alpha = Math.max(alpha, v);
                    if(beta <= alpha){
                        break; //(* beta cut-off *);
                    }
                }
            }
            if(curRound == round){
                myGlobMove = moves[selectedMove];
            }
            if(v == Integer.MIN_VALUE){
                return heuristic(node);
            }else{
                return v;
            }
        }else{
            int v = Integer.MAX_VALUE;
            int moves[] = getValidMoves(curRound, node);
            for(int i = 0; i < 32; i++){
                if(moves[i] > 0){
                    int childState[][] = node.clone();
                    int player = me;
                    if(me == 2){
                        player = 1;
                    }else{
                        player = 2;
                    }
                    childState[moves[i] / 8][moves[i] % 8] = player;
                    changeColors(childState, moves[i] / 8, moves[i] % 8, player - 1);
                    int childValue = alphabeta(childState, curRound + 1, depth - 1, alpha, beta, true);
                    v = Math.min(v, childValue);
                    beta = Math.min(beta, v);
                    if (beta <= alpha){
                        break; // (* alpha cut-off *);
                    }
                }
            }
            if(v == Integer.MAX_VALUE){
                return heuristic(node);
            }else{
                return v;
            }
        }
    }

    public static void changeColors(int myState[][], int row, int col, int turn) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                checkDirectionChange(myState, row, col, incx, incy, turn);
            }
        }
    }

    public static void checkDirectionChange(int myState[][], int row, int col, int incx, int incy, int turn) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;

        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;

            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;

            sequence[seqLen] = myState[r][c];
            seqLen++;
        }

        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (turn == 0) {
                if (sequence[i] == 2)
                    count++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        count = 20;
                    break;
                }
            } else {
                if (sequence[i] == 1)
                    count++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        count = 20;
                    break;
                }
            }
        }

        if (count > 10) {
            if (turn == 0) {
                i = 1;
                r = row+incy*i;
                c = col+incx*i;
                while (myState[r][c] == 2) {
                    myState[r][c] = 1;
                    i++;
                    r = row+incy*i;
                    c = col+incx*i;
                }
            } else {
                i = 1;
                r = row+incy*i;
                c = col+incx*i;
                while (myState[r][c] == 1) {
                    myState[r][c] = 2;
                    i++;
                    r = row+incy*i;
                    c = col+incx*i;
                }
            }
        }
    }

    private int heuristic(int myState[][]){
        int numOriginal = 0;
        int numCurrent = 0;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 8; j++){
                if(state[i][j] == me){
                    numOriginal++;
                }
                if(state[i][j] == me){
                    numCurrent++;
                }
            }
        }
        return numCurrent - numOriginal;
    }
    
    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    private int[] getValidMoves(int round, int myState[][]) {
        int i, j;
        int validMovesRet[] = new int[32];
        numValidMoves = 0;
        if (round < 4) {
            if (myState[3][3] == 0) {
                validMovesRet[numValidMoves] = 3*8 + 3;
                numValidMoves++;
            }
            if (myState[3][4] == 0) {
                validMovesRet[numValidMoves] = 3*8 + 4;
                numValidMoves++;
            }
            if (myState[4][3] == 0) {
                validMovesRet[numValidMoves] = 4*8 + 3;
                numValidMoves++;
            }
            if (myState[4][4] == 0) {
                validMovesRet[numValidMoves] = 4*8 + 4;
                numValidMoves++;
            }
            for (i = 0; i < numValidMoves; i++) {

            }
        }
        else {

            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (myState[i][j] == 0) {
                        if (couldBe(myState, i, j)) {
                            validMovesRet[numValidMoves] = i*8 + j;
                            numValidMoves ++;

                        }
                    }
                }
            }
        }
        return validMovesRet;
        
        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
    }
    
    private boolean checkDirection(int myState[][], int row, int col, int incx, int incy) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;
        
        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row+incy*i;
            c = col+incx*i;
        
            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;
        
            sequence[seqLen] = myState[r][c];
            seqLen++;
        }
        
        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (me == 1) {
                if (sequence[i] == 2)
                    count ++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            }
            else {
                if (sequence[i] == 1)
                    count ++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }
        
        return false;
    }
    
    private boolean couldBe(int myState[][], int row, int col) {
        int incx, incy;
        
        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;
            
                if (checkDirection(myState, row, col, incx, incy))
                    return true;
            }
        }
        
        return false;
    }

    public void readMessage() {
        int i, j;
        String status;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());

            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                System.exit(1);
            }

            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
            //System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            //System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    state[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        //System.out.println("Turn: " + turn);
        //System.out.println("Round: " + round);
        for (i = 7; i >= 0; i--) {
            for (j = 0; j < 8; j++) {
                //System.out.print(state[i][j]);
            }
            //System.out.println();
        }
        //System.out.println();
    }
    
    public void initClient(String host) {
        int portNumber = 3333+me;
        
        try {
			s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
			sin = new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }

    
    // compile on your machine: javac *.java
    // call: java AlphaMale [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new AlphaMale(Integer.parseInt(args[1]), args[0]);
    }
    
}
