/*
 * Copyright (c) 2022 Michael Wiesendanger
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:

 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.ragedunicorn.tools.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

@Mojo(name = "sql-execute")
public class SqlExecuteMojo extends AbstractMojo {
  // the database driver to use
  @Parameter(property = "driver", required = true)
  private String driver;

  // connection string for the database
  @Parameter(property = "url", required = true)
  private String url;

  // user to use to connect to the database - overwritten by server
  @Parameter(property = "user")
  private String user;

  // password to use to connect to the database - overwritten by server
  @Parameter(property = "password")
  private String password;

  // references a server configuration in your .m2 settings.xml.
  // This is the preferred way for storing credentials
  @Parameter(property = "server")
  private String server;

  // an sql query to execute
  @Parameter(property = "sqlQuery")
  private String sqlQuery;

  // a list of one or more sql files to execute
  @Parameter(property = "sqlFiles")
  private File[] sqlFiles;

  @Parameter(defaultValue = "${mojoExecution}", readonly = true)
  private MojoExecution execution;

  @Parameter(defaultValue = "${settings}", readonly = true)
  private Settings settings;

  private List<Transaction> transactions = new ArrayList<>();

  // connection to the database
  private Connection connection;

  /**
   * Plugin execution callback.
   *
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  public void execute() throws MojoExecutionException {
    validateRequiredInputParameters();

    if (sqlQuery != null) {
      Transaction transaction = new Transaction(sqlQuery);
      transactions.add(transaction);
    }

    if (sqlFiles != null) {
      for (File sqlFile : sqlFiles) {
        Transaction transaction = new Transaction(sqlFile);
        transactions.add(transaction);
      }
    }

    connection = getConnection(url, getCredentials());

    if (connection != null && testConnection(connection)) {
      getLog().info("Connection to database was successful");
      processTransactions();
    }
  }

  /**
   * Validate required input parameters.
   *
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private void validateRequiredInputParameters() throws MojoExecutionException {
    if (driver == null || driver.isEmpty()) {
      throw new MojoExecutionException("Missing required parameter driver");
    }

    if (url == null || url.isEmpty()) {
      throw new MojoExecutionException("Missing required parameter url");
    }
  }

  /**
   * Retrieves a connection to a database.
   *
   * @param url  A fully qualified database url
   * @param info Contains credentials necessary to connect to the database
   * @return A connection to a database
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private Connection getConnection(final String url, final Properties info)
      throws MojoExecutionException {
    try {
      return DriverManager.getConnection(url, info);
    } catch (SQLException e) {
      throw new MojoExecutionException("Failed to establish connection to database", e);
    }
  }

  /**
   * Check whether a successful connection to the database could be established by executing a
   * simple query.
   *
   * @param connection An established connection to a database
   * @return true for a successful connection otherwise false
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private boolean testConnection(final Connection connection) throws MojoExecutionException {
    try {
      Statement stm = connection.createStatement();
      ResultSet resultSet;
      resultSet = stm.executeQuery("SELECT 1");

      if (resultSet.next() && resultSet.getInt(1) == 1) {
        return true;
      } else {
        getLog().error("Testing database connection returned error");
        return false;
      }
    } catch (SQLException e) {
      throw new MojoExecutionException("SQLException while testing connection to database", e);
    }
  }

  /**
   * Gather credentials for connecting to the database. First search through the users settings.xml
   * before falling back to the configuration inside the pom file itself.
   * While the user cannot be empty the password is optional.
   *
   * @return A property object containing credentials to a database
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private Properties getCredentials() throws MojoExecutionException {
    String user = null;
    String password = null;

    // prefer settings parameter over direct configuration in pom
    if (settings != null && server != null) {
      final Server serverEntry = settings.getServer(server);
      if (serverEntry != null) {
        user = serverEntry.getUsername();
        password = serverEntry.getPassword();

        if (user == null || user.isEmpty()) {
          throw new MojoExecutionException("Found server entry in settings.xml "
              + "but user parameter was missing or is empty");
        }

        // password for a database may be empty
        if (password == null || password.isEmpty()) {
          getLog().warn("Found server entry in settings.xml "
              + "but password parameter was missing or is empty");
        }
      } else {
        getLog().debug("Unable to retrieve settings or server. Falling back to project settings");
      }
    }

    Properties info = new Properties();
    // fallback to plugin configuration if credentials could not be retrieved from mvn settings.xml
    if (user != null) {
      info.setProperty("user", user);
    } else if (this.user != null) {
      info.setProperty("user", this.user);
    } else {
      throw new MojoExecutionException("Unable to read user configuration make"
          + "sure to set the user property");
    }

    if (password != null) {
      info.setProperty("password", password);
    } else if (this.password != null) {
      info.setProperty("password", this.password);
    } else {
      getLog().warn("No password set. Using empty password for connection");
      info.setProperty("password", "");
    }

    return info;
  }

  /**
   * Process all transactions and execute them on the database.
   *
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private void processTransactions() throws MojoExecutionException {
    if (transactions == null) {
      throw new MojoExecutionException("Unable to read transactions");
    }

    for (Transaction transaction : transactions) {
      String sqlCommand = transaction.getSqlCommand();

      if (sqlCommand != null) {
        executeStatements(convertToStream(sqlCommand));
      }

      File sqlFile = transaction.getSqlFile();

      if (sqlFile != null) {
        executeStatements(convertToStream(sqlFile));
      }
    }
  }

  /**
   * Convert an sqlCommand to an InputStream.
   *
   * @param sqlCommand An sql query that should get converted
   * @return InputStream or null if {link #sqlCommand} could not get converted
   */
  private InputStream convertToStream(final String sqlCommand) {
    if (sqlCommand != null && !sqlCommand.isEmpty()) {
      // convert sql string to stream input
      return new ByteArrayInputStream(sqlCommand.getBytes(StandardCharsets.UTF_8));
    }

    getLog().warn("sqlQuery statement is empty - skipping");
    return null;
  }

  /**
   * Convert an SQL file to an InputStream. If the file does not exist the operation is halted
   * preventing any further statements that might depend on this to be executed.
   *
   * @param sqlFile An SQL file that should get converted
   * @return InputStream or null if {link #sqlFile} could not get converted
   */
  private InputStream convertToStream(final File sqlFile) throws MojoExecutionException {
    if (sqlFile != null) {
      try {
        return new FileInputStream(sqlFile);
      } catch (FileNotFoundException e) {
        throw new MojoExecutionException("Configured sqlFile does not exist");
      }
    } else {
      throw new MojoExecutionException("Configured sqlFile does not exist");
    }
  }

  /**
   * Execute a stream of sql statements.
   *
   * @param stream A stream of sql statements
   * @throws MojoExecutionException An exception occurring during the execution of a plugin
   */
  private void executeStatements(final InputStream stream) throws MojoExecutionException {
    getLog().info("Processing " + execution.getGoal() + ":" + execution.getExecutionId());

    if (stream == null) {
      throw new MojoExecutionException("Missing stream parameter");
    }

    Scanner scanner = new Scanner(stream, "UTF-8");
    scanner.useDelimiter("(;(\r)?\n)|(--\n)");
    Statement st = null;

    try {
      st = connection.createStatement();
      while (scanner.hasNext()) {
        String line = scanner.next();
        if (line.startsWith("/*!") && line.endsWith("*/")) {
          int i = line.indexOf(' ');
          line = line.substring(i + 1, line.length() - " */".length());
        }

        // skip empty lines and lines that contain only linefeeds
        if (!line.isEmpty() && !line.matches("[\\n\\r]+")) {
          st.execute(line);
        }
      }
    } catch (SQLException e) {
      throw new MojoExecutionException("Failed to execute sql", e);
    } finally {
      if (st != null) {
        try {
          st.close();
        } catch (SQLException e) {
          getLog().error("Failed to close statement", e);
        }
      }
    }
  }
}

