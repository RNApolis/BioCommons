package pl.poznan.put.circular;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import pl.poznan.put.circular.enums.ValueType;
import pl.poznan.put.circular.exception.InvalidVectorFormatException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AngleTest {
  private static final Angle DEGREES_0 = new Angle(0, ValueType.DEGREES);
  private static final Angle DEGREES_45 = new Angle(45.0, ValueType.DEGREES);
  private static final Angle DEGREES_90 = new Angle(90.0, ValueType.DEGREES);
  private static final Angle DEGREES_135 = new Angle(135.0, ValueType.DEGREES);
  private static final Angle DEGREES_180 = new Angle(180.0, ValueType.DEGREES);
  private static final Angle DEGREES_225 = new Angle(225.0, ValueType.DEGREES);
  private static final Angle DEGREES_270 = new Angle(270.0, ValueType.DEGREES);
  private static final Angle DEGREES_315 = new Angle(315.0, ValueType.DEGREES);
  private static final Angle[] ANGLES = {
    AngleTest.DEGREES_0, AngleTest.DEGREES_45, AngleTest.DEGREES_90,
    AngleTest.DEGREES_135, AngleTest.DEGREES_180, AngleTest.DEGREES_225,
    AngleTest.DEGREES_270, AngleTest.DEGREES_315
  };

  @Test
  public final void fromHourMinuteString() {
    assertThat(Angle.fromHourMinuteString("00.00"), is(AngleTest.DEGREES_0));
    assertThat(Angle.fromHourMinuteString("03.00"), is(AngleTest.DEGREES_45));
    assertThat(Angle.fromHourMinuteString("06.00"), is(AngleTest.DEGREES_90));
    assertThat(Angle.fromHourMinuteString("09.00"), is(AngleTest.DEGREES_135));
    assertThat(Angle.fromHourMinuteString("12.00"), is(AngleTest.DEGREES_180));
    assertThat(Angle.fromHourMinuteString("15.00"), is(AngleTest.DEGREES_225));
    assertThat(Angle.fromHourMinuteString("18.00"), is(AngleTest.DEGREES_270));
    assertThat(Angle.fromHourMinuteString("21.00"), is(AngleTest.DEGREES_315));
  }

  @Test(expected = InvalidVectorFormatException.class)
  public final void fromHourMinuteStringInvalidDots() {
    Angle.fromHourMinuteString("00.00.00");
  }

  @Test(expected = InvalidVectorFormatException.class)
  public final void fromHourMinuteStringInvalidNumber() {
    Angle.fromHourMinuteString("aa.bb");
  }

  @Test
  public final void subtract() {
    for (int i = 0; i < 360; i++) {
      final double ri = FastMath.toRadians(i);
      for (int j = 0; j < 360; j++) {
        final double rj = FastMath.toRadians(j);
        assertEquals(
            String.format("Difference in subtraction for: %d and %d", i, j),
            Angle.subtractByMinimum(ri, rj),
            Angle.subtractAsVectors(ri, rj),
            1.0e-6);
      }
    }
  }

  @Test
  public final void invalidInstance() {
    final Angle invalidInstance = Angle.invalidInstance();
    assertThat(invalidInstance.isValid(), is(false));

    // whatever operation you do, the result remains invalid
    for (final Angle angle : AngleTest.ANGLES) {
      assertThat(invalidInstance.subtract(angle).isValid(), is(false));
    }

    // all values are NaN
    assertThat(Double.isNaN(invalidInstance.getDegrees()), is(true));
    assertThat(Double.isNaN(invalidInstance.getDegrees360()), is(true));
    assertThat(Double.isNaN(invalidInstance.getRadians()), is(true));
    assertThat(Double.isNaN(invalidInstance.getRadians2PI()), is(true));
  }

  @Test
  public final void testIsBetween() {
    // 0 <= 45 < 90
    assertThat(AngleTest.DEGREES_45.isBetween(AngleTest.DEGREES_0, AngleTest.DEGREES_90), is(true));
    // 45 <= 45 < 90
    assertThat(
        AngleTest.DEGREES_45.isBetween(AngleTest.DEGREES_45, AngleTest.DEGREES_90), is(true));
    // not (0 <= 45 < 45)
    assertThat(
        AngleTest.DEGREES_45.isBetween(AngleTest.DEGREES_0, AngleTest.DEGREES_45), is(false));
    // not (45 <= 0 < 90)
    assertThat(
        AngleTest.DEGREES_0.isBetween(AngleTest.DEGREES_45, AngleTest.DEGREES_90), is(false));
    // 315 <= 0 < 45
    assertThat(
        AngleTest.DEGREES_0.isBetween(AngleTest.DEGREES_315, AngleTest.DEGREES_45), is(true));
    // 270 <= 315 < 45
    assertThat(
        AngleTest.DEGREES_315.isBetween(AngleTest.DEGREES_270, AngleTest.DEGREES_45), is(true));
  }

  @Test
  public final void multiply() {
    // multiplied by 1.0 does not change the value
    for (final Angle angle : AngleTest.ANGLES) {
      assertThat(angle.multiply(1.0), is(angle));
    }

    // multiplied by 2.0, the distance is equal to the value itself
    for (final Angle angle : AngleTest.ANGLES) {
      assertThat(angle.multiply(2.0).orderedSubtract(angle), is(angle));
    }
  }

  @Test
  public final void orderedSubtract() {
    final int length = AngleTest.ANGLES.length;
    for (int i = 1; i < length; i++) {
      final Angle ai = AngleTest.ANGLES[i];

      for (int j = i + 1; j < length; j++) {
        final Angle aj = AngleTest.ANGLES[j];
        final double dij = ai.orderedSubtract(aj).getRadians();
        final double dji = aj.orderedSubtract(ai).getRadians();
        assertEquals(String.format("Test failed for: %s and %s", ai, aj), dij, -dji, 1.0e-3);
      }
    }
  }
}
