package pl.poznan.put.torsion;

import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.samples.AngleSample;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbResidue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class AverageTorsionAngleType extends TorsionAngleType
        implements MasterTorsionAngleType {
    private final String displayName;
    private final String exportName;
    private final List<MasterTorsionAngleType> consideredAngles;

    public AverageTorsionAngleType(
            final MoleculeType moleculeType,
            final MasterTorsionAngleType... masterTypes) {
        super(moleculeType);
        consideredAngles = Arrays.asList(masterTypes);
        displayName = AverageTorsionAngleType.toDisplayName(consideredAngles);
        exportName = AverageTorsionAngleType.toExportName(consideredAngles);
    }

    private static String toDisplayName(
            final Iterable<MasterTorsionAngleType> consideredAngles) {
        Collection<String> angleNames = new LinkedHashSet<>();
        for (final MasterTorsionAngleType angleType : consideredAngles) {
            angleNames.add(angleType.getShortDisplayName());
        }

        StringBuilder builder = new StringBuilder("MCQ(");
        Iterator<String> iterator = angleNames.iterator();
        builder.append(iterator.next());

        while (iterator.hasNext()) {
            builder.append(", ");
            builder.append(iterator.next());
        }

        builder.append(')');
        return builder.toString();
    }

    private static String toExportName(
            final Iterable<MasterTorsionAngleType> consideredAngles) {
        Collection<String> angleNames = new LinkedHashSet<>();
        for (final MasterTorsionAngleType angleType : consideredAngles) {
            angleNames.add(angleType.getExportName());
        }

        StringBuilder builder = new StringBuilder("MCQ_");
        Iterator<String> iterator = angleNames.iterator();
        builder.append(iterator.next());

        while (iterator.hasNext()) {
            builder.append('_');
            builder.append(iterator.next());
        }

        return builder.toString();
    }

    public AverageTorsionAngleType(
            final MoleculeType moleculeType,
            final List<MasterTorsionAngleType> consideredAngles) {
        super(moleculeType);
        this.consideredAngles = new ArrayList<>(consideredAngles);
        displayName = AverageTorsionAngleType.toDisplayName(consideredAngles);
        exportName = AverageTorsionAngleType.toExportName(consideredAngles);
    }

    private AverageTorsionAngleType(
            final MoleculeType moleculeType,
            final List<MasterTorsionAngleType> consideredAngles,
            final String displayName, final String exportName) {
        super(moleculeType);
        this.consideredAngles = consideredAngles;
        this.displayName = displayName;
        this.exportName = exportName;
    }

    @Override
    public String getLongDisplayName() {
        return displayName;
    }

    @Override
    public String getShortDisplayName() {
        return displayName;
    }

    @Override
    public String getExportName() {
        return exportName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Iterable<MasterTorsionAngleType> getConsideredAngles() {
        return Collections.unmodifiableList(consideredAngles);
    }

    @Override
    public TorsionAngleValue calculate(
            final List<PdbResidue> residues, final int currentIndex) {
        PdbResidue residue = residues.get(currentIndex);
        List<Angle> angles = new ArrayList<>();

        for (final TorsionAngleType type : residue.getTorsionAngleTypes()) {
            for (final MasterTorsionAngleType masterType : consideredAngles) {
                if (masterType.getAngleTypes().contains(type)) {
                    TorsionAngleValue angleValue =
                            type.calculate(residues, currentIndex);
                    angles.add(angleValue.getValue());
                }
            }
        }

        AngleSample angleSample = new AngleSample(angles);
        return new TorsionAngleValue(this, angleSample.getMeanDirection());
    }

    public TorsionAngleValue calculate(
            final Iterable<TorsionAngleValue> values) {
        List<Angle> angles = new ArrayList<>();

        for (final MasterTorsionAngleType masterType : consideredAngles) {
            for (final TorsionAngleValue angleValue : values) {
                if (masterType.getAngleTypes()
                              .contains(angleValue.getAngleType())) {
                    if (angleValue.isValid()) {
                        angles.add(angleValue.getValue());
                    }
                    break;
                }
            }
        }

        AngleSample angleSample = new AngleSample(angles);
        return new TorsionAngleValue(this, angleSample.getMeanDirection());
    }

    @Override
    public Collection<? extends TorsionAngleType> getAngleTypes() {
        return Collections.singleton(this);
    }
}