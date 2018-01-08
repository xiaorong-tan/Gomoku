import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

public class GobangFrame extends JFrame {

    JRadioButton manualBtn = new JRadioButton("VS Human");
    JRadioButton halfAutoBtn = new JRadioButton("VS Computer", true);
    JRadioButton treeBtn = new JRadioButton("Alpha-beta pruning", true);
    JComboBox<Integer> levelCombo = new JComboBox<Integer>(new Integer[] { 1,
            2, 3 });
    JComboBox<Integer> nodeCombo = new JComboBox<Integer>(new Integer[] { 3, 5,
            10 });
    JButton newbtn = new JButton("New Game");
    JButton undoBtn = new JButton("Undo");
    GobangPanel panel = new GobangPanel();// 棋盘面板

    public GobangFrame()throws Exception {
        super("Gobang by Xiaorong");
        add(panel, BorderLayout.WEST);

        ButtonGroup grp_mode = new ButtonGroup();
        grp_mode.add(manualBtn);
        grp_mode.add(halfAutoBtn);

        ButtonGroup grp_alg = new ButtonGroup();

        grp_alg.add(treeBtn);

        JPanel rightPanel = new JPanel();
        //rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel optPanel = new JPanel();
        optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
        optPanel.setBorder(new TitledBorder("Setting"));

        JPanel panel2 = new JPanel();
        panel2.setBorder(new TitledBorder("Mode"));
       // panel2.setPreferredSize(new Dimension(150,170));
        panel2.add(manualBtn);
        panel2.add(halfAutoBtn);
        optPanel.add(panel2);

        JPanel panel3 = new JPanel();
        panel3.setBorder(new TitledBorder("Algorithm"));
        panel3.add(treeBtn);
        optPanel.add(panel3);


        optPanel.add(newbtn);
        optPanel.add(undoBtn);
        rightPanel.add(optPanel);


        add(rightPanel);
        newbtn.addActionListener(l);
        undoBtn.addActionListener(l);

        setSize(900, 700);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

    }

    private ActionListener l = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == newbtn) {
                int mode = -1, intel = -1, level, node;
                if (manualBtn.isSelected())
                    mode = GobangPanel.MANUAL;
                else if (halfAutoBtn.isSelected())
                    mode = GobangPanel.HALF;


               if (treeBtn.isSelected())
                    intel = GobangPanel.TREE;

                level = (Integer) levelCombo.getSelectedItem();
                node = (Integer) nodeCombo.getSelectedItem();

                panel.startGame(mode, intel, level, node);
            }  else if (source == undoBtn) {
                panel.undo();
            }
        }
    };
}