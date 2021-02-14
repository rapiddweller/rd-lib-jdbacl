/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.FileUtil;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.OrderedMap;
import com.rapiddweller.common.SystemInfo;
import com.rapiddweller.common.ui.ApplicationUtil;
import com.rapiddweller.common.ui.JavaApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Simple GUI for database browsing.<br/><br/>
 * Created: 07.11.2011 16:28:47
 *
 * @author Volker Bergmann
 * @since 0.7.0
 */
@SuppressWarnings("serial")
public class JdbaclGUI extends JFrame implements JavaApplication {

  private static final Logger LOGGER = LogManager.getLogger(JdbaclGUI.class);

  private static final String DATABENE_DIRECTORY_NAME = SystemInfo.getUserHome() + File.separator + "rapiddweller";
  private static final String GUI_PROPERTIES_FILE_NAME =
      DATABENE_DIRECTORY_NAME + File.separator + "JdbaclGUI.properties";

  private final EnvironmentSelector environmentSelector;
  private final DatabasePane databasePane;

  private final JTextField exclusionField;

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    ApplicationUtil.prepareNativeLAF("jdbacl");
    JdbaclGUI appAndFrame = new JdbaclGUI();
    ApplicationUtil.configureApplication(appAndFrame);
    appAndFrame.setVisible(true);
  }

  /**
   * Instantiates a new Jdbacl gui.
   */
  public JdbaclGUI() {
    this.exclusionField = new JTextField();
    this.environmentSelector = new EnvironmentSelector();
    this.databasePane = new DatabasePane(new TextFieldValueProvider(exclusionField));
    this.environmentSelector.addActionListener(evt -> {
      String environment = environmentSelector.getSelectedItem();
      try {
        databasePane.setEnvironment(environment);
      } catch (ConnectFailedException | ImportFailedException ex) {
        LOGGER.error("Error importing environment " + environment, ex);
      }
    });
    createMenuBar();
    createToolBar();
    getContentPane().add(databasePane, BorderLayout.CENTER);
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    restoreState();
  }

  private void createToolBar() {
    JToolBar toolBar = new JToolBar();
    add(toolBar, BorderLayout.NORTH);
    toolBar.add(new JLabel("Exclusion:"));
    toolBar.add(exclusionField);
    toolBar.add(Box.createHorizontalStrut(8));
    toolBar.add(new JLabel("Environment:"));
    toolBar.add(environmentSelector);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.setFloatable(false);
  }

  private void createMenuBar() {
    JMenuBar menubar = new JMenuBar();

    // create file menu
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    menubar.add(fileMenu);
    if (!SystemInfo.isMacOsx()) {
      fileMenu.add(new AbstractAction("Exit") {
        @Override
        public void actionPerformed(ActionEvent evt) {
          exit();
        }
      });
    }

    // create edit menu
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic('E');
    menubar.add(editMenu);

    // create help menu
    JMenu helpMenu = new JMenu("Help");
    editMenu.setMnemonic('H');
    menubar.add(helpMenu);
    if (!SystemInfo.isMacOsx()) {
      helpMenu.add(new AbstractAction("About") {
        @Override
        public void actionPerformed(ActionEvent evt) {
          about();
        }
      });
    }

    setJMenuBar(menubar);
  }

  @Override
  public void about() {
    JOptionPane.showMessageDialog(this,
        "DB Sanity GUI " + SystemInfo.getLineSeparator() + // include version info
            "(c) 2011 by Volker Bergmann");
  }

  @Override
  public void exit() {
    saveState();
    System.exit(0);
  }

  private void saveState() {
    try {
      FileUtil.ensureDirectoryExists(new File(DATABENE_DIRECTORY_NAME));
      Map<String, String> props = new OrderedMap<>();
      props.put("exclusionPattern", exclusionField.getText());
      IOUtil.writeProperties(props, GUI_PROPERTIES_FILE_NAME);
    } catch (IOException e) {
      // writing the file failed but isn't tragic
    }
  }

  private void restoreState() {
    try {
      Map<String, String> props = IOUtil.readProperties(GUI_PROPERTIES_FILE_NAME);
      exclusionField.setText(props.get("exclusionPattern"));
    } catch (Exception e) {
      // no file defined yet, use default settings
    }
  }

  @Override
  public String iconPath() {
    return null;
  }

  @Override
  public void preferences() {
    // nothing to do
  }

  @Override
  public boolean supportsPreferences() {
    return false;
  }

}
