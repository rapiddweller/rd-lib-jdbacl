/*
 * (c) Copyright 2008 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

/**
 * Indicates an error in the execution of a SQL script.<br/><br/>
 * Created at 31.07.2008 19:37:20
 * @author Volker Bergmann
 * @since 0.4.5
 */
public class SQLScriptException extends Exception {

  private static final long serialVersionUID = -6190417735216916815L;

  private String uri;
  private final int lineNo;

  // constructors ----------------------------------------------------------------------------------------------------

  public SQLScriptException(Throwable cause, String uri, int lineNo) {
    super(cause);
    this.uri = uri;
    this.lineNo = lineNo;
  }

  public SQLScriptException(int lineNo) {
    super();
    this.lineNo = lineNo;
  }

  public SQLScriptException(Throwable cause, int lineNo) {
    super(cause);
    this.lineNo = lineNo;
  }

  // properties ------------------------------------------------------------------------------------------------------

  public SQLScriptException withUri(String uri) {
    this.uri = uri;
    return this;
  }

  public int getLineNo() {
    return lineNo;
  }

  @Override
  public String getMessage() {
    return "Error in execution of script " + (uri != null ? uri + ' ' : "") + "line " + lineNo + ": "
        + (getCause() != null ? getCause().getMessage() : "");
  }

}
