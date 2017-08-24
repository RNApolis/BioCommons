package pl.poznan.put.torsion;

import lombok.Data;
import lombok.NoArgsConstructor;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.interfaces.DisplayableExportable;
import pl.poznan.put.utility.AngleFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@XmlRootElement
public class TorsionAngleValue implements DisplayableExportable {
    public static TorsionAngleValue invalidInstance(
            final TorsionAngleType type) {
        return new TorsionAngleValue(type, Angle.invalidInstance());
    }

    @XmlElement private TorsionAngleType angleType;
    @XmlElement private Angle value;

    public TorsionAngleValue(final TorsionAngleType angleType,
                             final Angle value) {
        super();
        this.angleType = angleType;
        this.value = value;
    }

    @Override
    public final String toString() {
        return getLongDisplayName();
    }

    @Override
    public final String getLongDisplayName() {
        return String.format("%s %s", angleType.getLongDisplayName(),
                             AngleFormat.formatDisplayLong(value.getRadians()));
    }

    @Override
    public final String getShortDisplayName() {
        return String.format("%s %s", angleType.getShortDisplayName(),
                             AngleFormat
                                     .formatDisplayShort(value.getRadians()));
    }

    @Override
    public final String getExportName() {
        return String.format("%s %s", angleType.getExportName(),
                             AngleFormat.formatExport(value.getRadians()));
    }
}
