package uk.co.createanet.footballformapp.models;

/**
 * Created by matt on 09/04/2014.
 */
public class ColumnIdentifier {

    public enum ColumnSize {
        SMALL, MEDIUM, LARGE
    }

    public ColumnIdentifier(String title, ColumnSize columnSize, String sqlColumn) {
        this(title, columnSize, sqlColumn, false);
    }

    public ColumnIdentifier(String title, ColumnSize columnSize, String sqlColumn, boolean sortable){
        this.title = title;
        this.columnSize = columnSize;
        this.sqlColumn = sqlColumn;
        this.sortable = sortable;
    }

    public String title;
    public ColumnSize columnSize;
    public String sqlColumn;
    public boolean sortable;

    public int getColumnSizePx(){
        switch (columnSize){
            case SMALL:
                return 100;
            case MEDIUM:
                return 200;
            case LARGE:
                return 400;
        }

        return 600;
    }

}
