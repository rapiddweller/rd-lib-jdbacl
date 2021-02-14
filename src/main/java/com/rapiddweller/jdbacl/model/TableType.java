/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS AS IS
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

package com.rapiddweller.jdbacl.model;

/**
 * Enumeration of the table types defined in the JDBC spec.<br/><br/>
 * Created: 28.01.2012 07:44:07
 *
 * @author Volker Bergmann
 * @since 0.8.0
 */
public enum TableType {
  /**
   * Table table type.
   */
  TABLE,
  /**
   * View table type.
   */
  VIEW,
  /**
   * System table table type.
   */
  SYSTEM_TABLE,
  /**
   * Global temporary table type.
   */
  GLOBAL_TEMPORARY,
  /**
   * Local temporary table type.
   */
  LOCAL_TEMPORARY,
  /**
   * Alias table type.
   */
  ALIAS,
  /**
   * Synonym table type.
   */
  SYNONYM;

  /**
   * Descriptive name string.
   *
   * @return the string
   */
  public String descriptiveName() {
    return name().replace('_', ' ').toLowerCase();
  }

}
