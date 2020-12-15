/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

import java.util.Set;

import com.rapiddweller.commons.CollectionUtil;
import com.rapiddweller.commons.ConfigurationError;
import com.rapiddweller.commons.StringUtil;
import com.rapiddweller.commons.xml.XMLUtil;
import com.rapiddweller.formats.xml.AbstractXMLElementParser;
import com.rapiddweller.formats.xml.ParseContext;
import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.IdentityProvider;
import com.rapiddweller.jdbacl.identity.NaturalPkIdentity;
import com.rapiddweller.jdbacl.identity.NkPkQueryIdentity;
import com.rapiddweller.jdbacl.identity.SubNkPkQueryIdentity;
import com.rapiddweller.jdbacl.identity.UniqueKeyIdentity;
import org.w3c.dom.Element;

/**
 * Parses an &lt;identity&gt; element in a DB Sanity XML file.<br/><br/>
 * Created: 05.12.2010 14:39:48
 * @since 0.7.1
 * @author Volker Bergmann
 */
public class IdentityParser extends AbstractXMLElementParser<Object> {
	
	public static final String IDENTITY = "identity";
	public static final Set<String> REQUIRED_ATTRIBUTES = CollectionUtil.toSet("type", "table");
	public static final Set<String> OPTIONAL_ATTRIBUTES = CollectionUtil.toSet("nk-pk-query", "sub-nk-pk-query", "parents", "unique-key", "natural-pk", "columns");
	
	public IdentityParser() {
		super(IDENTITY, REQUIRED_ATTRIBUTES, OPTIONAL_ATTRIBUTES, Object.class);
	}

	@Override
	public IdentityModel doParse(Element element, Object[] parentPath, ParseContext<Object> context) {
		String type = getRequiredAttribute("type", element);
		String tableName = getRequiredAttribute("table", element);
		
		IdentityModel identity;
		IdentityProvider identityProvider = ((IdentityParseContext) context).getIdentityProvider();
		if ("nk-pk-query".equals(type))
			identity = parseNkPkQuery(element, tableName);
		else if ("sub-nk-pk-query".equals(type))
			identity = parseSubNkPkQuery(element, identityProvider, tableName);
		else if ("unique-key".equals(type))
			identity = parseUniqueKey(element, tableName);
		else if ("natural-pk".equals(type))
			identity = parseNaturalPk(element, tableName);
		else
			throw new ConfigurationError("Not a supported <identity> type: " + type);
		identityProvider.registerIdentity(identity, identity.getTableName());
		return identity;
	}

	public static String createCheckName(String tableName, String type) {
		return tableName + "-identity-" + type;
	}
	
	// private helpers -------------------------------------------------------------------------------------------------

	private static IdentityModel parseNkPkQuery(Element element, String tableName) {
	    String nkPkQuery = XMLUtil.getWholeText(element);
	    return new NkPkQueryIdentity(tableName, nkPkQuery);
    }

	private static IdentityModel parseNaturalPk(Element element, String tableName) {
		return new NaturalPkIdentity(tableName);
    }

	private IdentityModel parseUniqueKey(Element element, String tableName) {
		String[] columnNames = getRequiredAttribute("columns", element).split(",");
		columnNames = StringUtil.trimAll(columnNames);
		UniqueKeyIdentity identity = new UniqueKeyIdentity(tableName, columnNames);
	    return identity;
    }

	private IdentityModel parseSubNkPkQuery(Element element, IdentityProvider identityProvider, String tableName) {
		String[] parentTableNames = getRequiredAttribute("parents", element).split(",");
		SubNkPkQueryIdentity identity = new SubNkPkQueryIdentity(tableName, parentTableNames, identityProvider);
	    identity.setSubNkPkQuery(XMLUtil.getWholeText(element));
	    return identity;
    }

}
