/**
 * Copyright (c) 2013 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2013 [name of copyright owner]
 */
package com.evolveum.midpoint.prism;

import static com.evolveum.midpoint.prism.PrismInternalTestUtil.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.foo.AssignmentType;
import com.evolveum.midpoint.prism.foo.UserType;
import com.evolveum.midpoint.prism.path.IdItemPathSegment;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.path.ItemPathSegment;
import com.evolveum.midpoint.prism.path.NameItemPathSegment;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.util.PrismAsserts;
import com.evolveum.midpoint.prism.util.PrismTestUtil;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.PrettyPrinter;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author semancik
 *
 */
public class TestPath {
	
	private static final String NS = "http://example.com/";

	@BeforeSuite
	public void setupDebug() throws SchemaException, SAXException, IOException {
		PrettyPrinter.setDefaultNamespacePrefix(DEFAULT_NAMESPACE_PREFIX);
		PrismTestUtil.resetPrismContext(new PrismInternalTestUtil());
	}
	
	@Test
    public void testPathNormalize() throws Exception {
		System.out.println("\n\n===[ testPathNormalize ]===\n");
		
		// GIVEN
		ItemPath path1 = new ItemPath(new QName(NS, "foo"), new QName(NS, "bar"));
		ItemPath path2 = new ItemPath(new NameItemPathSegment(new QName(NS, "foo")), new IdItemPathSegment("123"),
									  new NameItemPathSegment(new QName(NS, "bar")));
		ItemPath path3 = new ItemPath(new NameItemPathSegment(new QName(NS, "foo")), new IdItemPathSegment("123"),
				  					  new NameItemPathSegment(new QName(NS, "bar")), new IdItemPathSegment("333"));
		ItemPath path4 = new ItemPath(new QName(NS, "x"));
		ItemPath path5 = new ItemPath(new NameItemPathSegment(new QName(NS, "foo")), new IdItemPathSegment("123"));
		ItemPath pathE = new ItemPath();
		
		// WHEN
		ItemPath normalized1 = path1.normalize();
		ItemPath normalized2 = path2.normalize();
		ItemPath normalized3 = path3.normalize();
		ItemPath normalized4 = path4.normalize();
		ItemPath normalized5 = path5.normalize();
		ItemPath normalizedE = pathE.normalize();
		
		// THEN
		System.out.println("Normalized path 1:" + normalized1);
		System.out.println("Normalized path 2:" + normalized2);
		System.out.println("Normalized path 3:" + normalized3);
		System.out.println("Normalized path 4:" + normalized4);
		System.out.println("Normalized path 5:" + normalized5);
		System.out.println("Normalized path E:" + normalizedE);

		assertNormalizedPath(normalized1, "foo", null, "bar", null);
		assertNormalizedPath(normalized2, "foo", "123", "bar", null);
		assertNormalizedPath(normalized3, "foo", "123", "bar", "333");
		assertNormalizedPath(normalized4, "x", null);
		assertNormalizedPath(normalized5, "foo", "123");
		assert normalizedE.isEmpty() : "normalizedE is not empty";
	}
	
	@Test
    public void testPathCompare() throws Exception {
		System.out.println("\n\n===[ testPathCompare ]===\n");
		
		// GIVEN
		ItemPath pathFoo = new ItemPath(new QName(NS, "foo"));
		ItemPath pathFooNull = new ItemPath(new NameItemPathSegment(new QName(NS, "foo")), new IdItemPathSegment());
		ItemPath pathFoo123 = new ItemPath(new NameItemPathSegment(new QName(NS, "foo")), new IdItemPathSegment("123"));
		ItemPath pathFooBar = new ItemPath(new QName(NS, "foo"), new QName(NS, "bar"));
				
		// WHEN, THEN
		assert pathFoo.equivalent(pathFooNull);
		assert !pathFoo.equivalent(pathFoo123);
		assert pathFoo.isSubPath(pathFooBar);
		assert pathFooBar.isSuperPath(pathFoo);
		
	}

	private void assertNormalizedPath(ItemPath normalized, String... expected) {
		assertEquals("wrong path length",normalized.size(), expected.length);
		for(int i=0; i<normalized.size(); i+=2) {
			ItemPathSegment nameSegment = normalized.getSegments().get(i);
			assert nameSegment instanceof NameItemPathSegment : "Expected name segment but it was "+nameSegment.getClass();
			QName name = ((NameItemPathSegment)nameSegment).getName();
			assert name != null : "name is null";
			assert name.getNamespaceURI().equals(NS) : "wrong namespace: "+name.getNamespaceURI();
			assert name.getLocalPart().equals(expected[i]) : "wrong local name, expected "+expected[i]+", was "+name.getLocalPart();
			
			ItemPathSegment idSegment = normalized.getSegments().get(i+1);
			assert idSegment instanceof IdItemPathSegment : "Expected is segment but it was "+nameSegment.getClass();
			String id = ((IdItemPathSegment)idSegment).getId();
			assertId(id, expected[i+1]);
		}
	}

	private void assertId(String actual, String expected) {
		if (expected == null && actual == null) {
			return;
		}
		assert expected != null : "Expected null id but it was "+actual;
		assertEquals("wrong id", expected, actual);
	}



}