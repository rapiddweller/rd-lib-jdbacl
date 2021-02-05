# Release 1.1.0-jdk-11

## Release Highlights

* several new test cases to assure refactoring is more safe
* migrated to Java 11 language features
* centralized functions to build SQL based on dialect in SQLUtils
* implemented __includeTables="#all"__ tag to include all schemas into table validation list
* CaseSensitive database model is now supported based on dialect (postgres, mssql, h2, hsql)

## Important Notes

* ATTENTION : __includeTables="#all"__ takes long time to validate when you have big multischema database
* Oracle and Mysql does not support quoted tables

## Breaking Changes

N/A


