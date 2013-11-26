package com.anelarge.xml.ontology.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.anelarge.resource.bag.ResourceBag;
import com.anelarge.resource.xml.ontology.OntologyResourceUtil;
import com.anelarge.resource.xml.ontology.mapping.MappingResourceUtil;
import com.anelarge.resource.xml.ontology.report.ReportResourceUtil;
import com.anelarge.utils.formats.XmlDomUtils;
import com.anelarge.utils.io.FileUtils;
import com.anelarge.utils.io.ResourceUtils;
import com.anelarge.xml.ontology.XMLOntologyTransformWithXpathQueryImp;

public class XmlOntologyTransformTest
{
	private static final Logger	logger	= LoggerFactory.getLogger(XmlOntologyTransformTest.class);
	
	@Test
	public void testTransformDiscoveryToOntologyWithXPATH() throws Exception
	{
		/*
		 * This report has 1 node
		 */
		MappingResourceUtil mappingUtil = new MappingResourceUtil();
		String mapping = ResourceBag.getResource(mappingUtil.getGroupName(), "networkMapping.xml");
		
		String ontology = OntologyResourceUtil.getInstance().getOntologyFileContent();
		
		ReportResourceUtil reportResource = new ReportResourceUtil();
		String xmlDocument = ResourceBag.getResource(reportResource.getGroupName(), "nuriSetNetwork.xml");
		
		XMLOntologyTransformWithXpathQueryImp transform = new XMLOntologyTransformWithXpathQueryImp(xmlDocument,
				mapping, ontology);
		transform.transform();
		assertTrue(true);
		transform.getOntologyUtil().saveOntology("NuriSetNetwork.owl");
	}
	
	@Test
	public void testTransformNexposeToOntologyWithXPATH() throws Exception
	{
		/*
		 * This report has 1 node
		 */
		MappingResourceUtil mappingUtil = new MappingResourceUtil();
		String mapping = ResourceBag.getResource(mappingUtil.getGroupName(), "nexposeMapping.xml");
		
		String ontology = OntologyResourceUtil.getInstance().getOntologyFileContent();
		
		ReportResourceUtil reportResource = new ReportResourceUtil();
		String xmlDocument = ResourceBag.getResource(reportResource.getGroupName(), "nexpose_2013-05-17_11.35,02.xml");
		
		XMLOntologyTransformWithXpathQueryImp transform = new XMLOntologyTransformWithXpathQueryImp(xmlDocument,
				mapping, ontology);
		transform.transform();
		assertTrue(true);
		transform.getOntologyUtil().saveOntology("47nexpose.owl");
	}
	
	@Test
	public void testTransformAddDevice() throws Exception
	{
		
		MappingResourceUtil mappingUtil = new MappingResourceUtil();
		String mapping = ResourceBag.getResource(mappingUtil.getGroupName(), "addDeviceMapping.xml");
		
		String ontology = OntologyResourceUtil.getInstance().getOntologyFileContent();
		
		ReportResourceUtil reportResource = new ReportResourceUtil();
		String xmlDocument = ResourceBag.getResource(reportResource.getGroupName(), "addDevice XML.xml");
		
		XMLOntologyTransformWithXpathQueryImp transform = new XMLOntologyTransformWithXpathQueryImp(xmlDocument,
				mapping, ontology);
		transform.transform();
		transform.getOntologyUtil().saveOntology("addDeviceTest.owl");
	}
	
	@Test
	public void testTransformNmapToOntologyWithXPATH() throws Exception
	{
		
		MappingResourceUtil mappingUtil = new MappingResourceUtil();
		String mapping = ResourceBag.getResource(mappingUtil.getGroupName(), "nmapMapping.xml");
		
		String ontology = OntologyResourceUtil.getInstance().getOntologyFileContent();
		
		ReportResourceUtil reportResource = new ReportResourceUtil();
		String xmlDocument = ResourceBag.getResource(reportResource.getGroupName(), "nmap.xml");
		
		XMLOntologyTransformWithXpathQueryImp transform = new XMLOntologyTransformWithXpathQueryImp(xmlDocument,
				mapping, ontology);
		transform.transform();
		transform.getOntologyUtil().saveOntology("nmapOntolojiTye.owl");
		assertTrue(true);
	}
	
	@Test
	public void testConvertThreatXmlFileToOntology() throws ParserConfigurationException, SAXException, IOException,
		JAXBException, OWLException, XPathExpressionException, XPathFactoryConfigurationException
	{
		
		MappingResourceUtil mappingUtil = new MappingResourceUtil();
		String mapping = ResourceBag.getResource(mappingUtil.getGroupName(), "threatXmlMapping.xml");
		
		String ontology = OntologyResourceUtil.getInstance().getOntologyFileContent();
		
		ReportResourceUtil reportResource = new ReportResourceUtil();
		String xmlDocument = ResourceBag.getResource(reportResource.getGroupName(), "newThreat.xml");
		
		XMLOntologyTransformWithXpathQueryImp transform = new XMLOntologyTransformWithXpathQueryImp(xmlDocument,
				mapping, ontology);
		transform.transform();
		
	}
	
}
