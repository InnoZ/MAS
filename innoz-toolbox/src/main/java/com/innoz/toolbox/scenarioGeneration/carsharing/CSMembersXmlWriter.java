package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

public class CSMembersXmlWriter extends MatsimXmlWriter {
	
	private final static Logger log = Logger.getLogger(ObjectAttributesXmlWriter.class);
	
	/*package*/ final static String TAG_OBJECT_ATTRIBUTES = "memberships";
	/*package*/ final static String TAG_OBJECT = "person";
	/*package*/ final static String TAG_ATTRIBUTE = "company";
	/*package*/ final static String CONTENT_ATTRIBUTE = "carsharing";
	/*package*/ final static String ATTR_OBJECTID = "id";
	/*package*/ final static String ATTR_ATTRIBUTENAME = "name";
//	/*package*/ final static String ATTR_ATTRIBUTECLASS = "class";
	
	private final MembershipContainer container;

	public CSMembersXmlWriter(final MembershipContainer container) {
		this.container = container;
	}

	public void writeFile(final String filename) throws UncheckedIOException {
		openFile(filename);
		writeXmlHead();
		writeDoctype(TAG_OBJECT_ATTRIBUTES, "CSMembership.dtd");
		writeStartTag(TAG_OBJECT_ATTRIBUTES, null);
		List<Tuple<String, String>> xmlAttributes = new LinkedList<Tuple<String, String>>();
		for (Entry<Id<Person>, PersonMembership> entry : this.container.getPerPersonMemberships().entrySet()) {
			String personId = entry.getKey().toString();
			xmlAttributes.add(super.createTuple(ATTR_OBJECTID, personId));
			writeStartTag(TAG_OBJECT, xmlAttributes);
			xmlAttributes.clear();
			
			Map<String, String> objAttributes = new TreeMap<String, String>();
			for (Entry<String, Set<String>> e : entry.getValue().getMembershipsPerCompany().entrySet()) {
				String company = e.getKey();
				xmlAttributes.add(super.createTuple(ATTR_OBJECTID, company));
				writeStartTag(TAG_ATTRIBUTE, xmlAttributes);
				xmlAttributes.clear();
				
				// write attributes
				for (String csType : e.getValue()) {
					xmlAttributes.add(super.createTuple(ATTR_ATTRIBUTENAME, csType));
					writeStartTag(CONTENT_ATTRIBUTE, xmlAttributes, true);
					xmlAttributes.clear();
				}
				writeEndTag(TAG_ATTRIBUTE);
			}
			writeEndTag(TAG_OBJECT);
		}
		writeEndTag(TAG_OBJECT_ATTRIBUTES);
		close();
		
	}
	
	

}