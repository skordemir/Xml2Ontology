package com.anelarge.xml.ontology;

import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import com.anelarge.ontology.utils.ObjectProperty;
import com.anelarge.ontology.utils.OntologyUtil;
import com.anelarge.xml.ontology.XmlOntologyMapping.ClassProperty;
import com.anelarge.xml.ontology.XmlOntologyMapping.DataProperty;

/** @author sinan.kordemir */
public class XmlToOntologyUtils
{
	private OntologyUtil onto;

	public XmlToOntologyUtils(OntologyUtil onto)
	{
		this.onto = onto;
	}

	public String getXpathDomainDiff(String xpath, String domain)
	{
		if (xpath != null && domain != null)
		{
			int domainLength = domain.length();
			if (xpath.length() > domainLength)
			{
				return xpath.substring(0, xpath.length() - domainLength);
			}
		}
		return xpath;
	}

	public String createXpath(Stack<String> depth)
	{
		Enumeration<String> elements = depth.elements();
		String              xPath    = "";
		while (elements.hasMoreElements())
		{

			String nextElement = elements.nextElement();
			xPath += "/" + nextElement;
		}
		return xPath;
	}

	public String createXpathForAttribute(String attributeName, Stack<String> depth)
	{
		Enumeration<String> elements = depth.elements();
		String              xPath    = "";
		while (elements.hasMoreElements())
		{

			String nextElement = elements.nextElement();
			xPath += "/" + nextElement;
		}
		xPath += "/@" + attributeName;
		return xPath;
	}

	public DataProperty getDataMappingProperty(String pathName)
	{
		List<DataProperty> dataPropertymappings = XmlOntologyMapping.mappings.dataPropertyMappings;
		for (DataProperty dataProperty : dataPropertymappings)
		{
			if (dataProperty.xpath.equals(pathName))
			{
				return dataProperty;
			}
		}
		return null;
	}

	public ClassProperty getMappingClass(String pathName)
	{
		List<ClassProperty> classMappings = XmlOntologyMapping.mappings.classMappings;
		for (ClassProperty classProperty : classMappings)
		{
			if (classProperty.xpath.equals(pathName))
			{
				return classProperty;
			}
		}
		return null;
	}


	// this function gets the range property and returns the Object property

	public ObjectProperty getObjectProperty(String range, String domain)
	{
		List<ObjectProperty> objectProperties = onto.getObjectProperties();
		for (ObjectProperty objectProperty : objectProperties)
		{
			List<String> ranges  = objectProperty.getRanges();
			List<String> domains = objectProperty.getDomains();
			for (String string : ranges)
			{
				if (range.equals(string))
				{
					if (domain != null)
					{
						for (String string2 : domains)
						{
							if (string2.equals(domain))
							{
								return objectProperty;
							}
						}
					}
					else
					{
						return objectProperty;
					}
				}
			}

		}
		return null;
	}

	public boolean isClassMappedAsMappingClass(String className)
	{
		List<ClassProperty> classMappings = XmlOntologyMapping.mappings.classMappings;
		for (ClassProperty classProperty : classMappings)
		{
			if (classProperty.ontology.equals(className))
			{
				return true;
			}
		}
		return false;
	}

}
