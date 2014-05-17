package pl.poznan.put.common;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.helper.Constants;

public enum ChiTorsionAngleType implements TorsionAngle {
    CHI(MoleculeType.RNA, Constants.UNICODE_CHI),
    CHI1(MoleculeType.PROTEIN, Constants.UNICODE_CHI1),
    CHI2(MoleculeType.PROTEIN, Constants.UNICODE_CHI2),
    CHI3(MoleculeType.PROTEIN, Constants.UNICODE_CHI3),
    CHI4(MoleculeType.PROTEIN, Constants.UNICODE_CHI4),
    CHI5(MoleculeType.PROTEIN, Constants.UNICODE_CHI5);

    public static List<ChiTorsionAngleType> getChiTorsionAngles(
            MoleculeType moleculeType) {
        List<ChiTorsionAngleType> result = new ArrayList<ChiTorsionAngleType>();

        for (ChiTorsionAngleType type : ChiTorsionAngleType.values()) {
            if (type.getMoleculeType() == moleculeType) {
                result.add(type);
            }
        }

        return result;
    }

    private final String displayName;
    private final MoleculeType moleculeType;

    private ChiTorsionAngleType(MoleculeType moleculeType, String displayName) {
        this.moleculeType = moleculeType;
        this.displayName = displayName + " (" + name().toLowerCase() + ")";
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public MoleculeType getMoleculeType() {
        return moleculeType;
    }
}
