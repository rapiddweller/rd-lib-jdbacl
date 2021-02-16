/*
 * (c) Copyright 2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.identity;

/**
 * Represents an error induced by a faulty table identity definition.<br/><br/>
 * Created: 06.12.2010 23:02:43
 *
 * @author Volker Bergmann
 * @see IdentityModel
 * @since 0.4
 */
public class InvalidIdentityDefinitionError extends RuntimeException {

  private static final long serialVersionUID = 5760179913000903636L;

  /**
   * Instantiates a new Invalid identity definition error.
   */
  public InvalidIdentityDefinitionError() {
    super();
  }

  /**
   * Instantiates a new Invalid identity definition error.
   *
   * @param message the message
   * @param cause   the cause
   */
  public InvalidIdentityDefinitionError(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new Invalid identity definition error.
   *
   * @param message the message
   */
  public InvalidIdentityDefinitionError(String message) {
    super(message);
  }

  /**
   * Instantiates a new Invalid identity definition error.
   *
   * @param cause the cause
   */
  public InvalidIdentityDefinitionError(Throwable cause) {
    super(cause);
  }

}
