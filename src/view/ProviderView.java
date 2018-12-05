package view;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import misc.Logger;
import pojo.PowerType;
import pojo.PowerType.Kind;
import pojo.PowerSellInfo;
import util.Utils;
import agent.GeneratorAgent;

public class ProviderView extends JFrame {

  JPanel contentPane;
  JTextField txtOwner;
  JTextField txtPlantName;
  JTextField txtPrice;
  JTextField txtRating;
  JComboBox txtKind;
  List lstSold;
  JList lstProviders;
  DefaultListModel lstProviderList;

  /**
   * Create the frame.
   * @param runnable 
   */
  public ProviderView(final GeneratorAgent agent) {
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        agent.addBehaviour(agent.new ShutdownAgent());
      }
    });
    setTitle("Provider: " + agent.getLocalName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 600, 361);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(2, 2, 2, 2));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblInfo = new JLabel("Local Power Grid:");
    lblInfo.setBounds(6, 6, 152, 21);
    lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
    lblInfo.setVerticalAlignment(SwingConstants.TOP);
    contentPane.add(lblInfo);
    
    JLabel lblInfo2 = new JLabel("Add new:");
    lblInfo2.setBounds(320, 6, 163, 21);
    lblInfo2.setVerticalAlignment(SwingConstants.TOP);
    lblInfo2.setHorizontalAlignment(SwingConstants.LEFT);
    contentPane.add(lblInfo2);
    
    JLabel lblKind = new JLabel("Type:");
    lblKind.setBounds(320, 39, 81, 16);
    lblKind.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblKind);
    
    JLabel lblRating = new JLabel("S. Rating:");
    lblRating.setBounds(320, 159, 81, 16);
    lblRating.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblRating);
    
    JLabel lblPrice = new JLabel("Price KWH:");
    lblPrice.setBounds(320, 129, 81, 16);
    lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblPrice);
    
    JLabel lblPlantName = new JLabel("Plant#:");
    lblPlantName.setBounds(320, 99, 81, 16);
    lblPlantName.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblPlantName);
    
    JLabel lblOwner = new JLabel("Owner:");
    lblOwner.setBounds(320, 71, 81, 16);
    lblOwner.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(lblOwner);
    
    final JButton btnAdd = new JButton("Add");
    btnAdd.setBounds(410, 184, 179, 29);
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        
       Kind ptype = (PowerType.Kind)txtKind.getSelectedItem();
       String owner = txtOwner.getText();
       String plantno = txtPlantName.getText();
       float rating, price;
       try {
         rating = Integer.parseInt(txtRating.getText());
         price = Float.parseFloat(txtPrice.getText());
       } catch (NumberFormatException ex) {
         Logger.error(agent, ex, "Numbers are not numbers.");
         return;
       }
       
       if(Utils.isBlank(owner) || Utils.isBlank(plantno) || rating < 0 || rating > 5 || price < 0) {
         Logger.error(agent, "There is some missing information.");
         return;
       }
       
       txtOwner.setText("");
       txtPlantName.setText("");
       txtRating.setText("");
       txtPrice.setText("");
       
       PowerType s = new PowerType(owner, plantno, ptype);
       PowerSellInfo ssi = new PowerSellInfo(rating, price, agent.getAID(), s);
       
       if(lstProviderList.contains(ssi)) { Logger.warn(agent, "Duplicate Power ID?"); return; }
       
       lstProviderList.addElement(ssi);
       
       agent.addBehaviour(agent.new AddProvider(ssi));
      }
    });
    contentPane.add(btnAdd);
    
    txtKind = new JComboBox();
    txtKind.setBounds(410, 33, 179, 28);
    contentPane.add(txtKind);
    
    txtOwner = new JTextField();
    txtOwner.setBounds(410, 63, 179, 28);
    contentPane.add(txtOwner);
    txtOwner.setColumns(10);
    
    txtPlantName = new JTextField();
    txtPlantName.setBounds(410, 93, 179, 28);
    txtPlantName.setColumns(10);
    contentPane.add(txtPlantName);
    
    txtPrice = new JTextField();
    txtPrice.setBounds(410, 123, 179, 28);
    txtPrice.setColumns(10);
    contentPane.add(txtPrice);
    
    txtRating = new JTextField();
    txtRating.setBounds(410, 153, 179, 28);
    txtRating.setColumns(10);
    contentPane.add(txtRating);
    
    JButton btnDelete = new JButton("Delete Selected");
    btnDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PowerSellInfo ssi = (PowerSellInfo) lstProviders.getSelectedValue();
        
        if(ssi == null) {
          Logger.warn(agent, "Nothing is selected.");
          return;
        }
        
        lstProviderList.removeElement(ssi);
        agent.addBehaviour(agent.new RemoveProvider(ssi));
      }
    });
    btnDelete.setBounds(20, 300, 200, 29);
    contentPane.add(btnDelete);
    
    JLabel lblInfo3 = new JLabel("Sold:");
    lblInfo3.setVerticalAlignment(SwingConstants.TOP);
    lblInfo3.setHorizontalAlignment(SwingConstants.LEFT);
    lblInfo3.setBounds(330, 210, 81, 21);
    contentPane.add(lblInfo3);
    
    lstSold = new List();
    lstSold.setBounds(315, 235, 264, 90);
    contentPane.add(lstSold);
    
    lstProviders = new JList();
    lstProviders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lstProviderList = new DefaultListModel();
    
    lstProviders.setBounds(6, 27, 294, 271);
    lstProviders.setModel(lstProviderList);
    
    contentPane.add(lstProviders);
    
    for(PowerType.Kind g: PowerType.Kind.values()) {
      txtKind.addItem(g);
    }
  }

  public void addBuyedItem(String msg) {
    lstSold.add(msg);
  }
}
