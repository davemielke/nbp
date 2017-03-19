package org.nbp.calculator.conversion;

public class IncompatibleUnitsException extends ConversionException {
  public IncompatibleUnitsException (Unit unit1, Unit unit2) {
    super(("incompatible units: " + unit1.getName() + " & " + unit2.getName()));
  }
}
