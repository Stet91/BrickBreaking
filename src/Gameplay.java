import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Gameplay extends JPanel implements KeyListener, ActionListener, MouseListener, MouseMotionListener {

    private Status status = Status.OFF;
    private Timer timer;
    private int delay = 8;
    private int score;
    private int columnBricks;
    private int rowBricks;
    private int totalBricks;
    private int playerX;
    private int ballposX;
    private int ballposY;
    private int ballXdir; //скорость шара по Х
    private int ballYdir; //скорость шара по У
    private int cursorX;
    private int cursorY;
    private MapGenerator map;

    Gameplay() {
        initNewGame();
        addKeyListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        timer = new Timer(delay, this);
        timer.start();
    }

    public void paint(Graphics g) {

        try {
            Image img = ImageIO.read(new File("D://1.jpg"));
            g.drawImage(img, 0, 0, null);
        } catch (IOException e) {
            System.out.println("Фоновое изображение отсутствует");
        }

        // drawing map
        map.draw(((Graphics2D) g));

        // граница
        g.setColor(Color.yellow);
        g.fillRect(0, 0, 3, 592);
        g.fillRect(0, 0, 692, 3);
        g.fillRect(691, 0, 3, 592);

        // scores
        g.setColor(Color.white);
        g.setFont(new Font("Serif", Font.BOLD, 25));
        g.drawString("" + score, 590, 30);

        // планка
        g.setColor(Color.green);
        g.fillRect(playerX, 550, 100, 8);

        // the ball
        g.setColor(Color.yellow);
        g.fillOval(ballposX, ballposY, 20, 20);

        if (totalBricks <= 0) {
            status = Status.OFF;
            g.setColor(Color.red);
            g.setFont(new Font("Serif", Font.BOLD, 30));
            g.drawString("You Won, Scores: " + score, 200, 300);

            g.setFont(new Font("Serif", Font.BOLD, 20));
            g.drawString("Press Enter to Restart ", 230, 350);
        }

        if (ballposY > 570) {
            status = Status.OFF;
            g.setColor(Color.red);
            g.setFont(new Font("Serif", Font.BOLD, 30));
            g.drawString("Game Over, Scores: " + score, 190, 300);

            g.setFont(new Font("Serif", Font.BOLD, 20));
            g.drawString("Press Enter to Restart ", 230, 350);
        }

        g.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();

        if (status != Status.OFF) {
            if (ballRect().intersects(new Rectangle(playerX, 550, 40, 8))) {
                ballYdir = ballYdir > 0 ? -ballYdir + 1 : ballYdir - 1;
                ballXdir = ballXdir > 0 ? ballXdir + 1 : ballXdir - 1;
            }
            if (ballRect().intersects(new Rectangle(playerX + 40, 550, 20, 8))) {
                ballYdir = ballYdir > 0 ? -ballYdir : ballYdir;
            }
            if (ballRect().intersects(new Rectangle(playerX + 60, 550, 40, 8))) {
                ballYdir = ballYdir > 0 ? -ballYdir + 1 : ballYdir - 1;
                ballXdir = ballXdir > 0 ? ballXdir + 1 : ballXdir - 1;
            }

            A:
            for (int i = 0; i < map.map.length; i++) {
                for (int j = 0; j < map.map[0].length; j++) {
                    if (map.map[i][j] > 0) {
                        int brickX = j * map.brickWidth + 80;
                        int brickY = i * map.brickHeight + 50;
                        int brickWidth = map.brickWidth;
                        int brickHeight = map.brickHeight;

                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);
                        Rectangle ballRect = ballRect();

                        if (ballRect.intersects(brickRect)) {
                            map.setBrickValue(0, i, j);
                            totalBricks--;
                            score += 10;

                            if (ballposX + 19 <= brickRect.x || ballposX >= brickRect.x + brickRect.width) {
                                ballXdir = -ballXdir;
                            } else {
                                ballYdir = -ballYdir;
                            }
                            break A;
                        }
                    }
                }
            }
            ballposX += ballXdir;
            ballposY += ballYdir;
            if (ballposX < 0) {
                ballXdir = -ballXdir;
            }
            if (ballposX > 670) {
                ballXdir = -ballXdir;
            }
            if (ballposY < 0) {
                ballYdir = -ballYdir;
            }

        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (playerX >= 600) {
                playerX = 600;
            } else {
                moveRight();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (playerX < 10) {
                playerX = 10;
            } else {
                moveLeft();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (status == Status.OFF) {
                initNewGame();

                repaint();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (status == Status.OFF) {
                System.exit(0);
            }

            if (status != Status.OFF) {
                this.status = Status.OFF;
                initNewGame();

                repaint();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        status = status == Status.BEGIN ? Status.CONTINUE : Status.END;

        if (ballRect().intersects(new Rectangle(playerX, 550, 100, 8))) {
            ballXdir = Math.abs(e.getX() - cursorX) > 10 ? 10 : e.getX() - cursorX;
            ballYdir = Math.abs(e.getY() - cursorY) > 10 ? 10 : e.getY() - cursorY;
        }

        if (e.getX() < cursorX) {
            playerX = playerX + (e.getX() - cursorX);
            cursorX = e.getX();
        }

        if (e.getX() > cursorX) {
            playerX = playerX - (cursorX - e.getX());
            cursorX = e.getX();
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (cursorX != 360) {
            status = status == Status.BEGIN ? Status.CONTINUE : Status.END;
        }
        if (status != Status.OFF) {
            if (e.getX() < cursorX) {
                playerX = playerX + (e.getX() - cursorX);
                cursorX = e.getX();
            }

            if (e.getX() > cursorX) {
                playerX = playerX - (cursorX - e.getX());
                cursorX = e.getX();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (ballRect().intersects(new Rectangle(playerX, 550, 110, 15))) {
            ballYdir = 0;
            ballXdir = 0;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (ballRect().intersects(new Rectangle(playerX, 550, 100, 8))) {
            ballYdir = -3;
            ballXdir = 3;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private Rectangle ballRect() {
        return new Rectangle(ballposX, ballposY, 20, 20);
    }

    private void moveRight() {
        status = status == Status.END ? Status.END : Status.CONTINUE;
        playerX += 10;
    }

    private void moveLeft() {
        status = status == Status.END ? Status.END : Status.CONTINUE;
        playerX -= 10;
    }

    private void initNewGame() {
        this.score = 0;
        this.columnBricks = 10;
        this.rowBricks = 7;
        this.totalBricks = rowBricks * columnBricks;
        this.playerX = 300;
        this.ballXdir = -3;
        this.ballYdir = -4;
        this.ballposX = 340;
        this.ballposY = 530;
        this.cursorX = 350;
        this.cursorY = 530;
        this.map = new MapGenerator(rowBricks, columnBricks);
    }

    private enum Status {
        OFF,
        BEGIN,
        CONTINUE,
        END
    }
}
