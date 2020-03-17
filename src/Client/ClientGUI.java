/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import ClientsObject.ObjectClients;
import Models.WrapLayout;
import frames.ImgInChat;
import frames.ListFriends;
import frames.viewFile;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.OverlayLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author E6540
 */
public class ClientGUI extends javax.swing.JFrame {

    private String currentFriendUserName = "";

    private final String address = "127.0.0.1";
    private final int port = 14049;
    private Socket sock = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    private String userName;
    private String passWord;

    //Tạo lưu danh sách bạn bè
    private HashMap<String, ListFriends> listFriends = null;

    public ClientGUI(String userName, String passWord) {

        //Reset giao diện java swing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }

        UIManager.put("PopupMenu.border", BorderFactory.createEmptyBorder());

        initComponents();
        panel_StickerContent.setLayout(new WrapLayout(WrapLayout.LEFT, 0, 10));
        //init popup
        initPopUp();

        try {
            this.sock = new Socket(address, port);
            this.userName = userName;
            this.passWord = passWord;
            listFriends = new HashMap<String, ListFriends>();
            this.oos = new ObjectOutputStream(this.sock.getOutputStream());

            ObjectClients objClientLogin = new ObjectClients();
            objClientLogin.setStatus("login");
            objClientLogin.setUserNameSend(this.userName);
            objClientLogin.setPassWord(this.passWord);

            this.oos.writeObject(objClientLogin);
            this.oos.flush();
            Thread t = new Thread(new clientWorker(this.sock, this));
            t.start();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Connect to Server Error!");
            System.exit(0);
        }

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCurrentFriendUserName() {
        return currentFriendUserName;
    }

    public void setCurrentFriendUserName(String currentFriendUserName) {
        this.currentFriendUserName = currentFriendUserName;
    }

    public class clientWorker implements Runnable {

        Socket sock;
        ClientGUI clientGUI = null;

        public clientWorker(Socket sock, ClientGUI client) {
            this.sock = sock;
            this.clientGUI = client;
        }

        @Override
        public void run() {
            try {
                ois = new ObjectInputStream(this.sock.getInputStream());
            } catch (IOException ex) {
                System.out.println("LỖI tạo luồng client");
            }

            try {
                while (true) {
                    ObjectClients objServerResp = (ObjectClients) ois.readObject();
                    String status = objServerResp.getStatus();
                    String userName = objServerResp.getUserNameRecv();
                    String fullName = objServerResp.getFullName();
                    String userStatus = objServerResp.getUserStatus();
                    ImageIcon imgIcon = objServerResp.getAvatar();
                    //Nhận phản hồi login
                    switch (status) {
                        case "resLogin": {
                            String rs = objServerResp.getMessage();
                            if (rs.equals("success")) {
                                JOptionPane.showMessageDialog(null, "Login Success! Welcome to Meow~~~ App", "Login success", JOptionPane.INFORMATION_MESSAGE);
                                this.clientGUI.setVisible(true);
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        if (panel_ListFriends.getComponentCount() > 0) {
                                            ListFriends firstFriend = (ListFriends) panel_ListFriends.getComponent(0);
                                            initChat(firstFriend);
                                        }
                                    }
                                });
                                t.run();

                            } else {
                                JOptionPane.showMessageDialog(null, rs, "Login fail", JOptionPane.ERROR_MESSAGE);
                                Login login = new Login();
                                login.setVisible(true);
                            }
                            break;
                        }
                        //Nhận phản hồi lấy danh sách bạn bè
                        case "friend": {
                            loadFriends(objServerResp);
                            break;
                        }
                        //Nhận phản hồi lấy thông tin cá nhân của mình    
                        case "profile": {
                            loadProfile(objServerResp);
                            break;
                        }
                        //Nhận  phản hồi cập nhật trạng thái của bạn bè
                        case "updateFriendStatus": {
                            updateStatusFriend(userName, userStatus);
                            if (currentFriendUserName.equals(userName)) {
                                setUserStatus(userStatus);
                            }
                            break;
                        }
                        //Nhận phản hồi thêm bạn bè hoặc được bạn bè thêm    
                        case "resAddfriends": {
                            if (objServerResp.getMessage().equals("success")) {
                                if (panel_ListFriends.getComponentCount() == 0) {
                                    currentFriendUserName = userName;
                                    activeChat(fullName, userStatus, imgIcon);
                                }
                                ListFriends addFriend = new ListFriends();
                                addFriend.set(imgIcon, userName, fullName, userStatus);
                                addFriend.setVisible(true);

                                addMouseEventListFriends(addFriend, userName.trim(), fullName, userStatus, imgIcon);

                                panel_ListFriends.add(addFriend);
                                resetPanel(panel_ListFriends);

                                listFriends.put(userName, addFriend);

                                JOptionPane.showMessageDialog(null, "Add new friend success!", "Notification", JOptionPane.INFORMATION_MESSAGE);

                            } else {
                                JOptionPane.showMessageDialog(null, "Can't find username or username was friend!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                            }
                            break;

                        }
                        case "pushfriendtolist": {
                            if (objServerResp.getMessage().equals("success")) {
                                if (panel_ListFriends.getComponentCount() == 0) {
                                    currentFriendUserName = userName;
                                    activeChat(fullName, userStatus, imgIcon);
                                }
                                ListFriends addFriend = new ListFriends();
                                addFriend.set(imgIcon, userName, fullName, userStatus);
                                addFriend.setVisible(true);

                                addMouseEventListFriends(addFriend, userName.trim(), fullName, userStatus, imgIcon);

                                panel_ListFriends.add(addFriend);
                                resetPanel(panel_ListFriends);

                                listFriends.put(userName, addFriend);

                                JOptionPane.showMessageDialog(null, fullName + " add you to their list friends!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "Can't find username or username was friend!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                            }
                            break;
                        }
                        //Nhận phản hồi lấy danh sách tin nhắn của bạn bè    
                        case "resMess": {
                            if (!objServerResp.getMessage().equals("pkg_end")) {
                                hanldeMess(objServerResp, "resMess");
                            } else {
                                resetPanel(panel_ChatLog);
                            }
                            break;
                        }
                        //Tin nhắn mới tới
                        case "newMess": {
                            if (!userName.trim().equals(currentFriendUserName)) {
                                listFriends.get(userName.trim()).setLabel_notification();
                                playSound();
                            } else {
                                hanldeMess(objServerResp, "chat");
                            }
                            break;
                        }
                        case "stop": {
                            JOptionPane.showMessageDialog(null, "The server forced you to stop! You'll exit now!");
                            this.sock.close();
                            System.exit(0);
                        }
                    }

                }
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Server offline");
                Login login = new Login();
                login.setVisible(true);
                this.clientGUI.dispose();
            }
        }

    }

    //Nhạc nền nhận được tin nhắn mới
    public void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = getClass().getResource("/sound/soundRecvNewMess.wav");
                    System.out.println(url);
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    //Vẽ lại JPanel
    public void resetPanel(JPanel jpanel) {
        jpanel.revalidate();
        jpanel.repaint();
    }

    //set thông tin của tôi
    public void loadProfile(ObjectClients objMe) {
        setAva(label_myAvaProfile1, objMe.getAvatar(), 40);
        this.label_myName.setText(objMe.getFullName());
    }

    //Tải list friends
    public void loadFriends(ObjectClients objNewFriend) {
        ListFriends friend = new ListFriends();
        listFriends.put(objNewFriend.getUserNameRecv().trim(), friend);
        ImageIcon imgIcon = objNewFriend.getAvatar();
        String friendUserName = objNewFriend.getUserNameRecv();
        String friendName = objNewFriend.getFullName();
        String friendStatus = objNewFriend.getUserStatus();
        friend.set(imgIcon, friendUserName, friendName, friendStatus);
        addMouseEventListFriends(friend, friendUserName.trim(), friendName, friendStatus, imgIcon);
        this.panel_ListFriends.add(friend);
    }

    //Lấy người đầu tiên trong list friends làm currentFriendUserName
    public void initChat(ListFriends FisrtFriend) {
        showLoading();
        label_yourFriendName.setText(FisrtFriend.getFullName());
        setAva(label_avaYourFriends1, FisrtFriend.getAva(), 40);
        setUserStatus(FisrtFriend.getUserStatus());
        this.currentFriendUserName = FisrtFriend.getFriendUserName().trim();
        FisrtFriend.setBgClicked();
        getMess(this.currentFriendUserName.trim());
    }

    //Set style cho status
    public void setUserStatus(String status) {
        if (status.equals("Online")) {
            label_yourFriendStatus.setForeground(Color.GREEN);
        } else {
            label_yourFriendStatus.setForeground(Color.RED);
        }
        label_yourFriendStatus.setFont(new Font("Arial", Font.BOLD, 12));
        label_yourFriendStatus.setText(status);
    }

    //set avatar
    public void setAva(JLabel ava, ImageIcon imgIcon, int px) {
        Image img;
        if (imgIcon.getIconWidth() > imgIcon.getIconHeight()) {
            img = imgIcon.getImage().getScaledInstance(px, -1, Image.SCALE_SMOOTH);
        } else {
            img = imgIcon.getImage().getScaledInstance(-1, px, Image.SCALE_SMOOTH);
        }
        ava.setIcon(new ImageIcon(img));
    }

    // add event cho mỗi jpanel user 
    public void addMouseEventListFriends(ListFriends friend, String friendUserName, String friendName, String friendStatus, ImageIcon imgIcon) {
        friend.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!friendUserName.trim().equals(currentFriendUserName.trim())) {
                    showLoading();
                    activeChat(friendName, friendStatus, imgIcon);
                    listFriends.get(friendUserName).setBgClicked();
                    listFriends.get(currentFriendUserName.trim()).RemoveBg();
                    currentFriendUserName = friendUserName;
                    listFriends.get(currentFriendUserName.trim()).removeNotification();
                    panel_ChatLog.removeAll();
                    resetPanel(panel_ChatLog);
                    getMess(currentFriendUserName);//yêu cầu lấy tin nhắn của bạn bè khi click vào

                }

            }

        });
    }

    public void activeChat(String Name, String status, ImageIcon imgIcon) {
        label_yourFriendName.setText(Name);
        setUserStatus(status);
        setAva(label_avaYourFriends1, imgIcon, 40);
    }

    public void showLoading() {
        Thread tLoading = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loading.add(loading_icon);
                    loading.show(scollPanel_ChatLog, 0, 0);
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                loading.setVisible(false);
            }
        });
        tLoading.start();

    }

    //Kéo thanh cuộn xuống cuối
    public void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    //Yêu cầu server gửi mess theo tên người bạn
    public void getMess(String friend) {
        ObjectClients objGetMess = new ObjectClients();
        objGetMess.setStatus("getMess");
        objGetMess.setUserNameSend(this.userName);
        objGetMess.setUserNameRecv(friend);
        try {
            oos.writeObject(objGetMess);
            oos.flush();

        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Cập nhật trạng thái của bạn bè online/offline
    public void updateStatusFriend(String friendUserName, String status) {
        ListFriends f = listFriends.get(friendUserName);
        f.updateStatus(status);
        resetPanel(panel_ListFriends);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popup_MoreFe = new javax.swing.JPopupMenu();
        panel_popup = new javax.swing.JPanel();
        label_popup_file = new javax.swing.JLabel();
        label_popup_img = new javax.swing.JLabel();
        label_popup_sticker = new javax.swing.JLabel();
        loading = new javax.swing.JPopupMenu();
        loading_icon = new javax.swing.JPanel();
        label_loading = new javax.swing.JLabel();
        popup_emoji = new javax.swing.JPopupMenu();
        panel_emoji = new javax.swing.JPanel();
        popup_sticker = new javax.swing.JPopupMenu();
        splitPane_sticker = new javax.swing.JSplitPane();
        scollPane_StickerTitle = new javax.swing.JScrollPane();
        panel_StickerTitle = new javax.swing.JPanel();
        scollPanel_StickerContent = new javax.swing.JScrollPane();
        panel_StickerContent = new javax.swing.JPanel();
        panel_Main = new javax.swing.JPanel();
        panel_Left = new javax.swing.JPanel();
        panel_Profile = new javax.swing.JPanel();
        layerPane_Profile = new javax.swing.JLayeredPane();
        boder_label_myAvaProfile = new javax.swing.JLabel();
        label_myAvaProfile1 = new javax.swing.JLabel();
        label_myName = new javax.swing.JLabel();
        label_addFriends = new javax.swing.JLabel();
        panel_line = new javax.swing.JPanel();
        panel_searchFriends = new javax.swing.JPanel();
        txt_searchFriends = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        scollPane_ListFriends = new javax.swing.JScrollPane();
        panel_ListFriends = new javax.swing.JPanel();
        panel_Right = new javax.swing.JPanel();
        panel_topBar = new javax.swing.JPanel();
        layerPane_Friends = new javax.swing.JLayeredPane();
        border_label_avaYourFriends = new javax.swing.JLabel();
        label_avaYourFriends1 = new javax.swing.JLabel();
        panel_FriendsInfo = new javax.swing.JPanel();
        label_yourFriendName = new javax.swing.JLabel();
        label_yourFriendStatus = new javax.swing.JLabel();
        panel_Media = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        scollPanel_ChatLog = new javax.swing.JScrollPane();
        panel_ChatLog = new javax.swing.JPanel();
        panel_Message = new javax.swing.JPanel();
        label_moreFeature = new javax.swing.JLabel();
        panel_WriteMessage = new javax.swing.JPanel();
        txt_WriteMessage = new javax.swing.JTextField();
        btn_icon = new javax.swing.JLabel();
        btn_SendMessage = new javax.swing.JLabel();

        popup_MoreFe.setBackground(new java.awt.Color(255, 255, 255));
        popup_MoreFe.setAlignmentY(0.0F);
        popup_MoreFe.setBorder(null);
        popup_MoreFe.setBorderPainted(false);

        panel_popup.setBackground(new java.awt.Color(255, 255, 255));
        panel_popup.setAlignmentY(0.0F);
        panel_popup.setMaximumSize(new java.awt.Dimension(34, 120));
        panel_popup.setMinimumSize(new java.awt.Dimension(34, 120));
        panel_popup.setPreferredSize(new java.awt.Dimension(34, 120));
        panel_popup.setLayout(new javax.swing.BoxLayout(panel_popup, javax.swing.BoxLayout.Y_AXIS));

        label_popup_file.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/popup_file.png"))); // NOI18N
        label_popup_file.setToolTipText("Choose File");
        label_popup_file.setAlignmentY(0.0F);
        label_popup_file.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_popup_file.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_popup_fileMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_popup_fileMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_popup_fileMouseExited(evt);
            }
        });
        panel_popup.add(label_popup_file);

        label_popup_img.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/popup_img.png"))); // NOI18N
        label_popup_img.setToolTipText("Choose Image");
        label_popup_img.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_popup_img.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_popup_imgMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_popup_imgMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_popup_imgMouseExited(evt);
            }
        });
        panel_popup.add(label_popup_img);

        label_popup_sticker.setBackground(new java.awt.Color(255, 255, 255));
        label_popup_sticker.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/popup_sticker.png"))); // NOI18N
        label_popup_sticker.setToolTipText("Choose Emoji");
        label_popup_sticker.setAlignmentY(0.0F);
        label_popup_sticker.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_popup_sticker.setOpaque(true);
        label_popup_sticker.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_popup_stickerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_popup_stickerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_popup_stickerMouseExited(evt);
            }
        });
        panel_popup.add(label_popup_sticker);

        loading.setBackground(new java.awt.Color(255, 255, 255));
        loading.setBorder(null);
        loading.setMinimumSize(new java.awt.Dimension(800, 495));
        loading.setPreferredSize(new java.awt.Dimension(800, 495));

        loading_icon.setBackground(new java.awt.Color(255, 255, 255));
        loading_icon.setMaximumSize(new java.awt.Dimension(800, 495));
        loading_icon.setMinimumSize(new java.awt.Dimension(800, 495));
        loading_icon.setPreferredSize(new java.awt.Dimension(800, 495));
        loading_icon.setLayout(new javax.swing.BoxLayout(loading_icon, javax.swing.BoxLayout.Y_AXIS));

        label_loading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label_loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/Loading.gif"))); // NOI18N
        label_loading.setMaximumSize(new java.awt.Dimension(800, 495));
        label_loading.setMinimumSize(new java.awt.Dimension(800, 495));
        label_loading.setPreferredSize(new java.awt.Dimension(800, 495));
        loading_icon.add(label_loading);

        panel_emoji.setMaximumSize(new java.awt.Dimension(260, 260));
        panel_emoji.setMinimumSize(new java.awt.Dimension(260, 260));
        panel_emoji.setPreferredSize(new java.awt.Dimension(260, 260));
        panel_emoji.setLayout(new java.awt.GridLayout(6, 6, 5, 5));

        splitPane_sticker.setBackground(new java.awt.Color(255, 255, 255));
        splitPane_sticker.setBorder(null);
        splitPane_sticker.setDividerLocation(45);
        splitPane_sticker.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane_sticker.setMaximumSize(new java.awt.Dimension(320, 370));
        splitPane_sticker.setMinimumSize(new java.awt.Dimension(320, 370));
        splitPane_sticker.setPreferredSize(new java.awt.Dimension(320, 370));

        scollPane_StickerTitle.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scollPane_StickerTitle.setMaximumSize(new java.awt.Dimension(0, 0));
        scollPane_StickerTitle.setMinimumSize(new java.awt.Dimension(0, 0));
        scollPane_StickerTitle.setPreferredSize(new java.awt.Dimension(0, 0));

        panel_StickerTitle.setBackground(new java.awt.Color(255, 255, 255));
        panel_StickerTitle.setMaximumSize(new java.awt.Dimension(32767, 40));
        panel_StickerTitle.setMinimumSize(new java.awt.Dimension(0, 0));
        panel_StickerTitle.setPreferredSize(new java.awt.Dimension(0, 0));
        panel_StickerTitle.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));
        scollPane_StickerTitle.setViewportView(panel_StickerTitle);

        splitPane_sticker.setTopComponent(scollPane_StickerTitle);

        scollPanel_StickerContent.setBorder(null);

        panel_StickerContent.setBackground(new java.awt.Color(255, 255, 255));
        panel_StickerContent.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                panel_StickerContentMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panel_StickerContentLayout = new javax.swing.GroupLayout(panel_StickerContent);
        panel_StickerContent.setLayout(panel_StickerContentLayout);
        panel_StickerContentLayout.setHorizontalGroup(
            panel_StickerContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );
        panel_StickerContentLayout.setVerticalGroup(
            panel_StickerContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );

        scollPanel_StickerContent.setViewportView(panel_StickerContent);

        splitPane_sticker.setRightComponent(scollPanel_StickerContent);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Meoww~~ App Client");
        setBackground(new java.awt.Color(255, 255, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        panel_Main.setBackground(new java.awt.Color(255, 255, 255));
        panel_Main.setPreferredSize(new java.awt.Dimension(1080, 622));

        panel_Left.setBackground(new java.awt.Color(255, 255, 255));
        panel_Left.setMaximumSize(new java.awt.Dimension(260, 32820));
        panel_Left.setMinimumSize(new java.awt.Dimension(260, 0));
        panel_Left.setPreferredSize(new java.awt.Dimension(260, 93));
        panel_Left.setLayout(new javax.swing.BoxLayout(panel_Left, javax.swing.BoxLayout.PAGE_AXIS));

        panel_Profile.setBackground(new java.awt.Color(191, 226, 202));
        panel_Profile.setMaximumSize(new java.awt.Dimension(260, 50));
        panel_Profile.setMinimumSize(new java.awt.Dimension(250, 50));
        panel_Profile.setPreferredSize(new java.awt.Dimension(260, 50));
        panel_Profile.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 5));

        layerPane_Profile.setLayout(new javax.swing.OverlayLayout(layerPane_Profile));

        boder_label_myAvaProfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/border_ava_profile.png"))); // NOI18N
        boder_label_myAvaProfile.setToolTipText("EditProfile");
        layerPane_Profile.add(boder_label_myAvaProfile);

        label_myAvaProfile1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/ava_profile.png"))); // NOI18N
        label_myAvaProfile1.setToolTipText("Edit Profile");
        label_myAvaProfile1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        layerPane_Profile.add(label_myAvaProfile1);

        panel_Profile.add(layerPane_Profile);

        label_myName.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        label_myName.setText("TONY QUANG");
        label_myName.setMaximumSize(new java.awt.Dimension(170, 40));
        label_myName.setMinimumSize(new java.awt.Dimension(0, 40));
        label_myName.setPreferredSize(new java.awt.Dimension(170, 40));
        panel_Profile.add(label_myName);

        label_addFriends.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/addFr.png"))); // NOI18N
        label_addFriends.setToolTipText("AddFriends");
        label_addFriends.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_addFriends.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        label_addFriends.setPreferredSize(new java.awt.Dimension(30, 30));
        label_addFriends.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_addFriendsMouseClicked(evt);
            }
        });
        panel_Profile.add(label_addFriends);

        panel_Left.add(panel_Profile);

        panel_line.setBackground(new java.awt.Color(0, 153, 51));
        panel_line.setMaximumSize(new java.awt.Dimension(260, 3));
        panel_line.setMinimumSize(new java.awt.Dimension(250, 0));
        panel_line.setPreferredSize(new java.awt.Dimension(260, 3));

        javax.swing.GroupLayout panel_lineLayout = new javax.swing.GroupLayout(panel_line);
        panel_line.setLayout(panel_lineLayout);
        panel_lineLayout.setHorizontalGroup(
            panel_lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );
        panel_lineLayout.setVerticalGroup(
            panel_lineLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 3, Short.MAX_VALUE)
        );

        panel_Left.add(panel_line);

        panel_searchFriends.setBackground(new java.awt.Color(191, 226, 202));
        panel_searchFriends.setMaximumSize(new java.awt.Dimension(32767, 40));
        panel_searchFriends.setPreferredSize(new java.awt.Dimension(250, 40));
        panel_searchFriends.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        txt_searchFriends.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_searchFriends.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txt_searchFriends.setBorder(null);
        txt_searchFriends.setPreferredSize(new java.awt.Dimension(195, 30));
        panel_searchFriends.add(txt_searchFriends);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/searchFriends.png"))); // NOI18N
        panel_searchFriends.add(jLabel6);

        panel_Left.add(panel_searchFriends);

        scollPane_ListFriends.setBorder(null);

        panel_ListFriends.setBackground(new java.awt.Color(191, 226, 202));
        panel_ListFriends.setLayout(new javax.swing.BoxLayout(panel_ListFriends, javax.swing.BoxLayout.Y_AXIS));
        scollPane_ListFriends.setViewportView(panel_ListFriends);

        panel_Left.add(scollPane_ListFriends);

        panel_Right.setBackground(new java.awt.Color(255, 255, 255));
        panel_Right.setLayout(new javax.swing.BoxLayout(panel_Right, javax.swing.BoxLayout.Y_AXIS));

        panel_topBar.setBackground(new java.awt.Color(255, 255, 255));
        panel_topBar.setMaximumSize(new java.awt.Dimension(32767, 52));
        panel_topBar.setMinimumSize(new java.awt.Dimension(100, 52));
        panel_topBar.setLayout(new javax.swing.BoxLayout(panel_topBar, javax.swing.BoxLayout.LINE_AXIS));

        layerPane_Friends.setLayout(new javax.swing.OverlayLayout(layerPane_Friends));

        border_label_avaYourFriends.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/border_ava_friends.png"))); // NOI18N
        layerPane_Friends.add(border_label_avaYourFriends);

        label_avaYourFriends1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/ava_profile.png"))); // NOI18N
        layerPane_Friends.add(label_avaYourFriends1);

        panel_topBar.add(layerPane_Friends);

        panel_FriendsInfo.setBackground(new java.awt.Color(255, 255, 255));
        panel_FriendsInfo.setMaximumSize(new java.awt.Dimension(32767, 52));
        panel_FriendsInfo.setMinimumSize(new java.awt.Dimension(100, 52));
        panel_FriendsInfo.setPreferredSize(new java.awt.Dimension(600, 52));
        panel_FriendsInfo.setLayout(new javax.swing.BoxLayout(panel_FriendsInfo, javax.swing.BoxLayout.Y_AXIS));

        label_yourFriendName.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        label_yourFriendName.setText("YOUR FRIEND NAME");
        label_yourFriendName.setMaximumSize(new java.awt.Dimension(500, 30));
        label_yourFriendName.setMinimumSize(new java.awt.Dimension(100, 26));
        label_yourFriendName.setPreferredSize(new java.awt.Dimension(400, 30));
        panel_FriendsInfo.add(label_yourFriendName);

        label_yourFriendStatus.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        label_yourFriendStatus.setText("Active ");
        label_yourFriendStatus.setMaximumSize(new java.awt.Dimension(500, 15));
        panel_FriendsInfo.add(label_yourFriendStatus);

        panel_topBar.add(panel_FriendsInfo);

        panel_Media.setBackground(new java.awt.Color(255, 255, 255));
        panel_Media.setMaximumSize(new java.awt.Dimension(32767, 52));
        panel_Media.setMinimumSize(new java.awt.Dimension(100, 52));
        panel_Media.setPreferredSize(new java.awt.Dimension(140, 52));
        panel_Media.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 7));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/VideoCall.png"))); // NOI18N
        jLabel3.setToolTipText("Make Video Call");
        jLabel3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel3.setMaximumSize(new java.awt.Dimension(30, 30));
        jLabel3.setPreferredSize(new java.awt.Dimension(30, 30));
        panel_Media.add(jLabel3);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/PhoneCall.png"))); // NOI18N
        jLabel4.setToolTipText("Make Phone Call");
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        panel_Media.add(jLabel4);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/option.png"))); // NOI18N
        jLabel5.setToolTipText("More");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.setMinimumSize(new java.awt.Dimension(0, 30));
        panel_Media.add(jLabel5);

        panel_topBar.add(panel_Media);

        panel_Right.add(panel_topBar);

        scollPanel_ChatLog.setBorder(null);

        panel_ChatLog.setBackground(new java.awt.Color(255, 255, 255));
        panel_ChatLog.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                panel_ChatLogMouseMoved(evt);
            }
        });
        panel_ChatLog.setLayout(new javax.swing.BoxLayout(panel_ChatLog, javax.swing.BoxLayout.Y_AXIS));
        scollPanel_ChatLog.setViewportView(panel_ChatLog);

        panel_Right.add(scollPanel_ChatLog);

        panel_Message.setBackground(new java.awt.Color(255, 255, 255));
        panel_Message.setMaximumSize(new java.awt.Dimension(32767, 60));
        panel_Message.setPreferredSize(new java.awt.Dimension(841, 60));
        panel_Message.setLayout(new javax.swing.BoxLayout(panel_Message, javax.swing.BoxLayout.LINE_AXIS));

        label_moreFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/moreFeature.png"))); // NOI18N
        label_moreFeature.setToolTipText("More Feature");
        label_moreFeature.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_moreFeature.setMaximumSize(new java.awt.Dimension(34, 60));
        label_moreFeature.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_moreFeatureMouseClicked(evt);
            }
        });
        panel_Message.add(label_moreFeature);

        panel_WriteMessage.setBackground(new java.awt.Color(255, 255, 255));

        txt_WriteMessage.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        txt_WriteMessage.setToolTipText("Enter your message");
        txt_WriteMessage.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 102), 1, true));
        txt_WriteMessage.setMaximumSize(new java.awt.Dimension(2147483647, 20));
        txt_WriteMessage.setMinimumSize(new java.awt.Dimension(2, 20));
        txt_WriteMessage.setPreferredSize(new java.awt.Dimension(2, 20));
        txt_WriteMessage.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txt_WriteMessageKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout panel_WriteMessageLayout = new javax.swing.GroupLayout(panel_WriteMessage);
        panel_WriteMessage.setLayout(panel_WriteMessageLayout);
        panel_WriteMessageLayout.setHorizontalGroup(
            panel_WriteMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_WriteMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt_WriteMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 707, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel_WriteMessageLayout.setVerticalGroup(
            panel_WriteMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_WriteMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txt_WriteMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        panel_Message.add(panel_WriteMessage);

        btn_icon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btn_icon_normal.png"))); // NOI18N
        btn_icon.setToolTipText("Icon");
        btn_icon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_icon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_iconMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn_iconMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn_iconMouseExited(evt);
            }
        });
        panel_Message.add(btn_icon);

        btn_SendMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/send.png"))); // NOI18N
        btn_SendMessage.setToolTipText("Send Message");
        btn_SendMessage.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_SendMessage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_SendMessageMouseClicked(evt);
            }
        });
        panel_Message.add(btn_SendMessage);

        panel_Right.add(panel_Message);

        javax.swing.GroupLayout panel_MainLayout = new javax.swing.GroupLayout(panel_Main);
        panel_Main.setLayout(panel_MainLayout);
        panel_MainLayout.setHorizontalGroup(
            panel_MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_MainLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(panel_Left, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(panel_Right, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                .addGap(10, 10, 10))
        );
        panel_MainLayout.setVerticalGroup(
            panel_MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_MainLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(panel_MainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel_Right, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(panel_Left, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(panel_Main, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(panel_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int rs = JOptionPane.showConfirmDialog(null, "Do you want to close Chat app?", "Warring!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rs == JOptionPane.YES_OPTION) {
            this.setVisible(false);
            try {
                Thread.sleep(2000);

            } catch (InterruptedException ex) {
                Logger.getLogger(ClientGUI.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing
    //Gửi yêu cầu kết bạn
    private void label_addFriendsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_addFriendsMouseClicked
        String userNameNewFriend = "";
        userNameNewFriend = JOptionPane.showInputDialog(null, "Please enter new friends's user name", "Add new friend", JOptionPane.INFORMATION_MESSAGE);
        if (userNameNewFriend.equals("")) {
            return;
        } else {
            ObjectClients objAddFriend = new ObjectClients();
            objAddFriend.setStatus("addfriend");
            objAddFriend.setUserNameSend(this.userName);//User name của bạn
            objAddFriend.setUserNameRecv(userNameNewFriend); //User name của người kết bạn
            try {
                this.oos.writeObject(objAddFriend);
                this.oos.flush();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
    }//GEN-LAST:event_label_addFriendsMouseClicked
    //Cắt chuỗi cho xuống dòng Mess text nếu quá dài
    public String splitMess(String Mess) {
        int charPerLine = 65;
        Mess += " ";
        String splMess = "";
        int line = 1;

        int messLength = Mess.length();
        if (messLength <= charPerLine) {
            return String.valueOf(line) + Mess;
        } else {
            int flag = 0;
            while ((flag != messLength)) {
                if (messLength - flag <= charPerLine) {
                    splMess += Mess.substring(flag).trim();
                    return String.valueOf(line) + splMess;
                }
                String temp = Mess.substring(flag, flag + charPerLine);
                int indexSpace = temp.lastIndexOf(" ");
                if (indexSpace != -1) {
                    splMess += temp.substring(0, indexSpace) + "\n";
                    flag += indexSpace + 1;
                } else {
                    splMess += temp + "\n";;
                    flag = flag + charPerLine + 1;
                }
                line++;
            }
        }
        return String.valueOf(line) + splMess;
    }

    public JPanel myMess(ObjectClients objMess) {
        String Mess = objMess.getMessage();
        String sign = Character.toString(Mess.charAt(0));
        String username = objMess.getUserNameRecv().trim();

        JTextPane textPane = null;

        ImgInChat imgInChat = null;
        viewFile vF = null;
        JLabel icon = new JLabel();

        int height = 40;
        int width = 200;
        //Xử lý gói message
        switch (sign) {
            case "M": {
                textPane = new JTextPane();
                textPane.setEditable(false);
                StyledDocument doc = textPane.getStyledDocument();
                //Cắt chuỗi
                String rs = splitMess(Mess);
                Mess = rs.substring(2).trim();
                Style style = textPane.addStyle("I'm a Style", null);

                //SET STYLE FOR MESS
                SimpleAttributeSet aligment = new SimpleAttributeSet();
                StyleConstants.setAlignment(aligment, StyleConstants.ALIGN_JUSTIFIED);
                doc.setParagraphAttributes(0, doc.getLength(), aligment, false);
                StyleConstants.setFontFamily(style, "Arial");
                StyleConstants.setBold(style, true);
                StyleConstants.setFontSize(style, 20);
                StyleConstants.setForeground(style, new Color(19, 51, 55));

                if (username.equals(this.userName)) {
                    StyleConstants.setForeground(style, new Color(45, 51, 124));
                } else {
                    StyleConstants.setForeground(style, new Color(66, 135, 245));
                }
                try {
                    doc.insertString(doc.getLength(), Mess.trim(), style);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                height = (int) Math.round(textPane.getPreferredSize().getHeight());
                width = (int) Math.round(textPane.getPreferredSize().getWidth());
                textPane.setMaximumSize(textPane.getPreferredSize());
                break;
            }
            case "P": {
                imgInChat = new ImgInChat();
                ImageIcon imgIcon = new ImageIcon(objMess.getFile());
                if (imgIcon != null) {
                    imgInChat.setPhoto(imgIcon, panel_ChatLog);
                }
                imgInChat.setVisible(true);
                break;
            }
            case "F": {
                vF = new viewFile();
                vF.setFile(objMess.getFile(), Mess);
                vF.setVisible(true);
                break;
            }
            case "I": {
                icon.setPreferredSize(new Dimension(40, 40));
                String path = "/emoji/" + Mess.substring(1);
                icon.setIcon(new ImageIcon(getClass().getResource(path)));
                break;
            }
            case "S": {
                icon.setPreferredSize(new Dimension(150, 150));
                String path = "/sticker/" + Mess.substring(1);

                icon.setIcon(new ImageIcon(getClass().getResource(path)));
                break;
            }
        }
        //Xử lý gói message XONG

        //Xử lý ava
        JLabel ava = null;
        if (username.equals(this.userName)) {
            ava = new JLabel(label_myAvaProfile1.getIcon());
        } else {
            ava = new JLabel(label_avaYourFriends1.getIcon());
        }
        JLabel borderAva = new JLabel(border_label_avaYourFriends.getIcon());
        if (height < 40) {
            ava.setPreferredSize(new Dimension(40, 40));
            borderAva.setPreferredSize(new Dimension(40, 40));
        } else {
            ava.setPreferredSize(new Dimension(40, height));
            borderAva.setPreferredSize(new Dimension(40, height));
        }
        pack();
        JLayeredPane lpAva = new JLayeredPane();
        lpAva.setLayout(new OverlayLayout(lpAva));
        lpAva.add(borderAva);
        lpAva.add(ava);
        //Xử lý ava XONG

        //=======Xử lý thêm vào ô chat===========
        //====tạo mới jpanel==========
        JPanel panelChat = new JPanel();
        panelChat.setLayout(new BoxLayout(panelChat, BoxLayout.X_AXIS));
        panelChat.setBackground(Color.WHITE);

        //panelChat.setMaximumSize(new Dimension(30000, height));
        //====tạo mới jpanel XONG==========
        //So sánh căn lề
        //Nếu là tôi thì căn lề phải
        //Nếu là bạn thì căn lề trái
        if (username.equals(this.userName)) {
            panelChat.add(Box.createHorizontalGlue());
            //xử lý add loại mess nào vào

            if (sign.equals("M")) {
                panelChat.add(textPane);
            } else if (sign.equals("P")) {
                panelChat.add(imgInChat);
            } else if (sign.equals("F")) {
                panelChat.add(vF);
            } else if (sign.equals("I") || sign.equals("S")) {
                panelChat.add(icon);
            }
//            panelChat.add(Box.createHorizontalStrut(10));
            panelChat.add(lpAva);
        } else if (username.equals(currentFriendUserName)) {
            panelChat.add(lpAva);

//            panelChat.add(Box.createHorizontalStrut(10));
            //xử lý add loại mess nào vào
            if (sign.equals("M")) {
                panelChat.add(textPane);
            } else if (sign.equals("P")) {
                panelChat.add(imgInChat);
            } else if (sign.equals("F")) {
                panelChat.add(vF);
            } else if (sign.equals("I") || sign.equals("S")) {
                panelChat.add(icon);
            }
            panelChat.add(Box.createHorizontalGlue());

        }

        resetPanel(panelChat);
        return panelChat;

    }

    //Sử lý tin nhắn
    public void hanldeMess(ObjectClients objMess, String action) {

        JPanel panelChat = new JPanel();
        panelChat = myMess(objMess);

        if (panelChat != null) {
            panel_ChatLog.add(panelChat);
            panel_ChatLog.add(Box.createVerticalStrut(20));
            if (action.equals("chat")) {
                resetPanel(panel_ChatLog); // scrollToBottom(scollPanel_ChatLog);
            }
            pack();// tự tính toán lại kích thước chuẩn
            scrollToBottom(scollPanel_ChatLog);
        }

    }

    public void initEmoji() {
        //File folder = (new File(getClass().getResource("/my/path").toURI())).listFiles();

        try {
            File[] listEmoji = (new File(getClass().getResource("/emoji").toURI())).listFiles();
            for (File e : listEmoji) {
                JLabel label_emoji = new JLabel();
                label_emoji.setPreferredSize(new Dimension(40, 40));
                label_emoji.setIcon(new ImageIcon(e.getAbsolutePath()));
                label_emoji.setCursor(new Cursor(12));
                label_emoji.setName(e.getAbsolutePath());
                label_emoji.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JLabel label = (JLabel) e.getComponent();
                        handleIS(label, "I");
                    }

                });
                panel_emoji.add(label_emoji);
            }
            resetPanel(panel_emoji);
            popup_emoji.add(panel_emoji);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void btn_SendMessageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_SendMessageMouseClicked
        sendMess();
    }//GEN-LAST:event_btn_SendMessageMouseClicked

    private void txt_WriteMessageKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_WriteMessageKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sendMess();
        }
    }//GEN-LAST:event_txt_WriteMessageKeyPressed

    private void label_moreFeatureMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_moreFeatureMouseClicked
        popup_MoreFe.show(label_moreFeature, 0, -125);
    }//GEN-LAST:event_label_moreFeatureMouseClicked

    private void panel_ChatLogMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel_ChatLogMouseMoved
        scollPanel_ChatLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scollPanel_ChatLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }//GEN-LAST:event_panel_ChatLogMouseMoved

    private void label_popup_fileMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_fileMouseEntered
        label_popup_file.setIcon(new ImageIcon(getClass().getResource("/Image/popup_file_hover.png")));
    }//GEN-LAST:event_label_popup_fileMouseEntered

    private void label_popup_fileMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_fileMouseExited
        label_popup_file.setIcon(new ImageIcon(getClass().getResource("/Image/popup_file.png")));
    }//GEN-LAST:event_label_popup_fileMouseExited

    private void label_popup_imgMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_imgMouseEntered
        label_popup_img.setIcon(new ImageIcon(getClass().getResource("/Image/popup_img_hover.png")));
    }//GEN-LAST:event_label_popup_imgMouseEntered

    private void label_popup_imgMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_imgMouseExited
        label_popup_img.setIcon(new ImageIcon(getClass().getResource("/Image/popup_img.png")));
    }//GEN-LAST:event_label_popup_imgMouseExited

    private void label_popup_stickerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_stickerMouseEntered
        label_popup_sticker.setIcon(new ImageIcon(getClass().getResource("/Image/popup_sticker_hover.png")));
    }//GEN-LAST:event_label_popup_stickerMouseEntered

    private void label_popup_stickerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_stickerMouseExited
        label_popup_sticker.setIcon(new ImageIcon(getClass().getResource("/Image/popup_sticker.png")));
    }//GEN-LAST:event_label_popup_stickerMouseExited

    private void label_popup_fileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_fileMouseClicked
        popup_MoreFe.setVisible(false);
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setMultiSelectionEnabled(true);
        int returnValue = jfc.showOpenDialog(panel_ChatLog);

        ObjectClients objSendFiles = null;

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //ArrayList<String> listFilesName = new ArrayList<>();
            File[] files = jfc.getSelectedFiles();

            for (File file : files) {
                objSendFiles = new ObjectClients();
                objSendFiles.setStatus("chat");
                objSendFiles.setUserNameSend(this.userName);
                objSendFiles.setUserNameRecv(this.currentFriendUserName);
                byte[] messFile = readBytesFromFile(file);
                objSendFiles.setFile(messFile);
                String fileName = file.getName();
                objSendFiles.setMessage("F" + fileName);
                try {
                    oos.writeObject(objSendFiles);
                    oos.flush();
                    objSendFiles.setUserNameRecv(this.userName);
                    hanldeMess(objSendFiles, "chat");
                } catch (Exception e) {
                }
                //System.out.println(file.getName());
            }

        }
    }//GEN-LAST:event_label_popup_fileMouseClicked

    private void label_popup_imgMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_imgMouseClicked

        popup_MoreFe.setVisible(false);
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "bmp", "jpeg"));
        jfc.setAcceptAllFileFilterUsed(true);
        jfc.setMultiSelectionEnabled(true);
        int returnValue = jfc.showOpenDialog(panel_ChatLog);

        ObjectClients objSendFiles = null;

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //ArrayList<String> listFilesName = new ArrayList<>();
            File[] files = jfc.getSelectedFiles();

            for (File file : files) {
                objSendFiles = new ObjectClients();
                objSendFiles.setStatus("chat");
                objSendFiles.setUserNameSend(this.userName);
                objSendFiles.setUserNameRecv(this.currentFriendUserName);
                byte[] messFile = readBytesFromFile(file);
                objSendFiles.setFile(messFile);
                String fileName = file.getName();
                objSendFiles.setMessage("P" + fileName);
                try {
                    oos.writeObject(objSendFiles);
                    oos.flush();
                    objSendFiles.setUserNameRecv(this.userName);
                    hanldeMess(objSendFiles, "chat");
                } catch (Exception e) {
                }
                //System.out.println(file.getName());
            }

        }

    }//GEN-LAST:event_label_popup_imgMouseClicked

    private void btn_iconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_iconMouseClicked
        popup_emoji.show(btn_icon, -240, -270);
    }//GEN-LAST:event_btn_iconMouseClicked

    private void btn_iconMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_iconMouseEntered
        this.btn_icon.setIcon(new ImageIcon(getClass().getResource("/Image/btn_icon_hover.png")));
    }//GEN-LAST:event_btn_iconMouseEntered

    private void btn_iconMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_iconMouseExited
        this.btn_icon.setIcon(new ImageIcon(getClass().getResource("/Image/btn_icon_normal.png")));
    }//GEN-LAST:event_btn_iconMouseExited

    private void label_popup_stickerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_popup_stickerMouseClicked
        popup_sticker.show(label_moreFeature, 0, -365);
    }//GEN-LAST:event_label_popup_stickerMouseClicked

    private void panel_StickerContentMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel_StickerContentMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_panel_StickerContentMouseMoved

    //Đọc file to byte[]
    public byte[] readBytesFromFile(File file) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;

    }

    public void sendMess() {
        String txtMess = txt_WriteMessage.getText();
        if (!txtMess.equals("")) {
            ObjectClients objTxtMess = new ObjectClients();
            objTxtMess.setStatus("chat");
            objTxtMess.setMessage("M" + txtMess);
            objTxtMess.setUserNameSend(this.userName);
            objTxtMess.setUserNameRecv(currentFriendUserName);
            try {
                oos.writeObject(objTxtMess);
                oos.flush();
                //Sau khi gửi thì cập nhật lại setUserNameRecv cho handleMess sử lý thêm vào
                objTxtMess.setUserNameRecv(this.userName);
                txt_WriteMessage.setText("");
                hanldeMess(objTxtMess, "chat");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initSticker() {

        try {
            File[] dir = (new File(getClass().getResource("/sticker").toURI())).listFiles();
            for (File f : dir) {
                JLabel labelTitleSticker = new JLabel();
                labelTitleSticker.setPreferredSize(new Dimension(40, 40));
                String stickerName = f.getName();
                String path = f.getAbsolutePath() + "\\" + stickerName + ".png";
                labelTitleSticker.setIcon(new ImageIcon(path));
                labelTitleSticker.setCursor(new Cursor(12));

                labelTitleSticker.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        setSticker(stickerName);
                    }

                });

                panel_StickerTitle.add(labelTitleSticker);
            }
            resetPanel(panel_StickerTitle);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Xử lý gửi sticker hoặc icon
    public void handleIS(JLabel label, String type) {
        String Path = label.getName();
        String Name = Path.substring(Path.lastIndexOf("\\") + 1);

        ObjectClients objIcon = new ObjectClients();
        objIcon.setStatus("chat");

        objIcon.setMessage(type + Name);
        objIcon.setUserNameSend(userName);
        objIcon.setUserNameRecv(currentFriendUserName);
        try {
            oos.writeObject(objIcon);
            oos.flush();
            objIcon.setUserNameRecv(userName);
            hanldeMess(objIcon, "chat");
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setSticker(String stickerName) {
        panel_StickerContent.removeAll();
        try {
            String path = "/sticker/" + stickerName + "/";
            File[] listSticker = (new File(getClass().getResource(path).toURI())).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.equals(stickerName + ".png")) {
                        return false;
                    }
                    return true;
                }
            ;
            });
            for (File f : listSticker) {
                JLabel labelSticker = new JLabel();
                labelSticker.setPreferredSize(new Dimension(100, 100));
                ImageIcon imgIcon = new ImageIcon(f.getAbsolutePath());
                Image img = imgIcon.getImage().getScaledInstance(100, -1, Image.SCALE_SMOOTH);
                labelSticker.setIcon(new ImageIcon(img));
                labelSticker.setCursor(new Cursor(12));
                labelSticker.setName(f.getAbsolutePath());
                labelSticker.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JLabel label = (JLabel) e.getComponent();
                        handleIS(label, "S" + stickerName + "/");
                    }

                });
                panel_StickerContent.add(labelSticker);
            }
            resetPanel(panel_StickerContent);
            popup_sticker.add(splitPane_sticker);
        } catch (URISyntaxException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initPopUp() {
        //popup_MoreFe.setBackground(Color.red);
        popup_MoreFe.add(panel_popup);
        initEmoji();
        initSticker();
        setSticker("quoobee");
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel boder_label_myAvaProfile;
    private javax.swing.JLabel border_label_avaYourFriends;
    private javax.swing.JLabel btn_SendMessage;
    private javax.swing.JLabel btn_icon;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel label_addFriends;
    private javax.swing.JLabel label_avaYourFriends1;
    private javax.swing.JLabel label_loading;
    private javax.swing.JLabel label_moreFeature;
    private javax.swing.JLabel label_myAvaProfile1;
    private javax.swing.JLabel label_myName;
    private javax.swing.JLabel label_popup_file;
    private javax.swing.JLabel label_popup_img;
    private javax.swing.JLabel label_popup_sticker;
    private javax.swing.JLabel label_yourFriendName;
    private javax.swing.JLabel label_yourFriendStatus;
    private javax.swing.JLayeredPane layerPane_Friends;
    private javax.swing.JLayeredPane layerPane_Profile;
    private javax.swing.JPopupMenu loading;
    private javax.swing.JPanel loading_icon;
    private javax.swing.JPanel panel_ChatLog;
    private javax.swing.JPanel panel_FriendsInfo;
    private javax.swing.JPanel panel_Left;
    private javax.swing.JPanel panel_ListFriends;
    private javax.swing.JPanel panel_Main;
    private javax.swing.JPanel panel_Media;
    private javax.swing.JPanel panel_Message;
    private javax.swing.JPanel panel_Profile;
    private javax.swing.JPanel panel_Right;
    private javax.swing.JPanel panel_StickerContent;
    private javax.swing.JPanel panel_StickerTitle;
    private javax.swing.JPanel panel_WriteMessage;
    private javax.swing.JPanel panel_emoji;
    private javax.swing.JPanel panel_line;
    private javax.swing.JPanel panel_popup;
    private javax.swing.JPanel panel_searchFriends;
    private javax.swing.JPanel panel_topBar;
    private javax.swing.JPopupMenu popup_MoreFe;
    private javax.swing.JPopupMenu popup_emoji;
    private javax.swing.JPopupMenu popup_sticker;
    private javax.swing.JScrollPane scollPane_ListFriends;
    private javax.swing.JScrollPane scollPane_StickerTitle;
    private javax.swing.JScrollPane scollPanel_ChatLog;
    private javax.swing.JScrollPane scollPanel_StickerContent;
    private javax.swing.JSplitPane splitPane_sticker;
    private javax.swing.JTextField txt_WriteMessage;
    private javax.swing.JTextField txt_searchFriends;
    // End of variables declaration//GEN-END:variables
}
