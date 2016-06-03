package helloworld.example.com.hw4_b;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawingSurface(this));
    }
}

class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
    Canvas cacheCanvas;
    Bitmap backBuffer;
    int width, height, clientHeight;
    Paint paint;
    Context context;
    SurfaceHolder mHolder;
    int[][] maze = new int[21][14];
    Bitmap wall;
    Bitmap point;
    Bitmap location;
    int m_width = 21;
    int m_height = 14;
    boolean[][] wasHere = new boolean[m_width ][m_height];
    boolean[][] correctPath = new boolean[m_width ][m_height]; // The solution to the maze

    public DrawingSurface(Context context) {
        super(context);
        this.context = context;
        init();
    }
    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        wall = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
        wall = Bitmap.createScaledBitmap(wall, 50, 50, false);

        point = BitmapFactory.decodeResource(getResources(), R.drawable.point);
        point = Bitmap.createScaledBitmap(point, 25, 25, false);

        location = BitmapFactory.decodeResource(getResources(), R.drawable.location);
        location = Bitmap.createScaledBitmap(location, 50, 50, false);


        while(true){ //loop untill search for correct path which exists 'Exit'.
            make_maze();
            if(solveMaze() == true)
                break;
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
    public void surfaceCreated(SurfaceHolder holder) {
        width = getWidth();
        height = getHeight();
        cacheCanvas = new Canvas();
        backBuffer = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888); //back buffer
        cacheCanvas.setBitmap(backBuffer);
        cacheCanvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        draw();
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    int lastX, lastY, currX, currY;
    boolean isDeleting;
    int flag = 0; // start지점에서 눌렀을 때만 실행시켜주기 위함.
    int re_draw = 1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();

        if(lastX/50==13 && lastY/50 ==19) { //만약 지정된 출구에 도착한다면 초기화
            Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            lastX=0;
            lastY=0;
            init();
        }
        if((event.getX()/50>0 && event.getX()/50 <2 && event.getY()/50>0 && event.getY()/50 <2) || flag == 1 ||
                (event.getX()+50>=lastX && event.getX()-50<=lastX && event.getY()+50>=lastY && event.getY()-50<=lastY )){
            //시작점에 처음 시작해야지 이벤트 시작. 그리고 옮긴 위치에서 시작해야지 다시 시작.

            flag = 1; // 그리기를 진행한다.

            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isDeleting) break;
                    currX = (int) event.getX();
                    currY = (int) event.getY();

                    int x = currX/50;
                    int y = currY/50;

                    if(maze[y][x] == 1){ //만약 벽을 만난다면 다시 maze를 만들고, 여태까지 그려진 선을 초기화.
                        init();
                        cacheCanvas.drawColor(Color.WHITE);
                        re_draw=0; //motionevent가 up되었을 때(손가락을 때었을때) 발생되는 이벤트를 막기위함.
                        break;
                    }

                    cacheCanvas.drawLine(lastX, lastY, currX, currY, paint);
                    lastX = currX;
                    lastY = currY;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isDeleting) isDeleting = false;

                    if(re_draw==0){ //선을 이어오다가 벽을 만나면 re_draw를 0으로해줌.
                        flag = 0; //초기화
                        lastX=0;  //초기화
                        lastY=0; //초기화
                        cacheCanvas.drawColor(Color.WHITE);
                        re_draw=1; //초기화
                        break;
                    }
                    else { //선을 정상적으로 이어온다면
                        int _y = lastY / 50;
                        int _x = lastX / 50;
                        if (maze[_y][_x] == 1) { //손가락을 떼어냈을 때, 벽을 만난다면
                            cacheCanvas.drawColor(Color.WHITE);
                            flag = 0;
                            break;
                        }
                        flag = 0; // 시작점의 위치가 바뀌었음. 그러나 다른 위치에서 그려주는 것을 막기위함.
                        cacheCanvas.drawColor(Color.WHITE);
                        cacheCanvas.drawBitmap(point, lastX, lastY, null);
                        break;
                    }
                case MotionEvent.ACTION_POINTER_DOWN:
                    cacheCanvas.drawColor(Color.WHITE);
                    isDeleting = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
        }
        draw(); // SurfaceView에 그리는 function을 직접 제작 및 호출
        return true;
    }
    protected void draw() {
        if(clientHeight==0) {
            clientHeight = getClientHeight();
            height = clientHeight;
            backBuffer = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas.setBitmap(backBuffer);
            cacheCanvas.drawColor(Color.WHITE);
        }
        draw_maze();
        Canvas canvas = null;
        try{
            canvas = mHolder.lockCanvas(null);
//back buffer에 그려진 비트맵을 스크린 버퍼에 그린다
            canvas.drawBitmap(backBuffer, 0,0, paint);
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            if(mHolder!=null) mHolder.unlockCanvasAndPost(canvas);
        }
    }
    /* 상태바, 타이틀바를 제외한 클라이언트 영역의 높이를 구한다 */
    private int getClientHeight() {
        Rect rect= new Rect();
        Window window = ((Activity)context).getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight= rect.top;
        int contentViewTop= window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;
        return ((Activity)context).getWindowManager().getDefaultDisplay().
                getHeight() - statusBarHeight - titleBarHeight;
    }

    private void make_maze(){ //make random maze.

        for(int i=0;i<21;i++){
            for(int j=0;j<14;j++){
                maze[i][j] = (int)(Math.random()*2);
            }
        }

        for(int i=0;i<21;i++){
            for(int j=0;j<14;j++){
                if(i==0 || i==20 || j==0 || j==13) //outterior is always wall.
                    maze[i][j] = 1;
            }
        }

        maze[1][1] = 0; //always start
        maze[19][13] = 0; //always exit
    }

    public boolean solveMaze() {
        for (int row = 0; row < maze.length; row++)
            // Sets boolean Arrays to default values
            for (int col = 0; col < maze[row].length; col++){
                wasHere[row][col] = false;
                correctPath[row][col] = false;
            }
        boolean b = recursiveSolve(1, 1);
        // Will leave you with a boolean array (correctPath)
        // with the path indicated by true values.
        // If b is false, there is no solution to the maze

        return b;
    }
    public boolean recursiveSolve(int x, int y) {
        if (x == 19 && y == 13) return true; // If you reached the end
        if (maze[x][y] == 1 || wasHere[x][y]) return false;
        // If you are on a wall or already were here
        wasHere[x][y] = true;
        if (x != 0) // Checks if not on left edge
            if (recursiveSolve(x-1, y)) { // Recalls method one to the left
                correctPath[x][y] = true; // Sets that path value to true;
                return true;
            }
        if (x != m_width - 1) // Checks if not on right edge
            if (recursiveSolve(x+1, y)) { // Recalls method one to the right
                correctPath[x][y] = true;
                return true;
            }
        if (y != 0)  // Checks if not on top edge
            if (recursiveSolve(x, y-1)) { // Recalls method one up
                correctPath[x][y] = true;
                return true;
            }
        if (y != m_height- 1) // Checks if not on bottom edge
            if (recursiveSolve(x, y+1)) { // Recalls method one down
                correctPath[x][y] = true;
                return true;
            }
        return false;
    }

    private void draw_maze(){ // draw maze
        int x=0;
        int y=0;
        for(int i=0;i<21;i++){
            for(int j=0;j<14;j++){
                if(maze[i][j] == 1) // if it is wall, draw wall.
                    cacheCanvas.drawBitmap(wall,x,y,null);
                x+=50;
            }
            y+=50;
            x=0;
        }

        cacheCanvas.drawBitmap(location,50,50,null); //draw start location
        cacheCanvas.drawBitmap(point,65,65,null); //draw point
        cacheCanvas.drawBitmap(location,650,950,null); //draw exit location.
    }
} // class DrawingSurface