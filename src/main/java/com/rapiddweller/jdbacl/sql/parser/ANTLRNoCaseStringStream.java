/*
 * (c) Copyright 2011-2014 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.sql.parser;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;

/**
 * Helper class which provides the content of a string as {@link ANTLRInputStream} 
 * while transforming token characters to upper case.<br/><br/>
 * Created: 07.06.2011 20:04:29
 * @since 0.1
 * @author Volker Bergmann
 */
public class ANTLRNoCaseStringStream extends ANTLRStringStream implements TextHolder {

	private String text;
	
	public ANTLRNoCaseStringStream(String text) {
		super(text);
		this.text = text;
	}

	@Override
    public int LA(int i) {
        if (i == 0)
            return 0; // undefined
        if (i < 0)
            i++; // e.g., translate LA(-1) to use offset 0
        if ((p + i - 1) >= n)
            return CharStream.EOF;
        return Character.toUpperCase(data[p + i - 1]);
    }

	@Override
	public String toString() {
		return new String(data);
	}
	
	@Override
	public String getText() {
		return text;
	}
	
}
