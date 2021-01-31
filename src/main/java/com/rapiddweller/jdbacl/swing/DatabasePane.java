/*
 * (c) Copyright 2011-2012 by Volker Bergmann. All rights reserved.
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
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.ui.swing.SwingTreeModelAdapter;
import com.rapiddweller.jdbacl.DatabaseTreeModel;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.JDBCMetaDataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.io.IOException;

/**
 * {@link JPanel} which displays database information and the hierarchy tree.<br/><br/>
 * Created: 07.11.2011 16:46:33
 * @since 0.7.0
 * @author Volker Bergmann
 */
@SuppressWarnings("serial")
public class DatabasePane extends JPanel {
	
	private static final Logger LOGGER = LogManager.getLogger(DatabasePane.class);
	
	private final JScrollPane scrollPane;
	private DatabaseTree tree;
	private DBMetaDataImporter importer;
	private final TextFieldValueProvider exclusionPatternProvider;

	public DatabasePane(TextFieldValueProvider exclusionPatternProvider) {
		super(new BorderLayout());
		this.exclusionPatternProvider = exclusionPatternProvider;
		this.scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setEnvironment(String environment) throws ConnectFailedException, ImportFailedException {
		if (importer != null) {
			try {
				importer.close();
			} catch (IOException e) {
				LOGGER.error("Error closing " + getClass().getName(), e);
			}
			if (tree != null)
				scrollPane.remove(tree);
		}
		new Thread(new Importer(environment)).start();
	}

	class Importer implements Runnable {
		final String environment;
		public Importer(String environment) {
			this.environment = environment;
		}
		@Override
		public void run() {
			try {
				Database database = JDBCMetaDataUtil.getMetaData(environment, true, true, true, true, 
						".*", exclusionPatternProvider.getValue(), true, true);
				DatabasePane.this.importer = importer;
				final TreeModel model = new SwingTreeModelAdapter<>(new DatabaseTreeModel(database));
				SwingUtilities.invokeLater(() -> {
					tree = new DatabaseTree(model);
					scrollPane.setViewportView(tree);
				});
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
