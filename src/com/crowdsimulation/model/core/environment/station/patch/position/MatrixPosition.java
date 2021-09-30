package com.crowdsimulation.model.core.environment.station.patch.position;

import com.crowdsimulation.model.core.environment.station.Station;

import java.util.Objects;

public class MatrixPosition extends Location {
    private final int row;
    private final int column;

    public MatrixPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public static boolean inBounds(MatrixPosition matrixPosition, Station station) {
        return matrixPosition.getRow() >= 0 && matrixPosition.getRow() < station.getRows()
                && matrixPosition.getColumn() >= 0 && matrixPosition.getColumn() < station.getColumns();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatrixPosition that = (MatrixPosition) o;
        return row == that.row &&
                column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}
