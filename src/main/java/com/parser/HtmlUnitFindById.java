package com.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;



public class HtmlUnitFindById {

    // Properties Constants
    private static final String PROPERTIES = "config.properties";
    private static final String ELEMENT_ID = "original.elementId";
    private static final String ADDRESS_CONFIG = "address.configuration";

    private static Logger LOGGER = LoggerFactory.getLogger(HtmlUnitFindById.class);

    private static Properties properties;

    // initialization of the properties
    static {
        try {
            properties = getConfiguration();
        } catch (IOException e) {
            LOGGER.error("System properties file is missing",e);
        }
    }

   // program entry point
    public static void main(String[] args) {
        HtmlPage original = null;
        HtmlPage diffPage = null;
        Element resultElement = null;
        Object originalElement = null;
        WebClient client = configureWebClient();
        if (checkArgs(args)) {
            original = openPage(client, args[0]);
            diffPage = openPage(client, args[1]);
            originalElement = original.getElementById(properties.getProperty(ELEMENT_ID));
            if (originalElement instanceof HtmlAnchor) {
                resultElement = findElementInDiffCase((HtmlAnchor) originalElement, diffPage);
            }
            if(resultElement != null) {
                String result = ((DomElement) resultElement).getCanonicalXPath();
                LOGGER.info(resultElement.toString());
                LOGGER.info(result.replace("/", ">"));
            }
        }

    }

    /**
     * Validates the command line input parameters
     *
     * @param args - command line input parameters to check
     * @return if args are valid to continue execution true else false
     */
    private static boolean checkArgs(String... args) {
        if (args.length == 2 && !args[0].isEmpty()&&!args[1].isEmpty()) {
            return true;
        } else if (args.length == 1) {
            LOGGER.warn("Incorrect arguments specified !!!");
            return false;
        } else {
            return false;
        }
    }


    /**
     * Prepare configuration to be available during program execution in runtime
     *
     * @return System properties
     * @throws IOException -  IllegalArgumentException if the input stream contains a
     *                        malformed Unicode escape sequence.
     */

    private static Properties getConfiguration() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = loader.getResourceAsStream(PROPERTIES);
        properties.load(inputStream);
        return properties;
    }

    /**
     * Finds changed element in the diff case
     *
     * @param element - original element used in the search of the diff
     * @param pageToSearch - instance of the HtmlPage to search diff
     * @return Element of the XML/HTML page
     */
    private static Element findElementInDiffCase(HtmlAnchor element, HtmlPage pageToSearch) {
        Element result;
        String hrefValue = element.getHrefAttribute();
        List<HtmlAnchor> htmlAnchors = pageToSearch.getAnchors();
        List<HtmlAnchor> filtered =
                htmlAnchors.stream().filter(hr -> hr.getHrefAttribute().contains(hrefValue.replace("#",""))).collect(Collectors.toList());
        if (filtered.size() > 1) {
            result = filtered.stream().filter(hr -> hr.getClassAttribute().equals(element.getClassAttribute())).collect(Collectors.toList()).get(0);
        } else {
            result = filtered.get(0);
        }
        return result;
    }

    /**
     * Opens specified html page
     *
     * @param client - WebClient instance
     * @param path - path to the file on the local
     * @return HtmlPage instance
     */

    private static HtmlPage openPage(WebClient client, String path) {
        HtmlPage page = null;
        String property = properties.getProperty(ADDRESS_CONFIG);
        String query = property + path;
        try {
            page = client.getPage(query);
        } catch (IOException e) {
            LOGGER.error("Can't continue working with document. Exception -", e);
        }
        return page;
    }

    /**
     * Create an instance of the WebClient (Browser)
     * without CSS and JavaScript support
     *
     * @return instance of the WebClient
     */

    private static WebClient configureWebClient() {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);
        client.setJavaScriptEnabled(false);
        client.setCssEnabled(false);
        return client;
    }

}
