package view;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import misc.Logger;
import util.Utils;

public class PlatformManager extends JFrame {

  private JPanel contentPane;
  private JTextField textLoadAgent;
  private JTextField textGeneratorAgent;
  private  ContainerController jadeContainer;
  /**
   * Create the frame.
   * @param jadeContainer 
   */
  public PlatformManager(ContainerController jadeContainr) {
    this.jadeContainer = jadeContainr;
    setTitle("Platform Manager");
    setResizable(false);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 383, 178);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JButton btnLoadAgent = new JButton("Add");
    btnLoadAgent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          String agentName = textLoadAgent.getText();
          if(Utils.isBlank(agentName)) { Logger.warn("Agent has no name!"); return; }
          textLoadAgent.setText("");
          Logger.info("Agent %s is being created...", agentName);
          AgentController ac = jadeContainer.createNewAgent(agentName, "agent.LoadAgent", null);
          ac.start();
        } catch (StaleProxyException ex) {
          Logger.error(ex, "Agent couldn't created.");
        }
      }
    });
    btnLoadAgent.setBounds(281, 31, 90, 29);
    contentPane.add(btnLoadAgent);
    
    JLabel lblLoadAgent = new JLabel("Add new Load agent:");
    lblLoadAgent.setBounds(6, 6, 231, 16);
    contentPane.add(lblLoadAgent);
    
    JLabel lblGeneratorAgent = new JLabel("Name:");
    lblGeneratorAgent.setBounds(6, 36, 58, 16);
    contentPane.add(lblGeneratorAgent);
    
    textLoadAgent = new JTextField();
    textLoadAgent.setBounds(53, 30, 223, 28);
    contentPane.add(textLoadAgent);
    textLoadAgent.setColumns(10);
    
    JButton btnGeneratorAgent = new JButton("Add");
    btnGeneratorAgent.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          String agentName = textGeneratorAgent.getText();
          if(Utils.isBlank(agentName)) { Logger.warn("Agent has no name!"); return; }
          textGeneratorAgent.setText("");
          Logger.info("Agent %s is being created...", agentName);
          AgentController ac = jadeContainer.createNewAgent(agentName, "agent.GeneratorAgent", null);
          ac.start();
        } catch (StaleProxyException ex) {
          Logger.error(ex, "Agent couldn't created.");
        }
      }
    });
    btnGeneratorAgent.setBounds(281, 115, 90, 29);
    contentPane.add(btnGeneratorAgent);
    
    JLabel label = new JLabel("Name:");
    label.setBounds(6, 120, 48, 16);
    contentPane.add(label);
    
    textGeneratorAgent = new JTextField();
    textGeneratorAgent.setColumns(10);
    textGeneratorAgent.setBounds(53, 114, 223, 28);
    contentPane.add(textGeneratorAgent);
    
    JLabel lblSystem = new JLabel("Add new Generator agent:");
    lblSystem.setBounds(6, 92, 231, 16);
    contentPane.add(lblSystem);
  }
}
