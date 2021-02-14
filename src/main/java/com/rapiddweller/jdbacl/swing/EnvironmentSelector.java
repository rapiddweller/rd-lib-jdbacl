/*
 * (c) Copyright 2011-2014 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.jdbacl.swing;

import com.rapiddweller.jdbacl.DBUtil;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 * {@link JComboBox} which recognizes the configured environments and lets the user choose one.<br/><br/>
 * Created: 06.11.2011 19:26:37
 *
 * @author Volker Bergmann
 * @since 0.7.0
 */
@SuppressWarnings("serial")
public class EnvironmentSelector extends JComboBox {

  /**
   * Instantiates a new Environment selector.
   */
  public EnvironmentSelector() {
    this(null);
  }

  /**
   * Instantiates a new Environment selector.
   *
   * @param selection the selection
   */
  public EnvironmentSelector(String selection) {
    super(new EnvironmentModel());
    if (selection != null) {
      setSelectedItem(selection);
    }
  }

  @Override
  public String getSelectedItem() {
    return (String) super.getSelectedItem();
  }

  /**
   * The type Environment model.
   */
  public static class EnvironmentModel extends AbstractListModel implements ComboBoxModel {

    private String selectedItem;
    private final String[] environments;

    /**
     * Instantiates a new Environment model.
     */
    EnvironmentModel() {
      environments = DBUtil.getEnvironmentNames();
    }

    @Override
    public Object getElementAt(int index) {
      return environments[index];
    }

    @Override
    public int getSize() {
      return environments.length;
    }

    @Override
    public Object getSelectedItem() {
      return selectedItem;
    }

    @Override
    public void setSelectedItem(Object item) {
      this.selectedItem = (String) item;
    }

  }

}
