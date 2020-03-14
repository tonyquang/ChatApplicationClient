/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import ClientsObject.ObjectClients;
import frames.ImgInChat;
import frames.ListFriends;
import frames.viewFile;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.OverlayLayout;
import javax.swing.UIManager;
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
    private boolean soundStatus = false;
    
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
        initComponents();
        
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
                    if (status.equals("resLogin")) {
                        String rs = objServerResp.getMessage();
                        if (rs.equals("fail")) {
                            JOptionPane.showMessageDialog(null, "Username or Password wrong, Please try again!!!", "Login Fail", JOptionPane.ERROR_MESSAGE);
                            Login login = new Login();
                            login.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(null, "Login Success! Welcome to Meow~~~ App", "Login success", JOptionPane.INFORMATION_MESSAGE);                          
                            this.clientGUI.setVisible(true);

                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    ListFriends firstFriend = (ListFriends) panel_ListFriends.getComponent(0);
                                    initChat(firstFriend);
                                }
                            });
                            t.run();
                        }
                    //Nhận phản hồi lấy danh sách bạn bè
                    } else if (status.equals("friend")) {
                        loadFriends(objServerResp);                      
                    //Nhận phản hồi lấy thông tin cá nhân của mình    
                    } else if (status.equals("profile")) {
                        loadProfile(objServerResp);
                    //Nhận  phản hồi cập nhật trạng thái của bạn bè
                    } else if (status.equals("updateFriendStatus")) {
                        updateStatusFriend(userName, userStatus);
                        if (currentFriendUserName.equals(userName)) {
                            setUserStatus(userStatus);
                        }
                    //Nhận phản hồi thêm bạn bè hoặc được bạn bè thêm    
                    } else if (status.equals("resAddfriends") || status.equals("pushfriendtolist")) {
                        if (objServerResp.getMessage().equals("success")) {

                            ListFriends addFriend = new ListFriends();
                            addFriend.set(imgIcon, userName, fullName, userStatus);
                            addFriend.setVisible(true);

                            addMouseEventListFriends(addFriend, userName.trim(), fullName, userStatus, imgIcon);

                            panel_ListFriends.add(addFriend);
                            resetPanelListFriends(panel_ListFriends);

                            listFriends.put(userName, addFriend);

                            if (status.equals("resAddfriends")) {
                                JOptionPane.showMessageDialog(null, "Add new friend success!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, fullName + " add you to their list friends!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Can't find username or username was friend!", "Notification", JOptionPane.INFORMATION_MESSAGE);
                        }
                    //Nhận phản hồi lấy danh sách tin nhắn của bạn bè    
                    } else if (status.equals("resMess")) {                       
                            hanldeMess(objServerResp);                      
                    } else if(status.equals("newMess")){
                        if(!userName.trim().equals(currentFriendUserName))
                        {
                            listFriends.get(userName.trim()).setLabel_notification();
                            playSound();
                        }else
                        {
                            hanldeMess(objServerResp);
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
    public void resetPanelListFriends(JPanel jpanel) {
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
                    label_yourFriendName.setText(friendName);
                    setUserStatus(friendStatus);
                    setAva(label_avaYourFriends1, imgIcon, 40);
                    listFriends.get(friendUserName).setBgClicked();
                    listFriends.get(currentFriendUserName.trim()).RemoveBg();
                    currentFriendUserName = friendUserName;
                    listFriends.get(currentFriendUserName.trim()).removeNotification();
                    panel_ChatLog.removeAll();
                    resetPanelListFriends(panel_ChatLog);
                    getMess(currentFriendUserName);//yêu cầu lấy tin nhắn của bạn bè khi click vào
                }

            }

        });
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
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Cập nhật trạng thái của bạn bè online/offline
    public void updateStatusFriend(String friendUserName, String status) {
        ListFriends f = listFriends.get(friendUserName);
        f.updateStatus(status);
        resetPanelListFriends(panel_ListFriends);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        btn_SendMessage = new javax.swing.JLabel();

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
                .addComponent(txt_WriteMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 706, Short.MAX_VALUE)
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
                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing
    //Gửi yêu cầu kết bạn
    private void label_addFriendsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_addFriendsMouseClicked
        String userNameNewFriend = JOptionPane.showInputDialog(null, "Please enter new friends's user name", "Add new friend", JOptionPane.INFORMATION_MESSAGE);
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
        int height = 40;
        int width = 200;
        //Xử lý gói message
        if (sign.equals("M")) {
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
                StyleConstants.setForeground(style, new Color(34, 38, 57));
            }
            try {
                doc.insertString(doc.getLength(), Mess.trim(), style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            height = (int) Math.round(textPane.getPreferredSize().getHeight());
            width = (int) Math.round(textPane.getPreferredSize().getWidth());
            textPane.setMaximumSize(textPane.getPreferredSize());

        } else if (sign.equals("P")) {
            imgInChat = new ImgInChat();
            ImageIcon imgIcon = new ImageIcon(objMess.getFile());
            if (imgIcon != null) {
                imgInChat.setImage(imgIcon);
            }
        } else if (sign.equals("F")) {
            vF = new viewFile(objMess.getFile(), objMess.getMessage());
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
            }
            panelChat.add(lpAva);
        } else if (username.equals(currentFriendUserName)) {
            panelChat.add(lpAva);
            //xử lý add loại mess nào vào
            if (sign.equals("M")) {
                panelChat.add(textPane);
            } else if (sign.equals("P")) {
                panelChat.add(imgInChat);
            } else if (sign.equals("F")) {
                panelChat.add(vF);
            }
            panelChat.add(Box.createHorizontalGlue());

        }
        
        resetPanelListFriends(panelChat);
        return panelChat;

    }

    public void hanldeMess(ObjectClients objMess) {

        JPanel panelChat = new JPanel();
        panelChat = myMess(objMess);

        if (panelChat != null) {

            panel_ChatLog.add(panelChat);
            panel_ChatLog.add(Box.createVerticalStrut(20));
            resetPanelListFriends(panel_ChatLog);           // scrollToBottom(scollPanel_ChatLog);
            pack();// tự tính toán lại kích thước chuẩn
            
            scrollToBottom(scollPanel_ChatLog);
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

    public void sendMess() {
        String txtMess = txt_WriteMessage.getText();
        if(!txtMess.equals(""))
        {
            ObjectClients objTxtMess = new ObjectClients();
            objTxtMess.setStatus("chat");
            objTxtMess.setMessage("M"+txtMess);
            objTxtMess.setUserNameSend(this.userName);
            objTxtMess.setUserNameRecv(currentFriendUserName);
            try {
                oos.writeObject(objTxtMess);
                oos.flush();
                //Sau khi gửi thì cập nhật lại setUserNameRecv cho handleMess sử lý thêm vào
                objTxtMess.setUserNameRecv(this.userName);
                txt_WriteMessage.setText("");
                hanldeMess(objTxtMess);
                soundStatus = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel boder_label_myAvaProfile;
    private javax.swing.JLabel border_label_avaYourFriends;
    private javax.swing.JLabel btn_SendMessage;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel label_addFriends;
    private javax.swing.JLabel label_avaYourFriends1;
    private javax.swing.JLabel label_moreFeature;
    private javax.swing.JLabel label_myAvaProfile1;
    private javax.swing.JLabel label_myName;
    private javax.swing.JLabel label_yourFriendName;
    private javax.swing.JLabel label_yourFriendStatus;
    private javax.swing.JLayeredPane layerPane_Friends;
    private javax.swing.JLayeredPane layerPane_Profile;
    private javax.swing.JPanel panel_ChatLog;
    private javax.swing.JPanel panel_FriendsInfo;
    private javax.swing.JPanel panel_Left;
    private javax.swing.JPanel panel_ListFriends;
    private javax.swing.JPanel panel_Main;
    private javax.swing.JPanel panel_Media;
    private javax.swing.JPanel panel_Message;
    private javax.swing.JPanel panel_Profile;
    private javax.swing.JPanel panel_Right;
    private javax.swing.JPanel panel_WriteMessage;
    private javax.swing.JPanel panel_line;
    private javax.swing.JPanel panel_searchFriends;
    private javax.swing.JPanel panel_topBar;
    private javax.swing.JScrollPane scollPane_ListFriends;
    private javax.swing.JScrollPane scollPanel_ChatLog;
    private javax.swing.JTextField txt_WriteMessage;
    private javax.swing.JTextField txt_searchFriends;
    // End of variables declaration//GEN-END:variables
}
