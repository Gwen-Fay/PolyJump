package modelEdit;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.*;

/**
 * Created by gwen on 7/26/16.
 */
public class ModelJFrame extends JFrame {

    public static void main(String[] args){
        new ModelJFrame();
    }

    JPanel mainPanel;

    JMenuBar menuBar;
    JMenuItem write, writeAs, read;
    JMenuItem bgColor, lColor;

    JTabbedPane tabbedPane = new JTabbedPane();
    JPanel  brush = new JPanel(),
            filter = new JPanel(),
            animation = new JPanel();

    JColorChooser bgColorChooser = new JColorChooser();
    JColorChooser lColorChooser = new JColorChooser();

    JRadioButton normalSelect = new JRadioButton("Normal");
    JRadioButton addSelect = new JRadioButton("Add");
    JRadioButton subSelect = new JRadioButton("Subtract");
    JRadioButton intSelect = new JRadioButton("Intersection");
    JRadioButton boxBrush = new JRadioButton("Box");
    JRadioButton sphereBrush = new JRadioButton("Sphere");
    JRadioButton diamondBrush = new JRadioButton("Diamond");
    JRadioButton triangleBrush = new JRadioButton("Triangle");
    JCheckBox selectBrush = new JCheckBox("Select Brush");
    JCheckBox paintBrush = new JCheckBox("Paint Brush");
    JCheckBox brushVisible = new JCheckBox("brush visible");
    JLabel colorLabel = new JLabel("Select Color:");
    JColorChooser brushColor = new JColorChooser();
    JLabel colorVarianceLabel = new JLabel("Color Variance:");
    JTextField colorVariance = new JTextField("0.0",5);
    JLabel brushChanceLabel = new JLabel("Brush Chance:");
    JSlider brushChance = new JSlider(0,100,100);
    JButton brushNow = new JButton("Brush");

    JTextField pos1x = new JTextField("0",5);
    JTextField pos1y = new JTextField("0",5);
    JTextField pos1z = new JTextField("0",5);
    JButton move1 = new JButton("Move");

    JTextField pos2x = new JTextField("0",5);
    JTextField pos2y = new JTextField("0",5);
    JTextField pos2z = new JTextField("0",5);
    JButton move2 = new JButton("Move");

    JTextField pos3x = new JTextField("0",5);
    JTextField pos3y = new JTextField("0",5);
    JTextField pos3z = new JTextField("0",5);
    JButton move3 = new JButton("Move");

    JTextField brushX = new JTextField("1",5);
    JTextField brushY = new JTextField("1",5);
    JTextField brushZ = new JTextField("1",5);
    JButton brushSizeButton = new JButton("ReSize");

    JSlider deselectChance = new JSlider(0,100,100);
    JButton deselectNow = new JButton("De-Select");

    JRadioButton away = new JRadioButton("Away from Blue");
    JRadioButton tward = new JRadioButton("Tward Blue");
    JRadioButton line = new JRadioButton("From Green to Blue");
    JTextField normalVariance = new JTextField("0.0",5);
    JButton normalButton = new JButton("Change Normals");

    JTextField glow = new JTextField("0.0",5);
    JTextField spec = new JTextField("1.0",5);
    JButton specButton = new JButton("Change Specular Data");

    JRadioButton x = new JRadioButton("X");
    JRadioButton y = new JRadioButton("Y");
    JRadioButton z = new JRadioButton("Z");
    JCheckBox selectRevolve = new JCheckBox("Select Revolve");
    JCheckBox paintRevolve = new JCheckBox("Paint Revolve");
    JButton revolvePreview = new JButton("Preview");
    JButton revolveButton = new JButton("Revolve");

    JColorChooser selectColor = new JColorChooser();
    JTextField selectColorVariance = new JTextField("0.0",5);
    JButton colorButton = new JButton("Change Color");

    DefaultListModel frameModel = new DefaultListModel();
    JList frames = new JList(frameModel);
    DefaultListModel animationModel = new DefaultListModel();
    JList animations = new JList(animationModel);
    DefaultListModel currentAnimModel = new DefaultListModel();
    JList currentAnimation = new JList(currentAnimModel);
    JButton addFrame = new JButton("Add");
    JButton modifyFrame = new JButton("Modify");
    JButton removeFrame = new JButton("Remove");
    JButton duplicateFrame = new JButton("Duplicate");
    JButton addAnimation = new JButton("Add");
    JButton removeAnimation = new JButton("Remove");
    JButton modifyAnimation = new JButton("Modify");
    JButton playAnimation = new JButton("Play");
    boolean isPlaying = false;
    JButton addAnimFrame = new JButton("Add");
    JButton removeAnimFrame = new JButton("Remove");
    JButton modifyAnimFrame = new JButton("Modify");

    boolean hasSaved = false;

    Canvas modelCanvas = new Canvas();
    ModelEditor modelEditor;
    Thread modelThread;

    public ModelJFrame(){

        this.setLocationRelativeTo(null);


        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension d = tk.getScreenSize();
        this.setSize(d);
        this.setLocationRelativeTo(null);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("~ ModelEditor ~");

        mainPanel = new JPanel(new BorderLayout());
        this.add(mainPanel);

        // Top Menu

        menuBar = new JMenuBar();

        MenuListener ml = new MenuListener();

        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        write = new JMenuItem("Write file");
        write.addActionListener(ml);
        writeAs = new JMenuItem("Write file as...");
        writeAs.addActionListener(ml);
        read = new JMenuItem("Read file");
        read.addActionListener(ml);
        fileMenu.add(write);
        fileMenu.add(writeAs);
        fileMenu.addSeparator();
        fileMenu.add(read);
        fileMenu.addSeparator();

        menuBar.add(fileMenu);

        bgColor = new JMenuItem("Background Color");
        bgColor.addActionListener(ml);
        lColor = new JMenuItem("Light Color");
        lColor.addActionListener(ml);

        viewMenu.add(bgColor);
        viewMenu.addSeparator();
        viewMenu.add(lColor);

        menuBar.add(viewMenu);

        this.setJMenuBar(menuBar);

        //brush tab

        {
            JPanel pos1 = new JPanel();
            Border pos1Border = BorderFactory.createTitledBorder("Blue Position");
            pos1.setBorder(pos1Border);
            pos1.add(new JLabel("X: "));
            pos1.add(pos1x);
            pos1.add(new JLabel("Y: "));
            pos1.add(pos1y);
            pos1.add(new JLabel("Z: "));
            pos1.add(pos1z);
            pos1.add(move1);
            move1.addActionListener(ml);

            JPanel pos2 = new JPanel();
            Border pos2Border = BorderFactory.createTitledBorder("Green Position");
            pos2.setBorder(pos2Border);
            pos2.add(new JLabel("X: "));
            pos2.add(pos2x);
            pos2.add(new JLabel("Y: "));
            pos2.add(pos2y);
            pos2.add(new JLabel("Z: "));
            pos2.add(pos2z);
            pos2.add(move2);
            move2.addActionListener(ml);

            JPanel pos3 = new JPanel();
            Border pos3Border = BorderFactory.createTitledBorder("Red Position");
            pos3.setBorder(pos3Border);
            pos3.add(new JLabel("X: "));
            pos3.add(pos3x);
            pos3.add(new JLabel("Y: "));
            pos3.add(pos3y);
            pos3.add(new JLabel("Z: "));
            pos3.add(pos3z);
            pos3.add(move3);
            move3.addActionListener(ml);

            JPanel brushSize = new JPanel();
            Border brushSizeBoarder = BorderFactory.createTitledBorder("Brush Size");
            brushSize.setBorder(brushSizeBoarder);
            brushSize.add(new JLabel("X: "));
            brushSize.add(brushX);
            brushSize.add(new JLabel("Y: "));
            brushSize.add(brushY);
            brushSize.add(new JLabel("Z: "));
            brushSize.add(brushZ);
            brushSize.add(brushSizeButton);
            brushSizeButton.addActionListener(ml);

            ButtonGroup selectGroup = new ButtonGroup();
            selectGroup.add(normalSelect);
            selectGroup.add(addSelect);
            selectGroup.add(subSelect);
            selectGroup.add(intSelect);

            JPanel selectPanel = new JPanel();
            Border groupBorder = BorderFactory.createTitledBorder("Selection Mode");

            selectPanel.setBorder(groupBorder);

            selectPanel.add(normalSelect);
            selectPanel.add(addSelect);
            selectPanel.add(subSelect);
            selectPanel.add(intSelect);
            normalSelect.setSelected(true);

            ButtonGroup brushGroup = new ButtonGroup();
            brushGroup.add(boxBrush);
            brushGroup.add(diamondBrush);
            brushGroup.add(sphereBrush);
            brushGroup.add(triangleBrush);
            boxBrush.setSelected(true);

            boxBrush.addActionListener(ml);
            diamondBrush.addActionListener(ml);
            sphereBrush.addActionListener(ml);
            triangleBrush.addActionListener(ml);

            JPanel brushPanel = new JPanel();
            Border brushBorder = BorderFactory.createTitledBorder("Brush Mode");

            brushPanel.setBorder(brushBorder);

            brushPanel.add(boxBrush);
            brushPanel.add(diamondBrush);
            brushPanel.add(sphereBrush);
            brushPanel.add(triangleBrush);

            brushVisible.addActionListener(ml);
            brushVisible.setSelected(true);
            brushNow.addActionListener(ml);
            paintBrush.setSelected(true);

            ColorListener cl = new ColorListener();
            brushColor.getSelectionModel().addChangeListener(cl);

            brush.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.gridwidth = 4;
            gbc.gridheight = 1;
            brush.add(pos1, gbc);
            gbc.gridy = 1;
            brush.add(pos2, gbc);
            gbc.gridy = 2;
            brush.add(pos3, gbc);
            gbc.gridy = 3;
            brush.add(brushSize, gbc);

            gbc.gridy = 4;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            brush.add(selectPanel, gbc);
            gbc.gridx = 2;
            brush.add(brushPanel, gbc);
            gbc.gridy = 5;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            brush.add(paintBrush, gbc);
            gbc.gridx = 1;
            brush.add(selectBrush, gbc);
            gbc.gridx = 2;
            brush.add(brushVisible, gbc);
            gbc.gridx = 3;
            brush.add(brushNow, gbc);

            gbc.gridy = 6;
            gbc.gridx = 0;
            brush.add(colorLabel, gbc);
            gbc.gridy = 7;
            gbc.gridx = 0;
            gbc.gridwidth = 4;
            gbc.gridheight = 4;
            brush.add(brushColor, gbc);
            gbc.gridy = 11;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            brush.add(colorVarianceLabel, gbc);
            gbc.gridx = 1;
            brush.add(colorVariance, gbc);
            gbc.gridx = 2;
            brush.add(brushChanceLabel, gbc);
            gbc.gridx = 3;

            brushChance.setMinorTickSpacing(5);
            brushChance.setMajorTickSpacing(10);
            brushChance.setPaintTicks(true);
            brushChance.setPaintLabels(true);

            brush.add(brushChance, gbc);
            tabbedPane.add("Brush", brush);
        }

        //filter
        {
            tabbedPane.add("Filter", filter);

            JPanel deselect = new JPanel();
            Border deselectBoarder = BorderFactory.createTitledBorder("Deslect");
            deselect.setBorder(deselectBoarder);

            deselect.add(new JLabel("De-Select Chance: "));

            deselectChance.setMinorTickSpacing(5);
            deselectChance.setMajorTickSpacing(10);
            deselectChance.setPaintTicks(true);
            deselectChance.setPaintLabels(true);

            deselect.add(deselectChance);
            deselect.add(deselectNow);
            deselectNow.addActionListener(ml);

            JPanel normals = new JPanel();
            Border normalsBorder = BorderFactory.createTitledBorder("Normals");
            normals.setBorder(normalsBorder);

            ButtonGroup normalMode = new ButtonGroup();
            normalMode.add(away);
            normalMode.add(tward);
            normalMode.add(line);

            normals.add(away);
            normals.add(tward);
            normals.add(line);
            normals.add(new JLabel("Variance:"));
            normals.add(normalVariance);
            normals.add(normalButton);
            away.setSelected(true);
            normalButton.addActionListener(ml);

            JPanel specular = new JPanel();
            Border specularBoarder = BorderFactory.createTitledBorder("Specular Data");
            specular.setBorder(specularBoarder);

            specular.add(new JLabel("Glow: "));
            specular.add(glow);
            specular.add(new JLabel("Specular: "));
            specular.add(spec);
            specular.add(specButton);
            specButton.addActionListener(ml);

            JPanel revolve = new JPanel();
            Border revolveBoarder = BorderFactory.createTitledBorder("Revolve");
            revolve.setBorder(revolveBoarder);

            JPanel axis = new JPanel();
            Border axisBoarder = BorderFactory.createTitledBorder("Axis of Rotation");
            axis.setBorder(axisBoarder);

            ButtonGroup axisGroup = new ButtonGroup();
            axisGroup.add(x);
            axisGroup.add(y);
            axisGroup.add(z);

            axis.add(x);
            x.setSelected(true);
            axis.add(y);
            axis.add(z);

            revolve.add(axis);
            revolve.add(selectRevolve);
            revolve.add(paintRevolve);
            revolve.add(revolveButton);
            revolve.add(revolvePreview);
            paintRevolve.setSelected(true);
            revolveButton.addActionListener(ml);
            revolvePreview.addActionListener(ml);
            colorButton.addActionListener(ml);

            JPanel color = new JPanel();
            Border colorBorder = BorderFactory.createTitledBorder("Change Color");
            colorButton.addActionListener(ml);
            color.setBorder(colorBorder);
            color.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy =0;
            gbc.gridwidth = 3;
            color.add(selectColor,gbc);
            gbc.gridy =1;
            gbc.gridwidth = 1;
            color.add(new JLabel("Color Variance:"),gbc);
            gbc.gridx = 1;
            color.add(selectColorVariance,gbc);
            gbc.gridx = 2;
            color.add(colorButton,gbc);

            gbc = new GridBagConstraints();
            filter.setLayout(new GridBagLayout());
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            gbc.gridy=0;
            filter.add(deselect,gbc);
            gbc.gridy=1;
            filter.add(normals,gbc);
            gbc.gridy=2;
            filter.add(specular,gbc);
            gbc.gridy=3;
            filter.add(revolve,gbc);
            gbc.gridy=4;
            filter.add(color,gbc);
        }

        {
            JPanel frame = new JPanel();
            Border frameBoarder = BorderFactory.createTitledBorder("Frames of Animation");
            frame.setBorder(frameBoarder);

            frames.setVisibleRowCount(8);
            frames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane frameScroll = new JScrollPane(frames,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            frames.setFixedCellHeight(30);
            frames.setFixedCellWidth(300);
            frame.add(frameScroll);
            frame.add(addFrame);
            frame.add(duplicateFrame);
            frame.add(removeFrame);
            frame.add(modifyFrame);


            frameModel.add(0,"newFrame");
            frames.setSelectedIndex(0);

            addFrame.addActionListener(ml);
            removeFrame.addActionListener(ml);
            modifyFrame.addActionListener(ml);
            duplicateFrame.addActionListener(ml);

            ListListener ll = new ListListener();
            frames.addListSelectionListener(ll);

            JPanel animationList = new JPanel();
            Border animationBoarder = BorderFactory.createTitledBorder("Created Animations");
            animationList.setBorder(animationBoarder);

            animations.setVisibleRowCount(8);
            animations.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane animationScroll = new JScrollPane(animations,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            animations.setFixedCellHeight(30);
            animations.setFixedCellWidth(300);
            animationList.add(animationScroll);
            animationList.add(addAnimation);
            animationList.add(removeAnimation);
            animationList.add(modifyAnimation);
            animationList.add(playAnimation);

            addAnimation.addActionListener(ml);
            removeAnimation.addActionListener(ml);
            modifyAnimation.addActionListener(ml);
            playAnimation.addActionListener(ml);


            animations.addListSelectionListener(ll);

            JPanel currentList = new JPanel();
            Border currentBoarder = BorderFactory.createTitledBorder("Current Animation");
            currentList.setBorder(currentBoarder);

            currentAnimation.setVisibleRowCount(8);
            currentAnimation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane currentAnimaScroll = new JScrollPane(currentAnimation,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            currentAnimation.setFixedCellHeight(30);
            currentAnimation.setFixedCellWidth(300);
            currentList.add(currentAnimaScroll);
            currentList.add(addAnimFrame);
            currentList.add(removeAnimFrame);
            currentList.add(modifyAnimFrame);

            addAnimFrame.addActionListener(ml);
            removeAnimFrame.addActionListener(ml);
            modifyAnimFrame.addActionListener(ml);
            currentAnimation.addListSelectionListener(ll);

            animation.setLayout(new BoxLayout(animation,BoxLayout.Y_AXIS));
            animation.add(frame);
            animation.add(animationList);
            animation.add(currentList);

            tabbedPane.add("Animate", animation);
        }
        TabListener tl = new TabListener();
        tabbedPane.addChangeListener(tl);

        Dimension t = tabbedPane.getPreferredSize();
        t.width = d.width/3;
        tabbedPane.setPreferredSize(t);
        mainPanel.add(tabbedPane, BorderLayout.WEST);


        mainPanel.add(modelCanvas, BorderLayout.CENTER);
        this.add(mainPanel);

        FrameListener fl = new FrameListener();
        this.addWindowListener(fl);

        modelEditor = new ModelEditor(modelCanvas);

        this.setVisible(true);

        modelThread = new Thread(modelEditor);
        //modelThread.setPriority(Thread.MAX_PRIORITY);
        modelThread.start();


    }
    private class ColorListener implements ChangeListener{
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if(changeEvent.getSource() == brushColor.getSelectionModel()) {
                modelEditor.action |= modelEditor.MOVE_BIT;
                float r = brushColor.getColor().getRed() / 255.0f;
                float g = brushColor.getColor().getGreen() / 255.0f;
                float b = brushColor.getColor().getBlue() / 255.0f;
                float a = brushColor.getColor().getAlpha() / 255.0f;
                modelEditor.setColor(r, g, b, a);
                selectColor.setColor(brushColor.getColor());
            }
        }
    }

    private  class MenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(actionEvent.getSource() == brushNow){
                modelEditor.action |= modelEditor.BRUSH_BIT;
                if(normalSelect.isSelected()){
                    modelEditor.selectMode = modelEditor.NORMAL_SELECT;
                }else if(addSelect.isSelected()){
                    modelEditor.selectMode = modelEditor.ADD_SELECT;
                }else if(subSelect.isSelected()){
                    modelEditor.selectMode = modelEditor.SUB_SELECT;
                }else if(intSelect.isSelected()){
                    modelEditor.selectMode = modelEditor.INT_SELECT;
                }
                modelEditor.isPaintBrush = paintBrush.isSelected();
                modelEditor.isSelect = selectBrush.isSelected();
                modelEditor.setChance(brushChance.getValue());
                try{
                    float v = Float.parseFloat(colorVariance.getText());
                    modelEditor.colorVariance =v;
                    if(v<0){
                        throw new NumberFormatException();
                    }
                }catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Enter a Number Greater Than or Equal to 0 Into Color Variance!");
                }
            }

            else if(actionEvent.getSource() == boxBrush){
                modelEditor.brushMode = modelEditor.BOX;
                modelEditor.action |= modelEditor.MOVE_BIT;
            }

            else if(actionEvent.getSource() == diamondBrush){
                modelEditor.brushMode = modelEditor.DIAMOND;
                modelEditor.action |= modelEditor.MOVE_BIT;
            }

            else if(actionEvent.getSource() == sphereBrush){
                modelEditor.brushMode = modelEditor.SPHERE;
                modelEditor.action |= modelEditor.MOVE_BIT;
            }

            else if(actionEvent.getSource() == triangleBrush){
                modelEditor.brushMode = modelEditor.TRIANGLE;
                modelEditor.action |= modelEditor.MOVE_BIT;
            }

            else if(actionEvent.getSource() == brushSizeButton){
                    try{
                        int x,y,z;
                        x = Integer.parseInt(brushX.getText());
                        y = Integer.parseInt(brushY.getText());
                        z = Integer.parseInt(brushZ.getText());
                        boolean err = false;
                        if(x<1){
                            brushX.setText("1");
                            err = true;
                        }
                        if(y<1){
                            brushY.setText("1");
                            err = true;
                        }
                        if(z<1){
                            brushZ.setText("1");
                            err = true;
                        }
                        if(err){
                            throw new NumberFormatException();
                        }
                        modelEditor.setBrushSize(x,y,z);
                        modelEditor.action |= modelEditor.MOVE_BIT;
                    }catch(NumberFormatException e){
                        JOptionPane.showMessageDialog(null, "Enter Integers Greater Than 0 Into Text Fields!");
                    }
            }else if(actionEvent.getSource() == move1){
                try{
                    int x,y,z;
                    x = Integer.parseInt(pos1x.getText());
                    y = Integer.parseInt(pos1y.getText());
                    z = Integer.parseInt(pos1z.getText());
                    modelEditor.setPos1(x,y,z);
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "Enter Integers Into Text Fields!");
                }
            }else if(actionEvent.getSource() == move2){
                try{
                    int x,y,z;
                    x = Integer.parseInt(pos2x.getText());
                    y = Integer.parseInt(pos2y.getText());
                    z = Integer.parseInt(pos2z.getText());
                    modelEditor.setPos2(x,y,z);
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "Enter Integers Into Text Fields!");
                }
            }else if(actionEvent.getSource() == move3){
                try{
                    int x,y,z;
                    x = Integer.parseInt(pos3x.getText());
                    y = Integer.parseInt(pos3y.getText());
                    z = Integer.parseInt(pos3z.getText());
                    modelEditor.setPos3(x,y,z);
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "Enter Integers Into Text Fields!");
                }
            }else if(actionEvent.getSource() == deselectNow){
                modelEditor.setChance(deselectChance.getValue());
                modelEditor.action |= modelEditor.DESLECT_BIT;
            }else if(actionEvent.getSource() == normalButton){
                if(away.isSelected()){
                    modelEditor.normalMode = modelEditor.AWAY;
                }else if(tward.isSelected()){
                    modelEditor.normalMode = modelEditor.TWARD;
                }else if(line.isSelected()){
                    modelEditor.normalMode = modelEditor.LINE;
                }
                try{
                    float v = Float.parseFloat(normalVariance.getText());
                    modelEditor.normalVariance = v;
                    if(v<0){
                        throw new NumberFormatException();
                    }
                    modelEditor.action |= modelEditor.NORMAL_BIT;
                }catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Enter a Number Greater Than or Equal to 0 Into Color Variance!");
                }

            }else if(actionEvent.getSource() == specButton){
                try{
                    float s,g;
                    s = Float.parseFloat(spec.getText());
                    g = Float.parseFloat(glow.getText());
                    if(s<0 || s>1 || g<0 || g>1){
                        throw new NumberFormatException();
                    }
                    modelEditor.setSpec(g,s);
                    modelEditor.action |= modelEditor.GLOW_BIT;
                }catch(NumberFormatException e){
                    JOptionPane.showMessageDialog(null, "Enter Numbers in Range 0-1 Into Text Fields!");
                }
            }else if(actionEvent.getSource() ==brushVisible){
                modelEditor.displayBrush = brushVisible.isSelected();
                modelEditor.action |= modelEditor.MOVE_BIT;
            }
            else if(actionEvent.getSource() == colorButton){
                try{
                    float v = Float.parseFloat(selectColorVariance.getText());
                    modelEditor.colorVariance = v;
                    if(v<0){
                        throw new NumberFormatException();
                    }
                    Color c = selectColor.getColor();
                    modelEditor.changeSelectedColor(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f);
                    modelEditor.action |= modelEditor.COLOR_BIT;
                }catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Enter a Number Greater Than or Equal to 0 Into Color Variance!");
                }
            }

            else if(actionEvent.getSource() == revolvePreview) {
                if(x.isSelected()) {
                    modelEditor.revolveAxis = modelEditor.X;
                }
                if(y.isSelected()) {
                    modelEditor.revolveAxis = modelEditor.Y;
                }
                if(z.isSelected()) {
                    modelEditor.revolveAxis = modelEditor.Z;
                }
                modelEditor.action |= modelEditor.REVOLVE_BIT;
            }
            else if(actionEvent.getSource() == revolveButton){
                modelEditor.isPaintBrush = paintRevolve.isSelected();
                modelEditor.isSelect = selectRevolve.isSelected();
                modelEditor.action |= modelEditor.BRUSH_BIT;
            }
            else if(actionEvent.getSource() == write){
                if(!hasSaved){

                    String s = JOptionPane.showInputDialog(null, "What file are you writing to?");
                    if(s!=null) {
                        hasSaved = true;
                        modelEditor.fileName = s;
                    }
                }
                modelEditor.action |= modelEditor.SAVE_BIT;
            }
            else if(actionEvent.getSource() == writeAs){

                String s = JOptionPane.showInputDialog(null, "What file are you writing to?");
                if(s!=null) {
                    hasSaved = true;
                    modelEditor.fileName = s;
                    modelEditor.action |= modelEditor.SAVE_BIT;
                }
            }
            else if(actionEvent.getSource() == read){
                String s = JOptionPane.showInputDialog(null, "What file name are you reading?");
                if(s!=null) {
                    modelEditor.fileName = s;
                    modelEditor.action |= modelEditor.READ_BIT;

                    System.out.println("Waiting");
                    boolean waiting = true;
                    modelEditor.loading = true;
                    while(waiting){
                        System.out.println(modelEditor.loading);
                        waiting = modelEditor.loading;

                    }
                    System.out.println("Done Waiting");

                    frameModel.clear();
                    animationModel.clear();
                    currentAnimModel.clear();

                    for(int i=0;i<modelEditor.frames.size();i++){
                        frameModel.add(i,modelEditor.frames.get(i).name);
                    }
                    frames.setSelectedIndex(0);
                    modelEditor.currentFrame = modelEditor.frames.get(0);
                    for(int i=0;i<modelEditor.animations.size();i++){
                        animationModel.add(i,modelEditor.animations.get(i).name);

                    }
                    animations.setSelectedIndex(0);
                    /*
                    modelEditor.currentAnim = modelEditor.animations.get(0);
                    for(int i=0;i<modelEditor.currentAnim.frames.size();i++){
                        currentAnimModel.add(i,(modelEditor.currentAnim.frames.get(i).name + " , " + modelEditor.currentAnim.lengths.get(i)));
                    }
                    */
                }
            }
            else if(actionEvent.getSource() == addFrame){
                if(!isPlaying) {
                    String s = JOptionPane.showInputDialog(null, "Name of new frame:");
                    if (s != null) {
                        boolean dup =false;
                        for(Frame frame : modelEditor.frames){
                            if (s.equals(frame.name)){
                                dup = true;
                            }
                        }
                        if(!dup) {
                            modelEditor.frames.add(frames.getSelectedIndex()+1,new Frame(s));

                            frameModel.add(frames.getSelectedIndex()+1, new String(s));
                        }else{
                            JOptionPane.showMessageDialog(null,"Do not enter duplicate names");
                        }
                    }
                }
            }
            else if(actionEvent.getSource() == duplicateFrame){
                if(!isPlaying) {
                    String s = JOptionPane.showInputDialog(null, "Name of duplicate frame:");
                    if (s != null) {
                        boolean dup =false;
                        for(Frame frame : modelEditor.frames){
                            if (s.equals(frame.name)){
                                dup = true;
                            }
                        }
                        if(!dup) {
                            Frame f = new Frame(s);
                            for(Vert vert:modelEditor.currentFrame.verts){
                                int[] pos = new int[]{(int) (32 * vert.position.x), (int) (32 * vert.position.y), (int) (32 * vert.position.z)};
                                Vert newVert = new Vert(vert);
                                f.verts.add(newVert);
                                f.octVerts.add(newVert,pos);
                            }
                            modelEditor.frames.add(frames.getSelectedIndex()+1,f);

                            frameModel.add(frames.getSelectedIndex()+1, new String(s));
                        }else{
                            JOptionPane.showMessageDialog(null,"Do not enter duplicate names");
                        }
                    }
                }
            }
            else if(actionEvent.getSource() == removeFrame){
                if(!isPlaying) {
                    String frameName= null;
                    if (frameModel.size() == 1) {
                        frameName = modelEditor.currentFrame.name;
                        modelEditor.currentFrame.verts.clear();
                        modelEditor.currentFrame.octVerts.clear();
                        modelEditor.currentFrame.name = "newFrame";

                        frameModel.set(0, "newFrame");
                        frames.setSelectedIndex(0);
                    } else if (frameModel.size() > 1) {
                        int id = frames.getSelectedIndex();
                        frameName = modelEditor.frames.get(id).name;
                        modelEditor.frames.remove(id);
                        modelEditor.currentFrame = modelEditor.frames.get(0);

                        frames.clearSelection();

                        if (id >= 0) {
                            frameModel.remove(id);
                        }
                        frames.setSelectedIndex(0);
                    }
                    if(!modelEditor.animations.isEmpty()) {
                        for (FrameAnimation a:modelEditor.animations) {
                            java.util.List<Frame> removeFrame = new ArrayList<Frame>();
                            for(Frame f:a.frames){
                                if(f.name.equals(frameName)){
                                    int id = a.frames.indexOf(f);
                                    removeFrame.add(a.frames.get(id));
                                    a.lengths.remove(id);
                                }
                            }
                            for(Frame f:removeFrame){
                                a.frames.remove(f);
                            }
                        }
                    }
                    int id = animations.getSelectedIndex();
                    if(id >= 0) {
                        modelEditor.currentAnim = modelEditor.animations.get(id);
                        currentAnimModel.clear();
                        for(int i=0;i<modelEditor.currentAnim.frames.size();i++){
                            currentAnimModel.add(i,(modelEditor.currentAnim.frames.get(i).name + " , " + modelEditor.currentAnim.lengths.get(i)));
                        }
                        currentAnimation.setSelectedIndex(0);
                    }
                }

            }
            else if(actionEvent.getSource() == modifyFrame){
                if(!isPlaying) {
                    String s = JOptionPane.showInputDialog(null, "Name of new frame:");
                    if (s != null) {
                        boolean dup =false;
                        for(Frame frame : modelEditor.frames){
                            if (s.equals(frame.name)){
                                dup = true;
                            }
                        }
                        if(!dup) {
                            modelEditor.currentFrame.name = new String(s);

                            int id = frames.getSelectedIndex();
                            frameModel.set(id, s);

                            int AnimaiID = animations.getSelectedIndex();
                            if (AnimaiID >= 0) {
                                modelEditor.currentAnim = modelEditor.animations.get(AnimaiID);
                                currentAnimModel.clear();
                                for (int i = 0; i < modelEditor.currentAnim.frames.size(); i++) {
                                    currentAnimModel.add(i, (modelEditor.currentAnim.frames.get(i).name + " , " + modelEditor.currentAnim.lengths.get(i)));
                                }
                                currentAnimation.setSelectedIndex(0);
                            }
                        }else{
                            JOptionPane.showMessageDialog(null,"Do not enter duplicate names");
                        }
                    }
                }
            }
            else if(actionEvent.getSource() == addAnimation){
                if(!isPlaying) {
                    String s = JOptionPane.showInputDialog(null, "Name of new animation:");
                    if (s != null) {
                        boolean dup =false;
                        for(FrameAnimation animation : modelEditor.animations){
                            if (s.equals(animation.name)){
                                dup = true;
                            }
                        }
                        if(!dup) {
                            modelEditor.animations.add(new FrameAnimation(s));

                            animationModel.add(animationModel.getSize(), new String(s));
                        }else{
                            JOptionPane.showMessageDialog(null,"Do not enter duplicate names");
                        }
                    }
                }
            }
            else if(actionEvent.getSource() == removeAnimation){
                if(!isPlaying) {
                    if (animationModel.size() == 1) {
                        modelEditor.currentAnim = null;
                        modelEditor.animations.clear();
                        animationModel.clear();
                    } else if (animationModel.size() > 1) {
                        int id = animations.getSelectedIndex();
                        modelEditor.animations.remove(id);
                        modelEditor.currentAnim = modelEditor.animations.get(0);

                        animations.clearSelection();

                        if (id >= 0) {
                            animationModel.remove(id);
                        }
                        animations.setSelectedIndex(0);


                    }
                }

            }
            else if(actionEvent.getSource() == modifyAnimation){
                if(!isPlaying) {
                    String s = JOptionPane.showInputDialog(null, "Name of new animation:");
                    if (s != null) {
                        boolean dup =false;
                        for(FrameAnimation animation : modelEditor.animations){
                            if (s.equals(animation.name)){
                                dup = true;
                            }
                        }
                        if(!dup) {
                            modelEditor.currentAnim.name = new String(s);

                            int id = animations.getSelectedIndex();
                            animationModel.set(id, s);
                        }else{
                            JOptionPane.showMessageDialog(null,"Do not enter duplicate names");
                        }
                    }
                }
            }
            else if(actionEvent.getSource() == addAnimFrame){

                if(!isPlaying) {
                    if (animations.getSelectedIndex() >= 0) {
                        String s = JOptionPane.showInputDialog(null, "Length of time for frame:");
                        if (s != null) {
                            try {
                                float t = Float.parseFloat(s);
                                currentAnimModel.add(currentAnimModel.getSize(), frames.getSelectedValue() + " , " + t);

                                modelEditor.currentAnim.frames.add(modelEditor.currentFrame);
                                modelEditor.currentAnim.lengths.add(t);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Enter a decimal number!");
                            }

                        }
                    }
                }

            }

            else if(actionEvent.getSource() == removeAnimFrame){
                if(!isPlaying) {
                    if (animations.getSelectedIndex() >= 0) {
                        removeAnimFrame(modelEditor.currentAnim, currentAnimation.getSelectedIndex());
                    }
                }

            }
            else if(actionEvent.getSource() == modifyAnimFrame){
                if(!isPlaying) {
                    if (animations.getSelectedIndex() >= 0) {
                        String s = JOptionPane.showInputDialog(null, "Length of time for frame:");
                        if (s != null) {
                            try {
                                float t = Float.parseFloat(s);
                                currentAnimModel.set(currentAnimation.getSelectedIndex(), modelEditor.currentAnim.frames.get(currentAnimation.getSelectedIndex()).name + " , " + t);
                                modelEditor.currentAnim.lengths.set(currentAnimation.getSelectedIndex(), t);
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Enter a decimal number!");
                            }

                        }
                    }
                }
            }
            else  if(actionEvent.getSource() == playAnimation) {
                if(isPlaying){
                    stopPlaying();
                }else{
                    if(modelEditor.currentAnim != null && !modelEditor.currentAnim.frames.isEmpty()) {
                        isPlaying = true;
                        modelEditor.action |= modelEditor.PLAY_BIT;
                        playAnimation.setText("Stop");
                    }
                }
            }else  if(actionEvent.getSource() == bgColor) {
                Vector4f c = modelEditor.updateBgColor;
                Color oldColor = new Color(c.x,c.y,c.z,c.w);
                Color color = bgColorChooser.showDialog(null, "Background Color",oldColor);

                float r = color.getRed() / 255.0f;
                float g = color.getGreen() / 255.0f;
                float b = color.getBlue() / 255.0f;
                float a = color.getAlpha() / 255.0f;
                modelEditor.updateBgColor = new Vector4f(r,g,b,a);
                modelEditor.action |= modelEditor.MOVE_BIT;

            }else  if(actionEvent.getSource() == lColor) {
                Vector3f c = modelEditor.updateLColor;
                Color oldColor = new Color(c.x,c.y,c.z,1);
                Color color = lColorChooser.showDialog(null, "Light Color",oldColor);

                float r = color.getRed() / 255.0f;
                float g = color.getGreen() / 255.0f;
                float b = color.getBlue() / 255.0f;
                float a = color.getAlpha() / 255.0f;
                modelEditor.updateLColor = new Vector3f(r,g,b);
                modelEditor.action |= modelEditor.MOVE_BIT;
            }
        }
    }

    private void removeAnimFrame(FrameAnimation animation, int id){
        if (currentAnimModel.size() == 1) {
            animation.frames.clear();
            animation.lengths.clear();
            currentAnimModel.clear();
        } else if (currentAnimModel.size() > 1) {;
            if (id >= 0) {
                animation.frames.remove(id);
                animation.lengths.remove(id);
                currentAnimModel.remove(id);
            }
            currentAnimation.setSelectedIndex(0);
        }
    }

    private class ListListener implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            if(listSelectionEvent.getSource() == frames){
                int id = frames.getSelectedIndex();
                if(id >= 0) {
                    modelEditor.selected.clear();
                    modelEditor.currentFrame = modelEditor.frames.get(id);
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }
            }

            if(listSelectionEvent.getSource() == animations){
                if(isPlaying){
                    stopPlaying();
                }
                int id = animations.getSelectedIndex();
                if(id >= 0) {
                    modelEditor.currentAnim = modelEditor.animations.get(id);
                    currentAnimModel.clear();
                    for(int i=0;i<modelEditor.currentAnim.frames.size();i++){
                        currentAnimModel.add(i,(modelEditor.currentAnim.frames.get(i).name + " , " + modelEditor.currentAnim.lengths.get(i)));
                    }
                    currentAnimation.setSelectedIndex(0);
                }
            }

        }
    }

    public void stopPlaying(){
        isPlaying = false;
        modelEditor.animFrame = 0;
        modelEditor.action &= ~modelEditor.PLAY_BIT;

        frames.setSelectedIndex(0);
        playAnimation.setText("Play");

        modelEditor.currentFrame = modelEditor.frames.get(0);
        modelEditor.action |= modelEditor.MOVE_BIT;
    }

    private class TabListener implements ChangeListener{

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if(changeEvent.getSource() == tabbedPane){
                if(tabbedPane.getSelectedIndex() == 0){
                    if(isPlaying){
                        stopPlaying();
                    }
                    modelEditor.isBrush = true;
                    modelEditor.action |= modelEditor.MOVE_BIT;
                    modelEditor.displayBrush = brushVisible.isSelected();

                }
                if(tabbedPane.getSelectedIndex() == 1) {
                    if (isPlaying) {
                        stopPlaying();
                    }
                    modelEditor.clearBrush = true;
                    modelEditor.isBrush = false;
                    modelEditor.displayBrush = true;
                    modelEditor.action |= modelEditor.MOVE_BIT;

                }
                if(tabbedPane.getSelectedIndex() == 2){
                    modelEditor.displayBrush = false;
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }

                /*
                if(tabbedPane.getSelectedIndex() != 0){
                    //modelEditor.brush.clear();
                    modelEditor.isBrush = false;
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }else{
                    modelEditor.isBrush = true;
                    modelEditor.action |= modelEditor.MOVE_BIT;
                }
                if(tabbedPane.getSelectedIndex() != 2){

                    if(isPlaying){
                        stopPlaying();
                    }
                }else{
                    modelEditor.displayBrush = false;
                }
                */
            }


        }
    }

    private class FrameListener implements WindowListener{

        @Override
        public void windowOpened(WindowEvent windowEvent) {

        }

        @Override
        public void windowClosing(WindowEvent windowEvent) {
            modelEditor.quit();
            try {
                modelThread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread Inturupted already!");
                e.printStackTrace();
            }
        }

        @Override
        public void windowClosed(WindowEvent windowEvent) {

        }

        @Override
        public void windowIconified(WindowEvent windowEvent) {

        }

        @Override
        public void windowDeiconified(WindowEvent windowEvent) {

        }

        @Override
        public void windowActivated(WindowEvent windowEvent) {

        }

        @Override
        public void windowDeactivated(WindowEvent windowEvent) {

        }
    }
}
