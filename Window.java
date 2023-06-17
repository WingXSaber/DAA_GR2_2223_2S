
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridLayout;

public class Window{
    JFrame frame;
    JPanel displayPanel;
    public Window(int width, int height, String title, Game game){
        //Game extends Canvas class. And it has no definite size. We use layout for container or set size for canvas
        frame = new JFrame(title);
        
        displayPanel = new JPanel(new GridLayout());
        displayPanel.setMinimumSize  (new Dimension(width, height));
        displayPanel.setMaximumSize  (new Dimension(width, height));
        displayPanel.setPreferredSize(new Dimension(width, height));

        displayPanel.add(game);

        frame.add(displayPanel);
        frame.pack();   //finish initalize jFrame.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);  //so window spawns in the center of the screen
        frame.setVisible(true);
    }

    public int getCanvasHeight(){
        return displayPanel.getHeight();
    }

    public int getCanvasWidth(){
        return displayPanel.getWidth();
    }   

}