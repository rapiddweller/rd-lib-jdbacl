/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.identity.xml;

import com.rapiddweller.formats.xml.ParseContext;
import com.rapiddweller.formats.xml.XMLElementParserFactory;
import com.rapiddweller.jdbacl.identity.IdentityProvider;

/**
 * {@link ParseContext} implementation for identity definition files.<br/><br/>
 * Created: 07.12.2011 15:45:13
 * @since 0.7.1
 * @author Volker Bergmann
 */
public class IdentityParseContext extends ParseContext<Object> {
	
	protected IdentityProvider identityProvider;

	public IdentityParseContext() {
		this(new IdentityProvider());
	}

	public IdentityParseContext(IdentityProvider identityProvider) {
		super(Object.class, new XMLElementParserFactory<Object>());
		this.identityProvider = identityProvider;
		createParsers();
	}

	private void createParsers() {
		addParser(new IdentityParser());
	}

	public IdentityProvider getIdentityProvider() {
		return identityProvider;
	}

}
