package com.tlcsdm.eclipse.mavenview.internal.common;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class for creating secure XML parsers with XXE protection.
 */
public final class SecureXmlParser {

	private SecureXmlParser() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a {@link DocumentBuilder} with security features enabled to prevent
	 * XXE (XML External Entity) attacks.
	 *
	 * @return a securely configured {@link DocumentBuilder}
	 * @throws ParserConfigurationException if the parser cannot be configured
	 */
	public static DocumentBuilder createSecureDocumentBuilder() throws ParserConfigurationException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);

		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (ParserConfigurationException e) {
			// Feature not supported, continue
		}
		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (ParserConfigurationException e) {
			// Feature not supported, continue
		}
		try {
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (ParserConfigurationException e) {
			// Feature not supported, continue
		}
		try {
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (ParserConfigurationException e) {
			// Feature not supported, continue
		}
		try {
			factory.setExpandEntityReferences(false);
		} catch (IllegalArgumentException e) {
			// Feature not supported, continue
		}

		return factory.newDocumentBuilder();
	}
}
