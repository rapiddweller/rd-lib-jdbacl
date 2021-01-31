/*
 * (c) Copyright 2010-2014 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.IOUtil;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;

import java.io.IOException;

/**
 * Helper class which provides the content of a file as {@link ANTLRInputStream} 
 * while transforming token characters to upper case.<br/><br/>
 * Created: 10.08.2010 15:44:57
 * @since 0.1
 * @author Volker Bergmann
 */
public class ANTLRNoCaseFileStream extends ANTLRFileStream implements TextHolder {
	
	String fileName;
	
    public ANTLRNoCaseFileStream(String fileName) throws IOException {
        this(fileName, null);
    }

    public ANTLRNoCaseFileStream(String fileName, String encoding) throws IOException {
        super(fileName, encoding);
        this.fileName = fileName;
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
	public String getText() {
    	try {
			return IOUtil.getContentOfURI(fileName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
}
