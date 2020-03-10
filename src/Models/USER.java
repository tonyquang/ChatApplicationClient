/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import javax.swing.ImageIcon;

/**
 *
 * @author E6540
 */
public interface USER {
    public void set(ImageIcon img, String ID, String name, String status);
    public ImageIcon getImgIcon();
}
