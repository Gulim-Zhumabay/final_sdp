package gui;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class FormPanel extends JPanel {
    private static final int GAP = 14;
    private static final int LABEL_GAP = 4;

    private int row = 0;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public void addRow(String label, Component field) {
        add(Ui.formLabel(label), cell(gap(GAP), LABEL_GAP, GridBagConstraints.NONE));
        add(field, cell(0, 0, GridBagConstraints.HORIZONTAL));
    }

    public void addWide(Component component) {
        addWide(component, GAP + 6);
    }

    public void addWide(Component component, int topGap) {
        add(component, cell(gap(topGap), 0, GridBagConstraints.HORIZONTAL));
    }

    private int gap(int value) {
        return row == 0 ? 0 : value;
    }

    private GridBagConstraints cell(int top, int bottom, int fill) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row++;
        constraints.weightx = 1;
        constraints.fill = fill;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(top, 0, bottom, 0);
        return constraints;
    }
}