package com.anelarge.xml.ontology;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.xml.sax.InputSource;

/**
 * @author sinan.kordemir
 *
 *         This class reads Ontology XML mappings from file and convert them to
 *         Java Objects Here is an example XML mappings : <xmltoOntology>
 *         <classPropertyMapping> <mapping
 *         xpath="/summary/network/routers/router/devices/device/subnets/subnet"
 *         ontology="Subnet" domain="Device"/> </classPropertyMapping>
 *         <dataPropertyMapping> <mapping xpath=
 *         "/summary/network/routers/router/devices/device/subnets/subnet/startIp/@startIp"
 *         ontology="startIp" range="Subnet"/> </dataPropertyMapping>
 *         </xmltoOntology>
 *
 *         In here all the classes in the Ontology must be define at
 *         <classPropertyMapping> in XML , The XPATH of the XML must be define
 *         like xpath= "XPATH" and the ontology class of this element must be
 *         define as ontology="OntologyClass" and the domian of the ontology
 *         class will be define as domain="DOMAIN CLASS AT ONTOLOGY" All of the
 *         data properties must be define under the <dataPropertyMapping> and
 *         the XPATH of the dataproperty must be define
 *         "/summary/network/routers/router/devices/device/subnets/subnet/startIp/@startIp"
 *         and the ontology data property of the given element will be define
 *         like ontology="DATA PROPERTY NAME IN ONTOLOGY" the domain class of
 *         the data property will be define
 */

public class XPATHOntologyMapping
{

	public static class DataProperty
	{
		@XmlAttribute
		public String	xpath;

		@XmlAttribute
		public String	ontology;

		@XmlAttribute
		public String	domain;
	}


	public static class Mapping
	{
		@XmlAttribute
		public String	xpath;

		@XmlAttribute
		public String	ontology;

		@XmlAttribute
		public String	domain;

		@XmlElementWrapper(name = "dataProperties")
		@XmlElement(name = "dataProperty")
		public List<DataProperty>	dataProperties;

		@XmlElementWrapper(name = "mappings")
		@XmlElement(name = "mapping")
		public List<Mapping>	mappings;
	}

	@XmlRootElement
	public static class xmltoOntology
	{

		@XmlElementWrapper(name = "mappings")
		@XmlElement(name = "mapping")
		public List<Mapping>	mappings;
	}

	public static xmltoOntology	mappings;

	public static xmltoOntology loadXsdOntologyMappings(InputSource source) throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(xmltoOntology.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		mappings = (xmltoOntology) unmarshaller.unmarshal(source);
		return mappings;
	}

	public static xmltoOntology loadXsdOntologyMappings(String mappingFileDirectory) throws JAXBException
	{
		JAXBContext jaxbContext = JAXBContext.newInstance(xmltoOntology.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		mappings = (xmltoOntology) unmarshaller.unmarshal(new File(mappingFileDirectory));
		return mappings;
	}

	public XPATHOntologyMapping()
	{
	}
}
