package com.ragedunicorn.tools.maven;

import java.io.File;

public class Transaction {
  private String sqlCommand;

  private File sqlFile;

  Transaction(final String sqlCommand) {
    this.sqlCommand = sqlCommand;
  }

  Transaction(final File sqlFile) {
    this.sqlFile = sqlFile;
  }

  String getSqlCommand() {
    return sqlCommand;
  }

  public void setSqlCommand(final String sqlCommand) {
    this.sqlCommand = sqlCommand;
  }

  File getSqlFile() {
    return sqlFile;
  }

  public void setSqlFile(final File sqlFile) {
    this.sqlFile = sqlFile;
  }
}
