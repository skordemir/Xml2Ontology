package com.anelarge.xml.ontology;

import static com.anelarge.utils.general.StringUtils.createInputSource;
import static com.anelarge.utils.general.StringUtils.createInputStream;
import static com.anelarge.utils.general.StringUtils.notEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.anelarge.ontology.IOntologyTransform;
import com.anelarge.ontology.utils.ObjectProperty;
import com.anelarge.ontology.utils.OntologyUtil;
import com.anelarge.utils.formats.XmlDocument;
import com.anelarge.utils.general.StringUtils;
import com.anelarge.utils.logging.loglet.Reportlet;
import com.anelarge.xml.ontology.XPATHOntologyMapping.DataProperty;
import com.anelarge.xml.ontology.XPATHOntologyMapping.Mapping;

public class XMLOntologyTransformWithXpathQueryImp implements IOntologyTransform
{
	private static final Logger			logger							= LoggerFactory
																				.getLogger(XMLOntologyTransformWithXpathQueryImp.class);
	
	private final String				xmlDocument;
	
	private final String				ontologyFile;
	
	private final String				mappingFile;
	
	HashMap<String, Integer>			ontoElementCounter				= new HashMap<>();
	
	HashMap<String, OWLNamedIndividual>	lastAddedIndividualsAtDomain	= new HashMap<>();
	
	private OntologyUtil				onto;
	
	private XmlToOntologyUtils			helper;
	
	private Document					doc;
	
	public XMLOntologyTransformWithXpathQueryImp(String documentFile, String mappingFile, String ontology)
			throws SAXException, IOException, ParserConfigurationException, JAXBException, OWLOntologyCreationException
	{
		Reportlet.report();
		this.xmlDocument = documentFile;
		this.ontologyFile = ontology;
		this.mappingFile = mappingFile;
		init();
	}
	
	private static void OutputJaxpImplementationInfo()
	{
		System.out.println(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance()
				.getClass()));
		System.out.println(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
		System.out
				.println(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
		System.out.println(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
	}
	
	private static String getJaxpImplementationInfo(String componentName, Class componentClass)
	{
		CodeSource source = componentClass.getProtectionDomain().getCodeSource();
		return MessageFormat.format("{0} implementation: {1} loaded from: {2}", componentName,
				componentClass.getName(), source == null ? "Java Runtime" : source.getLocation());
	}
	
	private static XPath createXpath() throws XPathFactoryConfigurationException
	{
		setDefaultXpathFactoryImpl();
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		Class<? extends XPath> xpathClass = xpath.getClass();
		CodeSource xpathClassSource = xpathClass.getProtectionDomain().getCodeSource();
		
		logger.info("Using XPath implementation (name)     = {}", xpathClass.getName());
		logger.info("Using XPath implementation (source)   = {}", xpathClassSource);
		
		return xpath;
	}
	
	private static String getDefaultXpathImplKey()
	{
		return String.format("%s:%s", XPathFactory.DEFAULT_PROPERTY_NAME, XPathFactory.DEFAULT_OBJECT_MODEL_URI);
	}
	
	private static void setDefaultXpathFactoryImpl()
	{
		String propName = getDefaultXpathImplKey();
		String propValue = "org.apache.xpath.jaxp.XPathFactoryImpl";
		
		logger.info("(before) Default XPathFactory implementation = {}", System.getProperty(propName));
		System.setProperty(propName, propValue);
		logger.info("(after)  Default XPathFactory implementation = {}", System.getProperty(propName));
	}
	
	@Override
	public OntologyUtil getOntologyUtil()
	{
		return this.onto;
	}
	
	@Override
	public void transform() throws XPathFactoryConfigurationException, XPathExpressionException
	{
		Reportlet.report();
		List<Mapping> mappings2 = XPATHOntologyMapping.mappings.mappings;
		
		XPath xpath = createXpath();
		for (Mapping mapping : mappings2)
		{
			Reportlet.report(mapping);
			recurse(xpath, this.doc, mapping, null);
		}
	}
	
	/**
	 * Create the ontology , read the ontology mappings and read the XML
	 * document
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws JAXBException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void init() throws OWLOntologyCreationException, JAXBException, ParserConfigurationException, SAXException,
		IOException
	{
		Reportlet.report();
		// load ontology
		
		if (StringUtils.isEmpty(this.ontologyFile))
			throw new NullPointerException("Ontology File is empty");
		
		if (StringUtils.isEmpty(this.xmlDocument))
			throw new NullPointerException("Document File is empty");
		
		if (StringUtils.isEmpty(this.mappingFile))
			throw new NullPointerException("MappingFile File is empty");
		
		try
		{
			InputStream ontologyInputStream = createInputStream(this.ontologyFile);
			this.onto = new OntologyUtil(ontologyInputStream);
			this.helper = new XmlToOntologyUtils(this.onto);
		}
		catch (Exception e)
		{
			throw new OWLOntologyCreationException("Couldnt create ontology from given ontology content");
		}
		
		// load mapping file
		try
		{
			InputSource mappingInputSource = createInputSource(this.mappingFile);
			XPATHOntologyMapping.loadXsdOntologyMappings(mappingInputSource);
		}
		catch (Exception e)
		{
			throw new JAXBException("Coudnt load mappings from given mapping content");
		}
		
		// load report file
		
		try
		{
			this.doc = XmlDocument.fromString(this.xmlDocument);
		}
		catch (Exception e)
		{
			throw new SAXException("Coulndt create XML Document from given content");
		}
	}
	
	/**
	 * @param xpath
	 * @param item
	 * @param mapping
	 * @param parentInd
	 * 
	 * @throws XPathExpressionException
	 */
	public void recurse(XPath xpath, Node item, Mapping mapping, OWLNamedIndividual parentInd)
		throws XPathExpressionException
	{
		String xpathStr = mapping.xpath;
		Node foundNode = null;
		
		NodeList foundNodes = (NodeList) xpath.evaluate(xpathStr, item, XPathConstants.NODESET);
		
		if (foundNodes != null)
		{
			int foundNodesCount = foundNodes.getLength();
			for (int i = 0; i < foundNodesCount; i++)
			{
				foundNode = foundNodes.item(i);
				if (foundNode != null)
				{
					// create ontologyIndividual
					String ontology = mapping.ontology;
					OWLNamedIndividual individual = put(ontology);
					String parentDomain = this.onto.getIndClassName(parentInd);
					ObjectProperty property = this.helper.getObjectProperty(ontology, parentDomain);
					
					if ((parentInd != null) && (property != null))
					{
						String name = property.getName();
						this.onto.addObjectProperty(name, individual, parentInd);
					}
					
					if (property == null)
					{
						OWLNamedIndividual owlNamedIndividual = this.lastAddedIndividualsAtDomain.get(mapping.domain);
						if (owlNamedIndividual != null)
						{
							String indClassName = this.onto.getIndClassName(owlNamedIndividual);
							property = this.helper.getObjectProperty(ontology, indClassName);
							if (property != null)
							{
								String name = property.getName();
								this.onto.addObjectProperty(name, individual, owlNamedIndividual);
							}
						}
						else
						{
							property = this.helper.getObjectProperty(ontology, null);
							if (property != null)
							{
								String name = property.getName();
								List<String> domains = property.getDomains();
								String domainName = null;
								for (String domain : domains)
								{
									domainName = domain;
									break;
								}
								
								owlNamedIndividual = this.lastAddedIndividualsAtDomain.get(domainName);
								if (owlNamedIndividual != null)
									this.onto.addObjectProperty(name, individual, owlNamedIndividual);
							}
						}
					}
					
					// add data properties
					List<DataProperty> dataProperties = mapping.dataProperties;
					HashMap<String, OWLNamedIndividual> dataPropertyIndividuals = new HashMap<>();
					if (dataProperties != null)
					{
						for (DataProperty dataProperty : dataProperties)
						{
							String propertyValue = xpath.evaluate(dataProperty.xpath, foundNode);
							String ontologyClass = notEmpty(dataProperty.domain, ontology);
							boolean belongsToParentClass = ontologyClass.equals(ontology);
							
							OWLNamedIndividual individualToAdd = individual;
							if (!belongsToParentClass)
							{
								individualToAdd = getOwlNamedIndividual(ontology, dataPropertyIndividuals,
										dataProperty.domain, ontologyClass);
							}
							
							this.onto.addDataProperty(dataProperty.ontology, propertyValue, individualToAdd);
						}
					}
					
					addObjectProperties(xpath, mapping, foundNode, individual);
					if (foundNode != item)
					{
						recurse(xpath, foundNode, mapping, individual);
					}
				}
			}
		}
		
		if ((foundNode != null) && !foundNode.equals(item) && (item != null))
		{
			Node parentNode = item.getParentNode();
			if (parentNode != null)
			{
				parentNode.removeChild(item);
			}
		}
	}
	
	private OWLNamedIndividual getOwlNamedIndividual(String ontology,
			HashMap<String, OWLNamedIndividual> dataPropertyIndividuals, String domainClass, String ontologyClass)
	{
		OWLNamedIndividual owlNamedIndividual = dataPropertyIndividuals.get(ontologyClass);
		if (owlNamedIndividual == null)
		{
			ObjectProperty objectProperty2 = this.helper.getObjectProperty(domainClass, ontology);
			if (objectProperty2 != null)
			{
				
				owlNamedIndividual = put(domainClass);
				dataPropertyIndividuals.put(domainClass, owlNamedIndividual);
				
				OWLNamedIndividual foundInd = null;
				foundInd = findFirstNamedIndividual(objectProperty2);
				this.onto.addObjectProperty(objectProperty2.name, owlNamedIndividual, foundInd);
			}
			else
			{
				return findOwlIndividualByDomain(ontologyClass);
			}
		}
		return owlNamedIndividual;
	}
	
	private OWLNamedIndividual put(String className)
	{
		Integer count = this.ontoElementCounter.get(className);
		if (count == null)
		{
			count = 0;
		}
		count++;
		
		String invname = className + count;
		OWLNamedIndividual individual = this.onto.createInvidual(className, invname);
		this.lastAddedIndividualsAtDomain.put(className, individual);
		this.ontoElementCounter.put(className, count);
		return individual;
	}
	
	private void addObjectProperties(XPath xpath, Mapping mapping, Node foundNode, OWLNamedIndividual individual)
		throws XPathExpressionException
	{
		List<Mapping> mappings = mapping.mappings;
		if (mappings != null)
		{
			for (Mapping mapping2 : mappings)
			{
				recurse(xpath, foundNode, mapping2, individual);
			}
		}
	}
	
	private OWLNamedIndividual findOwlIndividualByDomain(String domain)
	{
		return this.lastAddedIndividualsAtDomain.get(domain);
	}
	
	private OWLNamedIndividual findFirstNamedIndividual(ObjectProperty objectProperty2)
	{
		try
		{
			List<String> domains = objectProperty2.getDomains();
			OWLNamedIndividual foundInd = null;
			for (String domain : domains)
			{
				foundInd = this.lastAddedIndividualsAtDomain.get(domain);
				if (foundInd != null)
				{
					break;
				}
			}
			return foundInd;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
