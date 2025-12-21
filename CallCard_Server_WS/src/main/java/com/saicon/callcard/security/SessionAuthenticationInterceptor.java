package com.saicon.callcard.security;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * CXF SOAP interceptor for session-based authentication.
 *
 * Validates session tokens passed in SOAP headers and enforces authentication
 * for protected service operations. This interceptor runs in the PRE_INVOKE phase
 * before service methods are executed.
 *
 * Expected SOAP header format:
 * <pre>
 * &lt;SessionToken xmlns="http://ws.saicon.com/session"&gt;
 *   &lt;token&gt;session-token-value&lt;/token&gt;
 *   &lt;userId&gt;user-id&lt;/userId&gt;
 * &lt;/SessionToken&gt;
 * </pre>
 *
 * Integrates with IGameInternalService for session validation.
 */
public class SessionAuthenticationInterceptor extends AbstractSoapInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthenticationInterceptor.class);

    private static final String SESSION_HEADER_NAME = "SessionToken";
    private static final String SESSION_HEADER_NS = "http://ws.saicon.com/session";
    private static final QName SESSION_QNAME = new QName(SESSION_HEADER_NS, SESSION_HEADER_NAME);

    // TODO: Inject IGameInternalService client for session validation
    // @Autowired
    // private IGameInternalService gameInternalService;

    public SessionAuthenticationInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        LOGGER.debug("SessionAuthenticationInterceptor - Validating session token");

        try {
            // Extract session token from SOAP headers
            String sessionToken = extractSessionToken(message);
            String userId = extractUserId(message);

            if (sessionToken == null || sessionToken.isEmpty()) {
                LOGGER.warn("Missing session token in SOAP request");
                throw new Fault(new SecurityException("Authentication required: Missing session token"));
            }

            // Validate session with IGameInternalService
            boolean isValid = validateSession(sessionToken, userId);

            if (!isValid) {
                LOGGER.warn("Invalid session token: {}", sessionToken);
                throw new Fault(new SecurityException("Authentication failed: Invalid session token"));
            }

            // Store authenticated user info in message context for downstream processing
            message.put("authenticated.userId", userId);
            message.put("authenticated.sessionToken", sessionToken);

            LOGGER.debug("Session authentication successful for user: {}", userId);

        } catch (SecurityException e) {
            LOGGER.error("Security exception during session validation", e);
            throw new Fault(e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during session authentication", e);
            throw new Fault(new SecurityException("Authentication error: " + e.getMessage()));
        }
    }

    /**
     * Extract session token from SOAP header
     */
    private String extractSessionToken(SoapMessage message) {
        List<Header> headers = message.getHeaders();
        for (Header header : headers) {
            QName qname = header.getName();
            if (SESSION_QNAME.equals(qname)) {
                Object headerObject = header.getObject();
                if (headerObject instanceof Element) {
                    Element element = (Element) headerObject;
                    Element tokenElement = getChildElement(element, "token");
                    if (tokenElement != null) {
                        return tokenElement.getTextContent();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extract user ID from SOAP header
     */
    private String extractUserId(SoapMessage message) {
        List<Header> headers = message.getHeaders();
        for (Header header : headers) {
            QName qname = header.getName();
            if (SESSION_QNAME.equals(qname)) {
                Object headerObject = header.getObject();
                if (headerObject instanceof Element) {
                    Element element = (Element) headerObject;
                    Element userIdElement = getChildElement(element, "userId");
                    if (userIdElement != null) {
                        return userIdElement.getTextContent();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get child element by tag name
     */
    private Element getChildElement(Element parent, String tagName) {
        org.w3c.dom.NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        return null;
    }

    /**
     * Validate session token with IGameInternalService
     *
     * TODO: Implement actual validation by calling IGameInternalService.validateSession()
     */
    private boolean validateSession(String sessionToken, String userId) {
        // Placeholder implementation
        // In production, call IGameInternalService to validate session:
        //
        // try {
        //     SessionValidationResponseDTO response = gameInternalService.validateSession(sessionToken, userId);
        //     return response != null && response.isValid();
        // } catch (Exception e) {
        //     LOGGER.error("Error validating session with IGameInternalService", e);
        //     return false;
        // }

        LOGGER.warn("Session validation not yet implemented - returning true for development");
        return true; // TODO: Remove this and implement actual validation
    }
}
