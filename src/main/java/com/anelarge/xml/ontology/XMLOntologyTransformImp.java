package com.anelarge.xml.ontology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.anelarge.ontology.IOntologyTransform;
import com.anelarge.ontology.utils.ObjectProperty;
import com.anelarge.ontology.utils.OntologyUtil;
import com.anelarge.utils.formats.XmlDocument;
import com.anelarge.utils.general.StringUtils;
import com.anelarge.xml.ontology.XmlOntologyMapping.ClassProperty;
import com.anelarge.xml.ontology.XmlOntologyMapping.DataProperty;

/**
 * @author sinan.kordemir
 * @deprecated Use @{link XMLOntologyTransformWithXpathQueryImp}
 */
@Deprecated
public class XMLOntologyTransformImp implements IOntologyTransform
{

	        HashMap<String, Integer> ontoElementCounter = new HashMap<>();

	private Stack<String>            depth              = new Stack<>();

	private OntologyUtil             onto;

	private XmlToOntologyUtils       helper;

	private String                   xmlDocument;

	private String                   ontologyFile;

	private String                   mappingFile;

	private Document                 doc;

	public XMLOntologyTransformImp(String documentFile, String mappingFile, String ontology)
			throws SAXException, IOException, ParserConfigurationException, JAXBException, OWLException
	{
		this.xmlDocument  = documentFile;
		this.ontologyFile = ontology;
		this.mappingFile  = mappingFile;
		init();
	}

	/**
	 * This method checks all the data properties in the XML report and creates DATA property individuals according to
	 * mappings
	 *
	 * @param parent    node at the XML Report
	 * @param className if the pranet node mapped to an Ontology_Class
	 */
	private void checkAttributes(Node parent, String className)
	{
		// match to data properties
		HashMap<String, OWLNamedIndividual> indBag                   = new HashMap<>();
		String                              createXpathForAttribute1 = this.helper
				.createXpathForAttribute(parent.getNodeName(), this.depth);
		DataProperty                        dataMappingProperty1     = this.helper
				.getDataMappingProperty(createXpathForAttribute1);
		/**
		 * If the data property is mapped at the mapping file
		 */
		if ((dataMappingProperty1 != null) && (parent != null))
		{
			String value = parent.getNodeValue();
			if (value == null)
			{
				value = parent.getTextContent();
			}
			String  domainClass = dataMappingProperty1.range;
			Integer integer2    = this.ontoElementCounter.get(domainClass);
			if (integer2 != null)
			{
				OWLNamedIndividual owlNamedIndividual = null;
				owlNamedIndividual = this.onto.getIndividual(domainClass + integer2);
				if (owlNamedIndividual != null)
				{
					this.onto.addDataProperty(dataMappingProperty1.ontology, value, owlNamedIndividual);
				}
			}
		}
		/**
		 * If parent node has attributes check if there are data property
		 * mappings for this attributes
		 */
		else if ((parent != null) && (parent.getAttributes() != null))
		{
			for (int i = 0; i < parent.getAttributes().getLength(); i++)
			{
				Node item = parent.getAttributes().item(i);
				/**
				 * If the node is like <value>value</value> get it as
				 * node.getTextContent or like <value = "value"/> get it as
				 * item.getNodeValue
				 */
				String value = item.getNodeValue();
				if (value == null)
				{
					value = parent.getTextContent();
				}

				String       createXpathForAttribute = this.helper
						.createXpathForAttribute(item.getNodeName(), this.depth);
				DataProperty dataMappingProperty     = this.helper.getDataMappingProperty(createXpathForAttribute);
				if (dataMappingProperty != null)
				{
					String domainClass = dataMappingProperty.range;
					if ((domainClass != null) && domainClass.equals(className))
					{
						Integer integer2 = this.ontoElementCounter.get(domainClass);
						if (integer2 != null)
						{
							OWLNamedIndividual owlNamedIndividual = null;
							owlNamedIndividual = this.onto.getIndividual(domainClass + integer2);
							this.onto.addDataProperty(dataMappingProperty.ontology, value, owlNamedIndividual);
						}
					}
					else
					{
						/**
						 * if domain class is mapped at the Class Mappings dont
						 * add it again so create an individual for domain class
						 * and after that create a data property individual add
						 * data property to domain
						 */
						OWLNamedIndividual owlNamedIndividual2       = indBag.get(domainClass);
						boolean            classMappedAsMappingClass = this.helper
								.isClassMappedAsMappingClass(domainClass);
						if (classMappedAsMappingClass)
						{
							Integer integer = this.ontoElementCounter.get(domainClass);
							owlNamedIndividual2 = this.onto.getIndividual(domainClass + integer);
						}

						if (owlNamedIndividual2 == null)
						{
							Integer integer2 = this.ontoElementCounter.get(domainClass);
							if (integer2 == null)
							{
								integer2 = 0;
							}
							integer2++;
							owlNamedIndividual2 = this.onto.createInvidual(domainClass, domainClass + integer2);
							this.ontoElementCounter.put(domainClass, integer2);
							indBag.put(domainClass, owlNamedIndividual2);

							ObjectProperty objectProperty = this.helper.getObjectProperty(domainClass, null);
							if (objectProperty != null)
							{
								List<String> domains = objectProperty.getDomains();
								for (String string : domains)
								{
									Integer integer = this.ontoElementCounter.get(string);
									if (integer != null)
									{
										OWLNamedIndividual individual = this.onto.getIndividual(string + integer);
										this.onto.addObjectProperty(objectProperty.getName(), owlNamedIndividual2,
										                            individual);
									}
								}
							}
						}
						this.onto.addDataProperty(dataMappingProperty.ontology, value, owlNamedIndividual2);

					}

				}

			}
		}

	}

	@Override
	public OntologyUtil getOntologyUtil()
	{
		// TODO Auto-generated method stub
		return this.onto;
	}

	@Override
	/**
	 * get first document element
	 */
	public void transform()
			throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException
	{
		Element documentElement = this.doc.getDocumentElement();
		this.depth.push(documentElement.getNodeName());
		recurse(documentElement);
		this.depth.pop();
		if (true)
		{
			Random           r        = new Random();
			long             nextLong = r.nextLong();
			File             f        = new File(            nextLong + ".owl");
			FileOutputStream fs       = new FileOutputStream(f);
			this.onto.saveOntology(fs);
			System.out.println(f.getAbsolutePath());
		}
	}

	/**
	 * Create the ontology , read the ontology mappings and read the XML document
	 *
	 * @throws OWLOntologyCreationException
	 * @throws JAXBException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void init()
			throws OWLOntologyCreationException, JAXBException, ParserConfigurationException, SAXException, IOException
	{
		// load ontology

		InputStream createInputStream = StringUtils.createInputStream(this.ontologyFile);
		this.onto = new OntologyUtil(createInputStream);
		this.helper = new XmlToOntologyUtils(this.onto);
		// load mapping file
		InputSource createInputSource = StringUtils.createInputSource(this.mappingFile);
		XmlOntologyMapping.loadXsdOntologyMappings(createInputSource);
		// load report file
		this.doc = XmlDocument.fromString(this.xmlDocument);
	}

	/**
	 * This method will search all the nodes in XML report and creates individuals according to ontology mappings
	 *
	 * @param parent
	 */
	private void recurse(Node parent)
	{

		// check mathcing if ontology instance is exist create it
		String        createXpath  = this.helper.createXpath(this.depth);
		ClassProperty mappingClass = this.helper.getMappingClass(createXpath);
		String        className    = null;
		if (mappingClass != null)
		{
			className = mappingClass.ontology;
			// create Individiual
			Integer integer = this.ontoElementCounter.get(mappingClass.ontology);
			if (integer == null)
			{
				integer = 0;
			}
			integer++;
			this.ontoElementCounter.put(mappingClass.ontology, integer);
			OWLNamedIndividual createInvidual       = this.onto
					.createInvidual(mappingClass.ontology, mappingClass.ontology + integer);
			String             objectPropertyClass  = mappingClass.ontology;
			String             objectPropertyDomain = mappingClass.domain;
			ObjectProperty     property             = this.helper
					.getObjectProperty(objectPropertyClass, objectPropertyDomain);
			if (property != null)
			{
				// create Object property
				List<String> domains = new ArrayList<>();
				if ((objectPropertyDomain != null) && !objectPropertyDomain.isEmpty())
				{
					domains.add(objectPropertyDomain);
				}
				else
				{
					domains = property.getDomains();
				}
				for (String domain : domains)
				{
					OWLNamedIndividual individual = null;
					Integer            integer2   = this.ontoElementCounter.get(domain);
					if (integer2 == null)
					{
						integer2   = 1;
						individual = this.onto.createInvidual(domain, domain + integer2);
						this.ontoElementCounter.put(domain, integer2);
					}

					else
					{
						individual = this.onto.getIndividual(domain + integer2);
					}
					this.onto.addObjectProperty(property.getName(), createInvidual, individual);
				}

			}

		}
		// check if has object property ?
		checkAttributes(parent, className);
		NodeList childs = parent.getChildNodes();
		// create new ontology elemenet
		if (childs.getLength() > 0)
		{                           // Complex Type
			for (int i = 0; i < childs.getLength(); i++)
			{
				Node e = childs.item(i);
				this.depth.push(e.getNodeName());
				recurse(e);
				this.depth.pop();
			}
		}
		else
		{

		}
	}

}
