# Release 1.1.16-jdk-11

## Release Highlights
* related to rapiddweller-benerator-ce 3.2.1 release check CHANGE_LOG.md for more details.

---

# Release 1.1.15-jdk-11

## Release Highlights
* support Vertica database now
* some improvements regarding multi threading
* some dependency updates

---

# Release 1.1.14-jdk-11

## Release Highlights
* adjust JDBC Details List for benerator CE wizard
* add dbCatalog to JDBC Details

---

# Release 1.1.13-jdk-11

## Release Highlights
* add missing Serializable interface

---

# Release 1.1.12-jdk-11

## Release Highlights
* fix(hsql): handle Jdbc Metatdataimport import sequences
* Removed duplicate attribute 2. Avoiding array index exception
* Introduced DbQueryFailed
* Improved logging
* Added QueryDataIterator.toString()
* Explicitly committing executed DDL statements
* Using log4j 2.17.0
* Improved exception handling
* Migrated to new XML parser setup
* Introduced UUID, BYTEA and JSON data types of Postgres 
* Improved code quality

---

# Release 1.1.11-jdk-11

## Release Highlights

* return back to slf4j
* upgrade rd-lib-common to 1.1.3
* upgrade rd-lib-contiperf to 2.5.0
* upgrade rd-lib-format to 1.1.4
* upgrade rd-lib-script to 1.1.3

---

# Release 1.1.10-jdk-11

## Release Highlights

* introduce checkstyle config
* reformat and style
* pom improvements
* update rd-libs, JUnit and log4j

---
# Release 1.1.9-jdk-11

## Release Highlights

* skipped

---
# Release 1.1.8-jdk-11

## Release Highlights

* improve error messages
* adjustments for benerator archetypes (db driver info)
* driver update and better pom props

---

# Release 1.1.7-jdk-11

## Release Highlights

Fix oracle sql handling

* exclude SYS_ Tables from metadata import
* handle sql builder for oracle correctly without catalog
* refactoring

---

# Release 1.1.6-jdk-11

## Release Highlights

* prevent potential errors by changing the way how getForeignSchemas works

---

# Release 1.1.5-jdk-11

## Release Highlights

* JDBCDBImporter add catalog
* switched to official MS SQL JDBC Driver

---

# Release 1.1.4-jdk-11

## Release Highlights

* multischema fix H2 dialect

---

# Release 1.1.3-jdk-11

## Release Highlights

* upgrade Postgresql dependency
* upgrade H2 dependency
* upgrade rd-lib-common dependency

## Important Notes

* Known Issue: when you have two tables with identical name in different schema, imported as Database connection in your benerator script ( for example: tableA in schema1 as db connection id="db1"
  and tableA in schema2 as dbconnection id="db2" ), the framework won't be able to identify the right table when it comes to persisting entities to Database.

## Breaking Changes

* removed __includeTables="#all"__ tag, this is not necessary anymore, because the is a mechanism to identify related schema automatically based on JDBC metadata.

---

# Release 1.1.2-jdk-11

## Release Highlights

* improved way of handling databases with multi schema references

## Important Notes

* Known Issue: when you have two tables with identical name in different schema, imported as Database connection in your benerator script ( for example: tableA in schema1 as db connection id="db1"
  and tableA in schema2 as dbconnection id="db2" ), the framework won't be able to identify the right table when it comes to persisting entities to Database.

## Breaking Changes

* removed __includeTables="#all"__ tag, this is not necessary anymore, because the is a mechanism to identify related schema automatically based on JDBC metadata.

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


