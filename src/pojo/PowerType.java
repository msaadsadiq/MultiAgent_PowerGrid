package pojo;

import jade.util.leap.Serializable;

public class PowerType implements Cloneable, Serializable {

  private final String owner;
  private final String plantno;
  private final PowerType.Kind ptype;
  
  public PowerType(String owner, String plantno, PowerType.Kind ptype) {
    super();
    this.owner = owner;
    this.plantno = plantno;
    this.ptype = ptype;
  }
  
  public String getOwner() {
    return this.owner;
  }
  public String getPlant() {
    return this.plantno;
  }
  public PowerType.Kind getKind() {
    return this.ptype;
  }

  @Override
  public String toString() {
    return this.owner + " - " + this.plantno + " [" + this.ptype + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.owner == null) ? 0 : this.owner.toLowerCase().hashCode());
    result = prime * result + ((this.ptype == null) ? 0 : this.ptype.hashCode());
    result = prime * result + ((this.plantno == null) ? 0 : this.plantno.toLowerCase().hashCode());
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
    PowerType other = (PowerType) obj;
    if (this.owner == null) {
      if (other.owner != null) {
        return false;
      }
    } else if (!this.owner.equalsIgnoreCase(other.owner)) {
      return false;
    }
    if (this.ptype != other.ptype) {
      return false;
    }
    if (this.plantno == null) {
      if (other.plantno != null) {
        return false;
      }
    } else if (!this.plantno.equalsIgnoreCase(other.plantno)) {
      return false;
    }
    return true;
  }

  public static enum Kind { Solar, Wind, FuelCell, Hydel, Nuclear }
}
