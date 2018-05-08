package pl.poznan.put.atom;

public enum AtomType {
  C(true),
  H(false),
  N(true),
  O(true),
  P(true),
  S(true),
  OTHER(true);

  private final boolean isHeavy;

  AtomType(final boolean isHeavy) {
    this.isHeavy = isHeavy;
  }

  public boolean isHeavy() {
    return isHeavy;
  }
}
