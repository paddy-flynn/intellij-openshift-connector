/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.service;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jboss.tools.intellij.openshift.utils.odo.ServiceTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateServiceDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField nameField;
    private JComboBox serviceTemplatesComboBox;
    private JTextField applicationTextField;
    private JComboBox servicePlanComboBox;

    public CreateServiceDialog(Component parent) {
        super(null, false, IdeModalityType.IDE);
        init();
        setTitle("Create service");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public void setServiceTemplates(ServiceTemplate[] serviceTemplates) {
        serviceTemplatesComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                value = ((ServiceTemplate) value).getName();
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        serviceTemplatesComboBox.addItemListener(item -> templateSelected((ServiceTemplate) item.getItem()));
        serviceTemplatesComboBox.setModel(new DefaultComboBoxModel(serviceTemplates));
        if (serviceTemplates.length > 0) {
            templateSelected(serviceTemplates[0]);
        }
    }

    private void templateSelected(ServiceTemplate template) {
        servicePlanComboBox.setModel(new DefaultComboBoxModel(template.getPlans().toArray()));
        servicePlanComboBox.setSelectedIndex(0);
        if (template.getPlans().size() < 2) {
            servicePlanComboBox.setEnabled(false);
        } else {
            servicePlanComboBox.setEnabled(true);
        }
    }

    public String getName() {
        return nameField.getText();
    }

    public ServiceTemplate getServiceTemplate() {
        return (ServiceTemplate) serviceTemplatesComboBox.getSelectedItem();
    }

    public String getServiceTemplatePlan() {
        return (String) servicePlanComboBox.getSelectedItem();
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validations = new ArrayList<>();
        if (nameField.getText().length() == 0) {
            validations.add(new ValidationInfo("Name must be provided", nameField));
        }
        if (applicationTextField.getText().length() == 0) {
            validations.add(new ValidationInfo("Application must be provided", applicationTextField));
        }
        return validations;
    }

    public void setApplication(String application){
        applicationTextField.setText(application);
        applicationTextField.setEditable(false);
    }

    public String getApplication(){
        return applicationTextField.getText();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameField = new JTextField();
        panel1.add(nameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Template");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serviceTemplatesComboBox = new JComboBox();
        panel1.add(serviceTemplatesComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
