/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author E6540
 */
public class viewFile extends javax.swing.JPanel {

    /**
     * Creates new form viewFile
     */
    private byte[] file;
    private String fileName;
    public viewFile(byte[] file, String fileName) {
        this.file = file;
        this.fileName = fileName;
        String fN = fileName.substring(1);
        int index = fN.lastIndexOf("\\");
        fN = fN.substring(index+1);
        if(fN.length() <= 25)
            this.label_fileName.setText(fN);
        else
            this.label_fileName.setText(fN.substring(0, 25)+"...");
    }

    public viewFile() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        label_fileName = new javax.swing.JLabel();
        label_download = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/file.png"))); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(40, 40));
        jLabel1.setPreferredSize(new java.awt.Dimension(40, 40));
        add(jLabel1);

        label_fileName.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        label_fileName.setText("File Name");
        label_fileName.setMaximumSize(new java.awt.Dimension(180, 40));
        label_fileName.setPreferredSize(new java.awt.Dimension(180, 40));
        add(label_fileName);

        label_download.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/downloadfile-normal.png"))); // NOI18N
        label_download.setMaximumSize(new java.awt.Dimension(30, 40));
        label_download.setPreferredSize(new java.awt.Dimension(30, 40));
        label_download.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                label_downloadMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                label_downloadMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                label_downloadMouseReleased(evt);
            }
        });
        add(label_download);
    }// </editor-fold>//GEN-END:initComponents

    private void label_downloadMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_downloadMousePressed
        label_download.setIcon(new ImageIcon(getClass().getResource("/Image/downloadfile-clicked.png")));
    }//GEN-LAST:event_label_downloadMousePressed

    private void label_downloadMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_downloadMouseReleased
        label_download.setIcon(new ImageIcon(getClass().getResource("/Image/downloadfile-normal.png")));
    }//GEN-LAST:event_label_downloadMouseReleased

    private void label_downloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_label_downloadMouseClicked
        if (!fileName.equals("")) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    FileOutputStream fileOuputStream = new FileOutputStream(file.getAbsolutePath());
                    fileOuputStream.write(this.file);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(viewFile.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(viewFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
            JOptionPane.showMessageDialog(null, "Save file success","Save file",JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_label_downloadMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel label_download;
    private javax.swing.JLabel label_fileName;
    // End of variables declaration//GEN-END:variables
}