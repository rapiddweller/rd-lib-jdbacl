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

package com.rapiddweller.jdbacl;

import com.rapiddweller.commons.ConnectFailedException;
import com.rapiddweller.commons.ImportFailedException;
import com.rapiddweller.commons.tree.TreeLogger;
import com.rapiddweller.commons.ui.ConsoleInfoPrinter;
import com.rapiddweller.commons.version.VersionInfo;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.JDBCMetaDataUtil;

/**
 * Retrieves meta data from a database and prints it to the console in a tree structure.<br/><br/>
 * Created: 26.06.2011 07:38:38
 * @since 0.6.9
 * @author Volker Bergmann
 */
public class Main {

	public static void main(String[] args) throws ConnectFailedException, ImportFailedException {
		String environment = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if ("-h".equals(arg)) {
				printHelpAndExit(0);
			} else {
				environment = arg;
			}
		}
		if (environment == null)
			printErrorAndHelpAndExit("No environment specified", -1);
		Database database = JDBCMetaDataUtil.getMetaData(environment, true, true, true, true, ".*", null, false, true);
		new TreeLogger().log(new DatabaseTreeModel(database));
	}

	private static void printErrorAndHelpAndExit(String message, int status) {
		ConsoleInfoPrinter.printHelp("Error: " + message);
	    printHelp();
	    System.exit(status);
    }

	private static void printHelpAndExit(int status) {
	    printHelp();
	    System.exit(status);
    }

	private static void printHelp() {
		VersionInfo version = VersionInfo.getInfo("jdbacl");
		ConsoleInfoPrinter.printHelp("jdbacl " + version);
		ConsoleInfoPrinter.printHelp("Usage: java -jar jdbacle-" + version.getVersion() + ".jar [options] <environment>");
		ConsoleInfoPrinter.printHelp(
			"",
			"Options:",
			"-h,--help               print this help",
			"",
			"The environment, eg. 'mydb', refers to a properties file, e.g. 'mydb.env.properties',",
			"which must provide JDBC connection data in the following format:",
			"	db_url=jdbc:hsqldb:hsql://localhost/mydb",
			"	db_driver=org.hsqldb.jdbcDriver",
			"	db_user=customer", 
			"	db_password=secret"
		);
	}

}
