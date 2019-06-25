package org.h2.server.web;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

public class H2ConsoleWebServlet extends HttpServlet {
    private transient WebServer server = new WebServer();

    public void initialize() {
        server = new WebServer();
        server.setAllowChunked(false);
        server.init();
    }

    @Override
    public void destroy() {
        server.stop();
    }

    private boolean allow(HttpServletRequest req) {
        if (server.getAllowOthers()) {
            return true;
        }
        String addr = req.getRemoteAddr();
        try {
            InetAddress address = InetAddress.getByName(addr);
            return address.isLoopbackAddress();
        } catch (UnknownHostException | NoClassDefFoundError e) {
            // Google App Engine does not allow java.net.InetAddress
            return false;
        }

    }

    private String getAllowedFile(HttpServletRequest req, String requestedFile) {
        if (!allow(req)) {
            return "notAllowed.jsp";
        }
        if (requestedFile.length() == 0) {
            return "index.do";
        }
        return requestedFile;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String basePath = "/h2-console";
        req.setCharacterEncoding("utf-8");
        String file = req.getRequestURI().replaceAll(req.getServletContext().getContextPath() + basePath, "");
        if (file.startsWith("/")) {
            file = file.substring(1);
        }
        file = getAllowedFile(req, file);

        // extract the request attributes
        Properties attributes = new Properties();
        Enumeration<?> en = req.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = req.getAttribute(name).toString();
            attributes.put(name, value);
        }
        en = req.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement().toString();
            String value = req.getParameter(name);
            attributes.put(name, value);
        }

        WebSession session = null;
        String sessionId = attributes.getProperty("jsessionid");
        if (sessionId != null) {
            session = server.getSession(sessionId);
        }
        WebApp app = new WebApp(server);
        app.setSession(session, attributes);
        String ifModifiedSince = req.getHeader("if-modified-since");

        String hostAddr = req.getRemoteAddr();
        file = app.processRequest(file, hostAddr);
        session = app.getSession();

        String mimeType = app.getMimeType();
        boolean cache = app.getCache();

        if (cache && server.getStartDateTime().equals(ifModifiedSince)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        byte[] bytes = server.getFile(file);
        if (bytes == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            bytes = ("File not found: " + file).getBytes(StandardCharsets.UTF_8);
        } else {
            if (session != null && file.endsWith(".jsp")) {
                String page = new String(bytes, StandardCharsets.UTF_8);
                page = PageParser.parse(page, session.map);
                bytes = page.getBytes(StandardCharsets.UTF_8);
            }
            resp.setContentType(mimeType);
            if (!cache) {
                resp.setHeader("Cache-Control", "no-cache");
            } else {
                resp.setHeader("Cache-Control", "max-age=10");
                resp.setHeader("Last-Modified", server.getStartDateTime());
            }
        }
        if (bytes != null) {
            ServletOutputStream out = resp.getOutputStream();
            out.write(bytes);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }
}
