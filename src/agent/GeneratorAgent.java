package agent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.awt.EventQueue;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingUtilities;

import misc.Logger;
import pojo.PowerType;
import pojo.PowerRequestInfo;
import pojo.PowerSellInfo;
import util.F.Tuple;
import view.ProviderView;

public class GeneratorAgent extends Agent {

  final private Set<PowerSellInfo> providerList = new HashSet<PowerSellInfo>();
  private GeneratorAgent agent = this;
  private ProviderView ui;
  
  @Override
  public void setup() {
    
    /* Broadcast this agent */
    ServiceDescription sd = new ServiceDescription();
    sd.setType("POWER-DISCOVERY");
    sd.setName(agent.getName());
    
    DFAgentDescription df = new DFAgentDescription();
    df.addServices(sd);
    
    try {
      DFService.register(this, df);
      Logger.info(agent, "Registering to DF agent...");
    } catch (FIPAException e) {
      Logger.error(agent, e, "Couldn't register to DF agent!");
    }
    
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        Logger.info(agent, "Creating Generator Agent UI...");
        try {
          ui = new ProviderView(agent);
          ui.setVisible(true);
        } catch (Exception e) {
          Logger.error(agent, e, "Couldn't create UI!");
        }
      }
    });
    
    addBehaviour(new CheckBuyerMessages());
  }

  public ProviderView getUi() {
    return ui;
  }
  
  private class CheckBuyerMessages extends CyclicBehaviour {

    @Override
    public void action() {
      Logger.info(agent, "Waiting for 'Buy Power' and 'Query Power' requests...");
      ACLMessage msg = this.myAgent.receive();
      if (msg == null) { this.block(); return; }
      
      try {
        Object something = msg.getContentObject();
        if(something.getClass().equals(PowerRequestInfo.class)) {
          agent.addBehaviour(agent.new ProviderSearch((PowerRequestInfo)something, msg));
          return;
        }
        
        if(something.getClass().equals(PowerSellInfo.class)) {
          agent.addBehaviour(agent.new BuyPower((PowerSellInfo)something, msg));
          return;
        }
        
        Logger.warn(agent, "Got an unexpected message from %s!", msg.getSender().getName());
      } catch (UnreadableException e) {
        Logger.error(agent, e, "Couldn't parse and read the message!");
      }
    }
  }
  
  private class ProviderSearch extends OneShotBehaviour {

    PowerRequestInfo sri;
    ACLMessage msg;
    public ProviderSearch(PowerRequestInfo sri, ACLMessage msg) {
      this.sri = sri;
      this.msg = msg;
    }

    @Override
    public void action() {
      Logger.info(agent, "Searching Power Providers with given criteria...");
      HashSet<PowerSellInfo> tbReturned = new HashSet<PowerSellInfo>();
      
      for(PowerSellInfo ssi: agent.providerList) {
        PowerType ptype = ssi.getPower();
        
        if(sri.ptype != null && !ptype.getKind().equals(sri.ptype)) {
          continue;
        }

        
        tbReturned.add(ssi.clone());
      }
      
      ACLMessage reply = msg.createReply();

      if(tbReturned.size() == 0) {
        reply.setPerformative(ACLMessage.REFUSE);
      } else {
        reply.setPerformative(ACLMessage.PROPOSE);
      }
      try {
        reply.setContentObject(tbReturned);
        this.myAgent.send(reply);
      } catch (Exception e) {
        Logger.error(agent, e, "Couldn't sent search results.");
      }
    }
  }
  
  private class BuyPower extends OneShotBehaviour {

    PowerSellInfo ssi;
    ACLMessage msg;
    public BuyPower(PowerSellInfo ssi, ACLMessage msg) {
      this.ssi = ssi;
      this.msg = msg;
    }

    @Override
    public void action() {
      
      ACLMessage reply = msg.createReply();
      if(!agent.providerList.contains(ssi)) {
        reply.setPerformative(ACLMessage.REFUSE);
      } else {
        reply.setPerformative(ACLMessage.PROPOSE);
      }

      Tuple<String, PowerSellInfo> urlRequest = new Tuple<String, PowerSellInfo>(generateRandomUrl(), ssi);
      try {
        reply.setContentObject(urlRequest);
        this.myAgent.send(reply);
        
        Logger.info(agent, "Power sold. Seller agent: %s Sale Info: %s - %s", msg.getSender().getName(), ssi.getPower().getOwner(), ssi.getPower().getPlant());
        
        Runnable addIt = new Runnable() { 
          @Override
          public void run() {
            ui.addBuyedItem("@" + msg.getSender().getLocalName() + " [" + ssi.getPower().getOwner() + " - " + ssi.getPower().getPlant() + "]");
          }
        };
       
        SwingUtilities.invokeLater(addIt);
      } catch (Exception e) {
        Logger.error(agent, e, "Couldn't sent Power purchase response.");
      }
    }

    private String generateRandomUrl() {
      return "------------------------" + new Random().nextInt(9999);
    }
  }
  
  public class RemoveProvider extends OneShotBehaviour {
    
    private PowerSellInfo ssi;
    public RemoveProvider(PowerSellInfo ssi) {
      this.ssi = ssi;
    }
    
    @Override
    public void action() {
      agent.providerList.remove(ssi);
    }
  }
  
  public class AddProvider extends OneShotBehaviour {

    private PowerSellInfo ssi;
    public AddProvider(PowerSellInfo ssi) {
      this.ssi = ssi;
    }
    
    @Override
    public void action() {
      agent.providerList.add(ssi);
    }
  }
  
  public class ShutdownAgent extends OneShotBehaviour {

    @Override
    public void action() {
      try {
        DFService.deregister(agent);
      } catch (FIPAException e) {
        Logger.error(agent, e, "Agent couldn't be unregistered from DF.");
      }
      agent.doDelete();
    }
  }
}
