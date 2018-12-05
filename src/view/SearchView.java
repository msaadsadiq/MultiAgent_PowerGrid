package view;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import agent.LoadAgent;
import misc.Logger;
import pojo.PowerType;

public class SearchView extends JFrame {

  JPanel contentPane;
  JTextField minRating;
  JTextField maxBudgetPerPower;
  JTextField totalBudget;
  JTextField maxProviderCount;
  JComboBox ProviderType;
  List console;
  JButton search;
  
  public void addMessageToConsole(String message) {
    console.add(message);
  }
  
  /**
   * Create the frame.
   * @param runnable 
   */
  public SearchView(final LoadAgent agent) {
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        agent.addBehaviour(agent.new ShutdownAgent());
      }
    });
    setTitle("Seeker: " + agent.getLocalName());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBounds(100, 100, 390, 380);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JLabel lblInfo = new JLabel("Load agent to search the Power Grid");
    lblInfo.setHorizontalAlignment(SwingConstants.LEFT);
    lblInfo.setVerticalAlignment(SwingConstants.TOP);
    lblInfo.setBounds(6, 6, 628, 21);
    contentPane.add(lblInfo);
    
//    JLabel lblInfo2 = new JLabel("Fill the following:");
//    lblInfo2.setVerticalAlignment(SwingConstants.TOP);
//    lblInfo2.setHorizontalAlignment(SwingConstants.CENTER);
//    lblInfo2.setBounds(6, 23, 628, 21);
//    contentPane.add(lblInfo2);
    
    JLabel lblKind = new JLabel("Type:");
    lblKind.setHorizontalAlignment(SwingConstants.LEFT);
    lblKind.setBounds(16, 56, 170, 16);
    contentPane.add(lblKind);
    
    JLabel lblMinRating = new JLabel("Min. S. rating:");
    lblMinRating.setHorizontalAlignment(SwingConstants.LEFT);
    lblMinRating.setBounds(16, 81, 170, 16);
    contentPane.add(lblMinRating);
    
    JLabel lblPricePerSource = new JLabel("Max price per KWH:");
    lblPricePerSource.setHorizontalAlignment(SwingConstants.LEFT);
    lblPricePerSource.setBounds(16, 109, 170, 16);
    contentPane.add(lblPricePerSource);
    
    JLabel lblTotalBudget = new JLabel("Max budget /KWH:");
    lblTotalBudget.setHorizontalAlignment(SwingConstants.LEFT);
    lblTotalBudget.setBounds(16, 137, 170, 16);
    contentPane.add(lblTotalBudget);
    
    JLabel lblMaxProviderCount = new JLabel("Max display limit:");
    lblMaxProviderCount.setHorizontalAlignment(SwingConstants.LEFT);
    lblMaxProviderCount.setBounds(16, 165, 170, 16);
    contentPane.add(lblMaxProviderCount);
    
    search = new JButton("Find & Buy!");
    search.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
       search.setEnabled(false);
 
       PowerType.Kind ptype = (PowerType.Kind)ProviderType.getSelectedItem();
       int minRatingI, maxProviderCountI;
       float maxBudgetPerProviderI, totalBudgetI;
       try {
         minRatingI = Integer.parseInt(minRating.getText());
         maxProviderCountI = Integer.parseInt(maxProviderCount.getText());
         maxBudgetPerProviderI = Float.parseFloat(maxBudgetPerPower.getText());
         totalBudgetI = Float.parseFloat(totalBudget.getText());
         
         if(maxProviderCountI <= 0 || minRatingI < 0 || minRatingI > 5 || totalBudgetI < 0 || maxBudgetPerProviderI < 0 || totalBudgetI < maxBudgetPerProviderI) {
           Logger.warn(agent, "Inputs are logically invalid.");
           enableUI();
           return;
         }
       } catch (NumberFormatException ex) {
        console.add("Numbers are not numbers.");
        Logger.error(agent, ex, "Numbers are not numbers.");
        enableUI();
        return;
       }
       
       disableUI();
       console.removeAll();
       agent.addBehaviour(agent.new FindAndPurchasePower(ptype, maxBudgetPerProviderI, maxProviderCountI, minRatingI, totalBudgetI));
      }
    });
    search.setBounds(56, 25, 170, 22);
    contentPane.add(search);
    
    ProviderType = new JComboBox();
    ProviderType.setBounds(158, 52, 221, 27);
    contentPane.add(ProviderType);
    
    minRating = new JTextField();
    minRating.setBounds(158, 75, 221, 28);
    contentPane.add(minRating);
    minRating.setColumns(10);
    
    maxBudgetPerPower = new JTextField();
    maxBudgetPerPower.setColumns(10);
    maxBudgetPerPower.setBounds(158, 103, 221, 28);
    contentPane.add(maxBudgetPerPower);
    
    totalBudget = new JTextField();
    totalBudget.setColumns(10);
    totalBudget.setBounds(158, 131, 221, 28);
    contentPane.add(totalBudget);
    
    maxProviderCount = new JTextField();
    maxProviderCount.setColumns(10);
    maxProviderCount.setBounds(158, 159, 221, 28);
    contentPane.add(maxProviderCount);
    
    console = new List();
    console.setBounds(16, 195, 360, 150);
    contentPane.add(console);
    
    for(PowerType.Kind g: PowerType.Kind.values()) {
    	ProviderType.addItem(g);
    }
  }

  public void enableUI() {
    minRating.setEnabled(true);
    maxBudgetPerPower.setEnabled(true);
    totalBudget.setEnabled(true);
    maxProviderCount.setEnabled(true);
    ProviderType.setEnabled(true);
    search.setEnabled(true);
  }
  
  public void disableUI() {
    minRating.setEnabled(false);
    maxBudgetPerPower.setEnabled(false);
    totalBudget.setEnabled(false);
    maxProviderCount.setEnabled(false);
    ProviderType.setEnabled(false);
    search.setEnabled(false);
  }
  
  
}
