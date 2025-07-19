package com.fuel;

public class ExportField {
    private int id;
    private String fieldName;
    private int orderIndex;
    private boolean enabled;
    private String displayName;

    /*--------------------------------------------------------------------------
    field.getFieldName() → nome tecnico in inglese (usato per salvare e export)
    field.getDisplayName() → nome tradotto da mostrare all’utente
    ------------------------------------------------------------------------- */
    public ExportField(int id, String fieldName, int orderIndex, boolean enabled) {
        this.id = id;
        this.fieldName = fieldName;
        this.orderIndex = orderIndex;
        this.enabled = enabled;
    }

    public int getId() { return id; }
    public String getFieldName() { return fieldName; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    //-------------------- per la tarduzione dei fields in italiano
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    public String getDisplayName() {
        return displayName != null ? displayName : fieldName;
    }
    //--------------------
}
