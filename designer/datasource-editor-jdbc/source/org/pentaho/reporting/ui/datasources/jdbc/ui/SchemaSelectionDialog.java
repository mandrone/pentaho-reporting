/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2008 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package org.pentaho.reporting.ui.datasources.jdbc.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.base.util.ResourceBundleSupport;
import org.pentaho.reporting.ui.datasources.jdbc.JdbcDataSourceModule;

/**
 * @author David Kincade
 */
public class SchemaSelectionDialog extends JDialog
{
  private class OKButtonActionListener extends AbstractAction
  {
    private OKButtonActionListener()
    {
      putValue(NAME, getBundleSupport().getString("Button.ok"));
    }

    public void actionPerformed(final ActionEvent e)
    {
      dispose();
    }
  }

  private JComboBox schemaComboBox;
  private ResourceBundleSupport bundleSupport;

  public SchemaSelectionDialog(final JDialog owner, final String[] schemas)
  {
    super(owner);
    bundleSupport = new ResourceBundleSupport(Locale.getDefault(), JdbcDataSourceModule.MESSAGES,
        ObjectUtilities.getClassLoader(JdbcDataSourceModule.class));
    setTitle(bundleSupport.getString("SchemaSelectionDialog.ChooseSchema"));
    setModal(true);
    setResizable(true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout());

    schemaComboBox = new JComboBox(schemas);
    final JComboBox comboBox = schemaComboBox;
    add(comboBox, BorderLayout.NORTH);
    final JPanel buttonPanel = new JPanel();
    final JButton okButton = new JButton(new OKButtonActionListener());
    buttonPanel.add(okButton);
    add(buttonPanel, BorderLayout.SOUTH);
    pack();
  }

  public String getSchema()
  {
    setVisible(true);
    return (String) schemaComboBox.getSelectedItem();
  }

  protected ResourceBundleSupport getBundleSupport()
  {
    return bundleSupport;
  }
}
