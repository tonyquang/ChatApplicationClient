/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;


import ClientsObject.ObjectClients;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;


/**
 *
 * @author E6540
 */
public class Register extends javax.swing.JFrame {

    private final String address = "127.0.0.1";
    private final int ports = 14049;
    private ImageIcon imgIcon = null;

    public Register() {
        initComponents();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel_Resginter = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_usr = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txt_pwd = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        txt_fullName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        label_chossenImgFile = new javax.swing.JLabel();
        label_imgName = new javax.swing.JLabel();
        label_register = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Resignter");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        panel_Resginter.setBackground(new java.awt.Color(255, 255, 255));
        panel_Resginter.setPreferredSize(new java.awt.Dimension(350, 400));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel1.setText("User Name");
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_usr.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_usr.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel2.setText("Password");
        jLabel2.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_pwd.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_pwd.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel3.setText("Full Name");
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 30));

        txt_fullName.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        txt_fullName.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel4.setText("Avatar");
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 30));

        label_chossenImgFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/chosseImg.png"))); // NOI18N
        label_chossenImgFile.setToolTipText("Choose Image File");
        label_chossenImgFile.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_chossenImgFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_chossenImgFileMouseClicked(evt);
            }
        });

        label_imgName.setMaximumSize(new java.awt.Dimension(180, 30));
        label_imgName.setPreferredSize(new java.awt.Dimension(180, 30));

        label_register.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/btnRegNormal.png"))); // NOI18N
        label_register.setToolTipText("Register");
        label_register.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        label_register.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_registerMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label_registerMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                label_registerMouseExited(evt);
            }
        });

        javax.swing.GroupLayout panel_ResginterLayout = new javax.swing.GroupLayout(panel_Resginter);
        panel_Resginter.setLayout(panel_ResginterLayout);
        panel_ResginterLayout.setHorizontalGroup(
            panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ResginterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_usr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_pwd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_fullName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panel_ResginterLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label_chossenImgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(label_imgName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel_ResginterLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(label_register)))
                .addContainerGap())
        );
        panel_ResginterLayout.setVerticalGroup(
            panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel_ResginterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_usr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_pwd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_fullName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addGroup(panel_ResginterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label_chossenImgFile)
                    .addComponent(label_imgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(label_register)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel_Resginter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panel_Resginter, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void label_registerMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseEntered
        this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btnRegHover.png")));
    }//GEN-LAST:event_label_registerMouseEntered

    private void label_registerMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseExited
        this.label_register.setIcon(new ImageIcon(getClass().getResource("/Image/btnRegNormal.png")));
    }//GEN-LAST:event_label_registerMouseExited

    //ĐĂNG KÝ TÀI KHOẢNG
    //DÙNG 1 PKGS ĐỂ GỬI BẰNG ObjectOutputStream
    //TRONG PKGS CHƯA Biến Action và 1 đối tượng Object
    //Gửi qua Server có nhiệm vụ kiểm tra Action để phân loại đọc Object đó là gì
    private void label_registerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_registerMouseClicked
        try {
            String userName = this.txt_usr.getText();
            String pwd = this.txt_pwd.getText();
            String fullName = this.txt_fullName.getText();
            if (this.imgIcon == null) {
                this.imgIcon = new ImageIcon(getClass().getResource("/Image/avatar.png"));
            }

            if (userName.equals("") || pwd.equals("") || fullName.equals("")) {
                JOptionPane.showMessageDialog(null, "Please fill all of field!!!");
            }

            Socket socket = new Socket(address, ports);
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream din = new ObjectInputStream(socket.getInputStream());

            //Clients clientReg = new Clients("register",userName, pwd, fullName, imgIcon);
            ObjectClients objClientReg = new ObjectClients("register", userName, pwd, fullName, imgIcon);
            oout.writeObject(objClientReg);//Gửi PKGS tới server chờ server xử lý
            
            String rs = "";
            try {
                rs = ((ObjectClients)din.readObject()).getStatus();
                if (rs.equals("already")) {
                    JOptionPane.showMessageDialog(null, "User name is already !");
                } else if (rs.equals("fail")) {
                    JOptionPane.showMessageDialog(null, "Error Please try again!");
                } else {
                    JOptionPane.showMessageDialog(null, "Create account successfuly!");
                    resetRegister();
                }
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Server is offline!");
            ex.printStackTrace();
        }
    }//GEN-LAST:event_label_registerMouseClicked

    public void resetRegister() {
        this.txt_fullName.setText("");
        this.txt_pwd.setText("");
        this.txt_usr.setText("");
        this.label_imgName.setText("");
        this.imgIcon = null;
    }

    private void label_chossenImgFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_chossenImgFileMouseClicked
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "bmp", "jpeg"));
        jfc.setAcceptAllFileFilterUsed(true);
        int returnValue = jfc.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            ImageIcon imgIcon = new ImageIcon(jfc.getSelectedFile().getAbsolutePath());
            Image img;
            if (imgIcon.getIconWidth() > imgIcon.getIconHeight()) {
                img = imgIcon.getImage().getScaledInstance(100, -1, Image.SCALE_SMOOTH);
            } else {
                img = imgIcon.getImage().getScaledInstance(-1, 100, Image.SCALE_SMOOTH);
            }
            this.imgIcon = new ImageIcon(img);
            if(jfc.getSelectedFile().getName().length()>25)
                this.label_imgName.setText(jfc.getSelectedFile().getName().substring(0, 25) + "...");
            else
                this.label_imgName.setText(jfc.getSelectedFile().getName());
        }
    }//GEN-LAST:event_label_chossenImgFileMouseClicked

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.dispose();
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel label_chossenImgFile;
    private javax.swing.JLabel label_imgName;
    private javax.swing.JLabel label_register;
    private javax.swing.JPanel panel_Resginter;
    private javax.swing.JTextField txt_fullName;
    private javax.swing.JPasswordField txt_pwd;
    private javax.swing.JTextField txt_usr;
    // End of variables declaration//GEN-END:variables
}
