package pojo;

import java.io.Serializable;


public class PowerRequestInfo implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -4333298041938820117L;
  public PowerType.Kind ptype;
  public float maxPricePerProvider;
  public float minRating;
  
  public PowerRequestInfo(PowerType.Kind ptype, float maxPricePerProvider, float minRating) {
    super();
    this.ptype = ptype;
    this.maxPricePerProvider = maxPricePerProvider;
    this.minRating = minRating;
  }

}
