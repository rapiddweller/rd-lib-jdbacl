# Release 1.1.2-jdk-11

## Release Highlights

* improved way of handling databases with multi schema references

## Important Notes

* Known Issue: when you have two tables with identical name in different schema, imported as Database connection in your
  benerator script ( for example: tableA in schema1 as db connection id="db1"
  and tableA in schema2 as dbconnection id="db2" ), the framework won't be able to identify the right table when it
  comes to persisting entities to Database.

## Breaking Changes

* removed __includeTables="#all"__ tag, this is not necessary anymore, because the is a mechanism to identify related
  schema automatically based on JDBC metadata.

---

# Release 1.1.1-jdk-11

## Release Highlights

N/A

## Important Notes

* HotFix: there were foreign schema index and keys missing in multiSchema object structure

## Breaking Changes

N/A

---

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


