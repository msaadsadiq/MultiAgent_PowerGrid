package agent;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import misc.Logger;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import pojo.PowerType;
import pojo.PowerRequestInfo;
import pojo.PowerSellInfo;
import util.F.Tuple;
import util.Utils;
import view.SearchView;

public class LoadAgent extends Agent {

  public final HashSet<DFAgentDescription> knownPowerDiscoveryServiceList = new HashSet<DFAgentDescription>();
  public final HashMap<PowerType.Kind, HashMap<PowerType, PowerSellInfo>> boughtProviders = new HashMap<PowerType.Kind, HashMap<PowerType, PowerSellInfo>>();
  private LoadAgent agent = this;
  private SearchView ui;
  
  @Override
  protected void setup() {
   
   EventQueue.invokeLater(new Runnable() {
     @Override
     public void run() {
       Logger.info(agent, "Creating Load Agent UI...");
       try {
         ui = new SearchView(agent);
         ui.setVisible(true);
       } catch (Exception e) {
         Logger.error(agent, e, "Couldn't create UI!");
       }
     }
   });
 
   addBehaviour(new UpdatePowerDiscoveryAgents());
  }
  
  public SearchView getUi() {
    return ui;
  }
  
  public final class FindAndPurchasePower extends SequentialBehaviour {
    private Set<DFAgentDescription> knownAgentsAtTimeBehaviourStarted;
    private Set<PowerSellInfo> powerOffers = new HashSet<PowerSellInfo>();
    private Set<PowerSellInfo> powerToBuy = new HashSet<PowerSellInfo>();
    
    public FindAndPurchasePower(PowerType.Kind ptype, float maxBudgetPerProviderI, int maxProviderCountI, float minRatingI, float totalBudgetI) {
      knownAgentsAtTimeBehaviourStarted = (HashSet<DFAgentDescription>) agent.knownPowerDiscoveryServiceList.clone();
      super.addSubBehaviour(new LookForPower(ptype, maxBudgetPerProviderI, minRatingI));
      super.addSubBehaviour(new ListenLookForProviderAnswers());
      super.addSubBehaviour(new SelectProvider(powerOffers, ptype, maxBudgetPerProviderI, maxProviderCountI, minRatingI, totalBudgetI));
      super.addSubBehaviour(new BuyPower(powerToBuy));
      super.addSubBehaviour(new ListenBuyPowerAnswers());
    }
    
    private class LookForPower extends OneShotBehaviour {
     private float maxBudgetPerProvider;
     private float minRating;
     private final PowerType.Kind ptype;
     public LookForPower(PowerType.Kind ptype, float maxBudgetPerProvider, float minRating) {
       this.ptype = ptype;
       this.maxBudgetPerProvider = maxBudgetPerProvider;
       this.minRating = minRating;
     }

    @Override
    public void action() {
      Logger.info(agent, "Sending Power Provider search request to Generator Agents...");
      ACLMessage msg = new ACLMessage(ACLMessage.CFP);
      for(DFAgentDescription df: knownAgentsAtTimeBehaviourStarted) {
        msg.addReceiver(df.getName());
      }
      try {
        msg.setContentObject(new PowerRequestInfo(ptype, maxBudgetPerProvider, minRating));
        this.myAgent.send(msg);
      } catch (Exception e) {
        Logger.error(agent, e, "Couldn't send Power search request to Generator Agents.");
      }
    }
   }
  
    private class ListenLookForProviderAnswers extends SimpleBehaviour {
      private int answerCount = 0;
      private final long TIMEOUT_MS = 15000;
      private final long WAIT_MS = 1000;
      private final long startTime;
      
      public ListenLookForProviderAnswers() {
        super();
        startTime = System.currentTimeMillis();
      }

      @Override
      public void action() {
        Logger.info(agent, "Recieving Power search results... (%s / %s)", answerCount, knownAgentsAtTimeBehaviourStarted.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        try {
          if(!msg.getContentObject().getClass().equals(HashSet.class)) {
            Logger.warn(agent, "Unexcepted message received from agent %s.", msg.getSender().getName());
            return;
          }
          
          answerCount++;
          
          if(msg.getPerformative() == ACLMessage.REFUSE) {
            Logger.warn(agent, "%s refuesd the message!", msg.getSender().getName());
            return;
          }
          
          HashSet<PowerSellInfo> PowerListReturned = (HashSet<PowerSellInfo>) msg.getContentObject();
          if(PowerListReturned == null || PowerListReturned.size() == 0) { 
            Logger.warn(agent, "%s sent an empty list of power sources!", msg.getSender().getName());
            return;
          }
          
          for(PowerSellInfo s: PowerListReturned) {

        	  if(!s.getSellerAgent().equals(msg.getSender())) {
              Logger.error(agent, "Generator agent isn't the agent he claims to be! Security! (for agent %s)", msg.getSender().getName());
              return;
            }
          }
          
          powerOffers.addAll(PowerListReturned);
          
        } catch (Exception e) {
          Logger.error(agent, e, "Couldn't collect the power provider search results.");
        }
       
      }

      @Override
      public boolean done() {
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured while waiting for response.");
          return true;
        }
        
        if(answerCount >= knownAgentsAtTimeBehaviourStarted.size()) {
          return true;
        }
        
        return false;
      }
    }
  
    private class SelectProvider extends OneShotBehaviour {

      private Set<PowerSellInfo> providersProposed;
      private PowerType.Kind ptype;
      private float maxBudgetPerProviderI;
      private int maxProviderCountI;
      private float minRatingF;
      private float totalBudgetI;
      
      public SelectProvider(Set<PowerSellInfo> providerOffers, PowerType.Kind ptype, float maxBudgetPerProviderI, int maxProviderCountI, float minRatingF, float totalBudgetI) {
        this.providersProposed = providerOffers;
        this.ptype = ptype;
        this.maxBudgetPerProviderI = maxBudgetPerProviderI;
        this.maxProviderCountI = maxProviderCountI;
        this.minRatingF = minRatingF;
        this.totalBudgetI = totalBudgetI;
      }

      @Override
      public void action() {
        Map<PowerType, Float> ratingMap = new HashMap<PowerType, Float>();
        Map<PowerType, Float> priceMap = new HashMap<PowerType, Float>();
        Map<PowerType, PowerSellInfo> ssiMap = new HashMap<PowerType, PowerSellInfo>();
        for(PowerSellInfo ssi: providersProposed) {
          PowerType powertype = ssi.getPower();
          
          Float maxRating = ratingMap.get(powertype);
          Float minPrice = priceMap.get(powertype);
          
          if(maxRating == null || maxRating < ssi.getAvgRating()) {
            ratingMap.put(powertype, ssi.getAvgRating());
          }
          
          if(minPrice == null || minPrice > ssi.getPrice()) {
            priceMap.put(powertype, ssi.getPrice());
            ssiMap.put(powertype, ssi);
          }
        }
        
        Set<PowerSellInfo> filteredProvidersList = new HashSet<PowerSellInfo>();
        float totalPrice = 0;
        
       
        for (PowerSellInfo ssi: providersProposed) {
          PowerType powertype = ssi.getPower();
          
          if(filteredProvidersList.contains(ssi) || !this.ptype.equals(ssi.getPower().getKind()) || ratingMap.get(powertype) < this.minRatingF || priceMap.get(powertype) > this.maxBudgetPerProviderI) {
            continue;
          }
          
          filteredProvidersList.add(ssiMap.get(powertype));
          totalPrice += ssiMap.get(powertype).getPrice();
        }
        
        if(filteredProvidersList.size() > this.maxProviderCountI) {
          Iterator<PowerSellInfo> i = filteredProvidersList.iterator();
          while (filteredProvidersList.size() > this.maxProviderCountI) {
            PowerSellInfo itemToDelete = i.next();
            totalPrice -= itemToDelete.getPrice();
            i.remove();
          }
        }
        
        if(totalPrice > this.totalBudgetI) {
          Iterator<PowerSellInfo> i = filteredProvidersList.iterator();
          while (totalPrice > this.totalBudgetI) {
            PowerSellInfo itemToDelete = i.next();
            totalPrice -= itemToDelete.getPrice();
            i.remove();
          }
        }
        
        powerToBuy.addAll(filteredProvidersList);
      }
    }
  
    private class BuyPower extends OneShotBehaviour {
      private Set<PowerSellInfo> providersToBuyList;
      public BuyPower(Set<PowerSellInfo> providersToBuy) {
        this.providersToBuyList = providersToBuy;
      }

      @Override
      public void action() {
        if(providersToBuyList == null || providersToBuyList.size() == 0) { Logger.warn(agent, "-------"); return; }
         Logger.info(agent, "Ordering Power purchase...");
         for(PowerSellInfo ssi: providersToBuyList) {
           ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
           msg.addReceiver(ssi.getSellerAgent());
           try {
             msg.setContentObject(ssi);
             this.myAgent.send(msg);
           } catch (Exception e) {
             Logger.error(agent, e, "Couldn't send Power purchase request.");
           }
         }
      }
    }
    
    private class ListenBuyPowerAnswers extends SimpleBehaviour {
      private int providersCount = 0;
      private final long TIMEOUT_MS = 15000;
      private final long WAIT_MS = 1000;
      private long startTime;
      private boolean isFirstRun = true;
      
      public ListenBuyPowerAnswers() {
        super();
      }
      
      @Override
      public void action() {
        if(isFirstRun) { startTime = System.currentTimeMillis(); } 
        if(powerToBuy.size() == 0) { return; }
        Logger.info(agent, "Waiting for Power purchase results... (%s / %s)", providersCount, powerToBuy.size());
        ACLMessage msg = this.myAgent.receive();
        if (msg == null) { block(WAIT_MS); return; }
        
        try {
          if(!msg.getContentObject().getClass().equals(Tuple.class)) {
            Logger.warn(agent, "Agent %s sent an unexpected message.", msg.getSender().getName());
            return;
          }
          
          providersCount++;
          
          if(msg.getPerformative() == ACLMessage.REFUSE) {
            Logger.warn(agent, "%s refused me!", msg.getSender().getName());
            return;
          }
          
          Tuple<String, PowerSellInfo> urlRequest = (Tuple<String, PowerSellInfo>) msg.getContentObject();
          final String url = urlRequest._1;
          final PowerSellInfo powerInfo = urlRequest._2;
          if(Utils.isBlank(url) || powerInfo == null) { 
            Logger.warn(agent, "Agent %s didn't return order result info, buying power failed.", msg.getSender().getName());
            return;
          }
          
          Logger.info(agent, "Saad Sadiq: %s [URL: %s]", powerInfo.toString(), url);
          
          Runnable addIt = new Runnable() { 
            @Override
            public void run() {
              ui.addMessageToConsole(powerInfo.toString() + " [URL: " + url + "]");
            }
          };
         
          SwingUtilities.invokeLater(addIt);
        } catch (Exception e) {
          Logger.error(agent, e, "Couldn't purchase Power source.");
        }
      }

      @Override
      public boolean done() {
        Runnable enableUI = new Runnable() { 
          @Override
          public void run() {
            ui.enableUI();
          }
        };
        
        if(System.currentTimeMillis() - startTime > TIMEOUT_MS) {
          Logger.warn(agent, "Timeout occured while waiting for response.");
          SwingUtilities.invokeLater(enableUI);
          return true;
        }
        
        if(providersCount >= powerToBuy.size()) {
          SwingUtilities.invokeLater(enableUI);
          return true;
        }
        
        return false;
      }
    }
  }
  
  private final class UpdatePowerDiscoveryAgents extends TickerBehaviour {

    public UpdatePowerDiscoveryAgents() {
      super(agent, 10000);
    }

    @Override
    protected void onTick() {
     
      ServiceDescription sd = new ServiceDescription();
      sd.setType("POWER-DISCOVERY");
      DFAgentDescription df = new DFAgentDescription();
      df.addServices(sd);
      try {
        DFAgentDescription[] result = DFService.search(this.myAgent, df);
        agent.knownPowerDiscoveryServiceList.clear();
        for(DFAgentDescription dfad: result) {
          agent.knownPowerDiscoveryServiceList.add(dfad);
        }
       
      } catch (FIPAException e) {
        Logger.error(agent,  e, "An error occured while updating power provider agent list from DF.");
      }
    }

  }
  
  public class ShutdownAgent extends OneShotBehaviour {

    @Override
    public void action() {
      agent.doDelete();
    }

  }
}
