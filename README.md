# sql-execute-maven-plugin

##TODO

To test the plugin:
C:\dev\github\sql-execute-plugin\example>mvn com.ragedunicorn.tools.maven:sql-execute-plugin:1.0-SNAPSHOT:sql-execute

add entry in settings.xml

<server>
  <id>plugin-test</id>
  <username>app</username>
  <password>app</password>
</server>


              <!-- sqlQuery is executed before srcFiles -->
              <sqlQuery></sqlQuery>


C:\dev\github\sql-execute-plugin\plugin\pom.xml to C:\Users\micha\.m2\repository\com\ragedunicorn\tools\maven\sql-execute-plugin\1.0\sql-execute-plugin-1.0.pom
com.ragedunicorn.tools.maven:sql-execute:pom:1.0


Optional<String> sqlCommand = transactions.stream().map(Transaction::getSqlCommand).findAny();


    if (sqlCommand.isPresent()) {
      executeStatements(convertToStream(sqlCommand.get()));
    }


for (Transaction transaction : transactions) {
      String sqlCommand = transaction.getSqlCommand();

      if (sqlCommand != null) {
        executeSQL(convertToStream(sqlCommand));
      }

      File sqlFile = transaction.getSqlFile();

      if (sqlFile != null) {
        executeSQL(convertToStream(sqlFile));
      }
    }


    mysql database example

    https://dev.mysql.com/doc/index-other.html
# sql-execute-maven-plugin
