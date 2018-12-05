package pojo;

import jade.core.AID;

import java.io.Serializable;

public class PowerSellInfo implements Serializable, Cloneable {

  private final float avgRating;
  private final float price;
  private final AID sellerAgent;
  private final PowerType powertype;
  
  public PowerSellInfo(float avgRating, float price, AID sellerAgent, PowerType powertype) {
    super();
    this.avgRating = avgRating;
    this.price = price;
    this.sellerAgent = sellerAgent;
    this.powertype = powertype;
  }
  public PowerSellInfo(float avgRating, float price) {
    this(avgRating, price, null, null);
  }
  
  public float getAvgRating() {
    return this.avgRating;
  }
  public float getPrice() {
    return this.price;
  }
  public AID getSellerAgent() {
    return sellerAgent;
  }
  public PowerType getPower() {
    return powertype;
  }
 
  @Override
  public String toString() {
    return this.powertype + " [R:" + this.avgRating + "] [P:" + this.price + "] @" + this.sellerAgent.getLocalName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(this.avgRating);
    result = prime * result + Float.floatToIntBits(this.price);
    result = prime * result + ((this.sellerAgent == null) ? 0 : this.sellerAgent.hashCode());
    result = prime * result + ((this.powertype == null) ? 0 : this.powertype.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PowerSellInfo other = (PowerSellInfo) obj;
    if (Float.floatToIntBits(this.avgRating) != Float.floatToIntBits(other.avgRating)) {
      return false;
    }
    if (Float.floatToIntBits(this.price) != Float.floatToIntBits(other.price)) {
      return false;
    }
    if (this.sellerAgent == null) {
      if (other.sellerAgent != null) {
        return false;
      }
    } else if (!this.sellerAgent.equals(other.sellerAgent)) {
      return false;
    }
    if (this.powertype == null) {
      if (other.powertype != null) {
        return false;
      }
    } else if (!this.powertype.equals(other.powertype)) {
      return false;
    }
    return true;
  }
  @Override
  public PowerSellInfo clone() {
    return new PowerSellInfo(this.getAvgRating(), this.getPrice(), this.getSellerAgent(), this.getPower());
  }
}
