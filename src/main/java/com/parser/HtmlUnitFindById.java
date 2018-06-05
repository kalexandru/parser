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


    public static void main(String[] args) {
        boolean isNotEmpty = (args.length != 0);
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
     *
     * @param args
     * @return
     */
    private static boolean checkArgs(String... args) {

        if (args.length == 2 && !args[0].isEmpty()&&!args[1].isEmpty()) {
            return true;
        } else if (args.length == 1) {
            LOGGER.warn("Incorrect arguments specified !!!");
        } else {
            return false;
        }
        return true;
    }


    /**
     * Prepare configuration to be available during program execution in runtime
     *
     * @return
     * @throws IOException
     */

    private static Properties getConfiguration() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = null;
        inputStream = loader.getResourceAsStream(PROPERTIES);
        properties.load(inputStream);
        return properties;
    }

    /**
     *
     * @param element
     * @param pageToSearch
     * @return
     */
    private static Element findElementInDiffCase(HtmlAnchor element, HtmlPage pageToSearch) {
        Element result = null;
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
     * @param client
     * @param item
     * @return
     */

    private static HtmlPage openPage(WebClient client, String item) {
        HtmlPage page = null;
        String property = properties.getProperty(ADDRESS_CONFIG);
        String query = property + item;
        try {
            page = (HtmlPage) client.getPage(query);
        } catch (IOException e) {
            LOGGER.error("Can't continue working with document. Exception -", e);
        }
        return page;
    }

    /**
     *
     * @return
     */

    private static WebClient configureWebClient() {
        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);
        client.setJavaScriptEnabled(false);
        client.setCssEnabled(false);
        return client;
    }

}
