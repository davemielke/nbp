package org.nbp.compass;

public abstract class ApplicationDefaults {
  private ApplicationDefaults () {
  }

  public final static DistanceUnit DISTANCE_UNIT = DistanceUnit.FEET;
  public final static SpeedUnit SPEED_UNIT = SpeedUnit.MPH;
  public final static AngleUnit ANGLE_UNIT = AngleUnit.DEGREES;
  public final static RelativeDirection RELATIVE_DIRECTION = RelativeDirection.OCLOCK;
  public final static ScreenOrientation SCREEN_ORIENTATION = ScreenOrientation.PORTRAIT;

  public final static boolean LOG_GEOCODING = false;
  public final static boolean LOG_SENSORS = false;
  public final static LocationProvider LOCATION_PROVIDER = LocationProvider.BEST;
}
