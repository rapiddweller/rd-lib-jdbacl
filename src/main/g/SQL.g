/*
 * (c) Copyright 2009-2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
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

grammar SQL;

options {
	output=AST;
}

@header {
	package com.rapiddweller.jdbacl.sql.parser;
}

@lexer::header{ 
	package com.rapiddweller.jdbacl.sql.parser;
}

@lexer::members {
	@Override
	public Token nextToken() {
		while (true) {
			state.token = null;
			state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
			state.text = null;
			if ( input.LA(1)==CharStream.EOF ) {
				return Token.EOF_TOKEN;
			}
			try {
				mTokens();
				if ( state.token==null ) {
					emit();
				}
				else if ( state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return state.token;
			}
			catch (RecognitionException re) {
				reportError(re);
				throw new RuntimeException(getClass().getSimpleName() + " error", re); // or throw Error
			}
		}
	}

}

@members {
	protected void mismatch(IntStream input, int ttype, BitSet follow)
	  throws RecognitionException
	{
	  throw new MismatchedTokenException(ttype, input);
	}
	
	public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow)
	  throws RecognitionException
	{
	  throw e;
	}
}

@rulecatch {
	catch (RecognitionException e) {
	  throw e;
	}
}

commands: command*;

command	: (create_table    ';'!)
	| (comment         ';'!)
	| (drop_table      ';'!)
	| (alter_table     ';'!)
	| (create_index    ';'!)
	| (drop_index      ';'!)
	| (create_sequence ';'!)
	| (drop_sequence   ';'!)
	;



create_table	: 'CREATE' 'GLOBAL'? 'TEMPORARY'? 'TABLE' table_name '(' table_details ')' ora_configs -> ^(CREATE_TABLE table_name table_details ora_configs);
table_name	: qualified_name -> ^(TABLE_NAME qualified_name);
table_details	: table_detail (',' table_detail)* -> ^(TABLE_DETAILS table_detail*);
table_detail	: column_spec | inline_constraint;
column_spec	: name type size? column_detail* -> ^(COLUMN_SPEC name type size? column_detail*);
type	: 'NUMBER' 
	| 'VARCHAR2' 
	| 'NVARCHAR2' 
	| 'CHAR' 
	| 'NCHAR2' 
	| 'TIMESTAMP' 
	| 'DATE' 
	| 'BLOB' 
	| 'CLOB' 
	| 'NCLOB' 
	| 'ROWID';

size		: '(' INT size_uom? fractionDigits? ')' -> ^(SIZE INT size_uom? fractionDigits?);
fractionDigits	: ','! INT;
size_uom	: 'BYTE' | 'CHAR';
column_detail	: nullability | default_value;
nullability	: NOT^? 'NULL';
default_value	: 'DEFAULT'^ value;

inline_constraint: ('CONSTRAINT'! name)? 'PRIMARY'^ 'KEY'! '('! name_list ')'!; 

ora_configs	: ora_config* -> ^(ORA_CONFIGS ora_config*);
ora_config
	: ('ORGANIZATION'^ name)
	| ('TABLESPACE'^ name)
	| ('PCTUSED'^ INT)
	| ('PCTFREE'^ INT)
	| ('INITRANS'^ INT)
	| ('MAXTRANS'^ INT)
	| ('COMPRESS'^ INT)
	| ora_storage_spec
	| ora_partition_spec
	| ora_flag
	| ora_lob
	| ('ENABLE' 'ROW'^ 'MOVEMENT'!)
	| ('DISABLE' 'ROW'^ 'MOVEMENT'!)
	| ('ON' 'COMMIT' 'DELETE' 'ROWS')
	;

ora_key		: 'TABLESPACE' | 'PCTUSED' | 'PCTFREE' | 'INITRANS' | 'MAXTRANS' | 'STORAGE' | ora_flag;
ora_storage_spec	: 'STORAGE'^ '(' ora_storage_opt* ')';
ora_storage_opt
	: ('INITIAL'^     INT IDENTIFIER)
	| ('NEXT'^        INT)
	| ('MINEXTENTS'^  INT)
	| ('MAXEXTENTS'^  IDENTIFIER)
	| ('PCTINCREASE'^ INT)
	| ('BUFFER_POOL'^ name)
	;
	
ora_partition_spec	: 'PARTITION'^ 'BY'! ('RANGE' | 'HASH' | 'LIST') '('! name ')'! ('SUBPARTITION' 'BY'! ('RANGE' | 'HASH') '('! name ')'!)? partition_part_config;
partition_part_config	: '('! partition_parts ')'!
			| 'PARTITIONS' INT 'STORE' 'IN' '(' name_list ')';
partition_parts	: partition_part (',' partition_part)*;
partition_part	: 'PARTITION'^ name 'VALUES' partition_value_spec ora_configs;
partition_value_spec	: ('LESS' 'THAN' expression ora_configs sub_partition_config)
			| ('(' value_list ')');
sub_partition_config	: 'SUBPARTITIONS'^ INT 'STORE' 'IN' '(' name_list ')';
ora_flag: 'LOGGING' 
	| 'NOLOGGING' 
	| 'NOCOMPRESS' 
	| 'CACHE' 
	| 'NOCACHE' 
	| 'NOPARALLEL' 
	| 'MONITORING'
	| 'RETENTION';

ora_lob	: 'LOB' '(' column_name ')' 'STORE' 'AS' '(' ora_lob_config* ')';
ora_lob_config
	: ('TABLESPACE'^ name)
	| ('ENABLE' 'STORAGE' 'IN' 'ROW')
	| ('CHUNK'^ INT)
	| ('PCTVERSION'^ INT)
	| ora_flag
	| ora_lob_index
	| ora_storage_spec
	;
ora_lob_index 
	: 'INDEX' '(' ora_configs ')';
	
alter_table	: 'ALTER' 'TABLE' table_name table_mutation -> ^(ALTER_TABLE table_name table_mutation);
table_mutation
	: drop_pk
	| add_constraints;
	
drop_pk		: 'DROP' 'PRIMARY' 'KEY' 'CASCADE'?;
add_constraints	: 'ADD' '(' constraint_spec (',' constraint_spec)* ')';
constraint_spec	: ('CONSTRAINT' name)? constraint_detail;
constraint_detail
	: primary_key_constraint
	| foreign_key_constraint
	| unique_constraint
	| check_constraint;
primary_key_constraint	: 'PRIMARY' 'KEY' '(' name_list ')' ('USING' 'INDEX' ora_configs)?;
foreign_key_constraint	: 'FOREIGN' 'KEY' '(' name_list ')' 'REFERENCES' table_name '(' + name_list + ')';
unique_constraint	: 'UNIQUE' '(' name_list ')' ('USING' 'INDEX' ora_configs)?;
check_constraint	: 'CHECK' expression; // ((trade_geo_region_from_bp_id IS NULL OR geo_scope_pol_id IS NULL) AND (trade_geo_region_to_bp_id IS NULL OR geo_scope_pod_id IS NULL))



drop_table	: 'DROP' 'TABLE' table_name ('CASCADE' 'CONSTRAINTS')? -> ^(DROP_TABLE table_name);



create_index	: 'CREATE' 'UNIQUE'? 'INDEX' index_name 'ON' table_name '(' name_list ')' ('INDEXTYPE' 'IS' qualified_name)? ora_configs -> ^(CREATE_INDEX index_name);
index_name	: qualified_name -> ^(INDEX_NAME qualified_name);



drop_index	: 'DROP' 'INDEX' index_name;


create_sequence	: 'CREATE' 'SEQUENCE' name -> ^(CREATE_SEQUENCE name);
drop_sequence	: 'DROP' 'SEQUENCE' name -> ^(DROP_SEQUENCE name);


comment		: 'COMMENT'! 'ON'! (table_comment_details | column_comment_details);
table_comment_details
		: 'TABLE' table_name 'IS' STRING -> ^(COMMENT_TABLE table_name STRING);
column_comment_details
		: 'COLUMN' column_name 'IS' STRING -> ^(COMMENT_COLUMN column_name STRING);
column_name	: qualified_name -> ^(COLUMN_NAME qualified_name);


expression		: and_expression ('OR'^ and_expression )*;
and_expression		: exclusive_or_expression ('AND'^ exclusive_or_expression)*;
exclusive_or_expression	: equality_expression ('XOR'^ equality_expression)?;
equality_expression	: null_comparison (('=' | '!=' | '<>')^ null_comparison)?;
null_comparison		: relational_expression ('IS'^ 'NOT'? 'NULL'!)?;
relational_expression	: like_expression (('<=' | '>=' | '<' | '>')^ like_expression)?;
like_expression		: in_expression ('NOT'? 'LIKE'^ unary_expression)?;
in_expression		: between_expression ('NOT'? 'IN'^ '('! value_list ')'!)?;
between_expression	: additive_expression ('BETWEEN'^ additive_expression 'AND'! additive_expression)?;
additive_expression	: multiplicative_expression (('+' | '-' | '||')^ multiplicative_expression)*;
multiplicative_expression: unary_expression (('*' | '/' | '%')^ unary_expression)*;
unary_expression 
    :   '-'^ primary
    |   'NOT'^ primary
    |   primary
    ;

primary 
    :   '('! expression ')'!
    |   IDENTIFIER arguments -> ^(INVOCATION IDENTIFIER arguments)
    |   value
    ;

arguments	: '(' (expression (',' expression)*)? ')' -> ^(ARGUMENTS expression*);
expression_list	: expression (','! expression)*;
qualified_name	: name ('.'! name)*;
value_list	: value (',' value)* -> ^(VALUE_LIST value*);
value		: name | literal;
name_list	: name (',' name)* -> ^(NAME_LIST name*);
name		: IDENTIFIER | ora_key | keyword | QUOTED_NAME;

literal		: INT | STRING | 'NULL';	

keyword	: 'CREATE' 
	| 'DROP' 
	| 'ALTER' 
	| 'TABLE' 
	| 'INDEX' 
	| 'SEQUENCE' 
	| 'TRIGGER' 
	| 'UNIQUE' 
	| 'BY' 
	| 'ON' 
	| 'IS' 
	| 'COLUMN' 
	| PRIMARY
	| 'KEY'
	| DEFAULT
	| COMMENT;


fragment CREATE_TABLE:;
fragment DROP_TABLE:;
fragment ALTER_TABLE:;
fragment TABLE_NAME:;
fragment COLUMN_NAME:;
fragment SIZE:;
fragment CREATE_SEQUENCE:;
fragment DROP_SEQUENCE:;
fragment NAME_LIST:;
fragment CREATE_INDEX:;
fragment INDEX_NAME:;
fragment TABLE_DETAILS:;
fragment COLUMN_SPEC:;
fragment ORA_CONFIGS:;
fragment COMMENT_TABLE:;
fragment COMMENT_COLUMN:;
fragment INVOCATION:;
fragment ARGUMENTS:;
fragment VALUE_LIST:;

OR	: 'OR';
AND	: 'AND';
XOR	: 'XOR';
EQ	: '=';
BANGEQ	: '!=';
LTGT	: '<>';
GT	: '>';
GE	: '>=';
LT	: '<';
LE	: '<=';
LIKE	: 'LIKE';
IS	: 'IS';
NOT	: 'NOT';
NULL 	: 'NULL';
IN	: 'IN';
BETWEEN : 'BETWEEN';
PLUS	: '+';
SUB	: '-';
STAR	: '*';
SLASH	: '/';
PERCENT	: '%';
BARBAR	: '||';

ROWID 		: 'ROWID';
NUMBER 		: 'NUMBER';
VARCHAR2	: 'VARCHAR2';
NVARCHAR2	: 'NVARCHAR2';
BYTE 		: 'BYTE';
CHAR 		: 'CHAR';
DATE 		: 'DATE';
TIMESTAMP 	: 'TIMESTAMP';

BEFORE 		: 'BEFORE';
PRIMARY 	: 'PRIMARY';
DEFAULT		: 'DEFAULT';

IDENTIFIER  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'$')*;

INT :	'0'..'9'+;

COMMENT
    :   '--' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING	:  '\'' ( ~('\\'|'\'') )* '\'';
QUOTED_NAME	:  '"' ( ~('\\'|'\"') )* '"';
