/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo.main;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ProxySelector;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import com.btr.proxy.search.ProxySearch;

/**
 *
 * @author p_7174
 */
public class AppNoFX  {
	private static final Logger logger = Logger.getLogger(AppNoFX.class.getName());
            
	private static Server server;
    private static Thread serverThread;

    private static final int DEFAULT_PORT = 36486;
    private static final int PORT = getPort();
    
    private static int getPort(){
    	if (getPort(DEFAULT_PORT)> 0){
    		return DEFAULT_PORT;
    	} else {
    		int port = getPort(0);
    		if (port> 0){
    			return port;
    		}
    	}
    	
		logger.log(Level.SEVERE, "Problema de Entrada/Saída. Não foi possível obter uma porta para conexão.");
		
		return -1;
    }
    
    private static int getPort(int port){
		try {
			ServerSocket ss = new ServerSocket(port,0,InetAddress.getLocalHost());
			int l_port = ss.getLocalPort();
			
			if (ss != null && !ss.isClosed()){
				ss.close();
				return l_port;
			}
		} catch (BindException e){
			logger.log(Level.SEVERE, "Porta "+port+" já está em uso.", e);
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, "Host não conhecido. Não foi possível obter uma porta para conexão.", e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problema de Entrada/Saída. Não foi possível obter uma porta para conexão.", e);
		}
    	return -1;
    }
    
    private static final String URL ="http://127.0.0.1:"+PORT+"/";
    
    public static String getURL(){
    	return URL;    	
    }

    private void initServer() {

        serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    server = new Server();
                    
                    SelectChannelConnector connector = new SelectChannelConnector();
                    connector.setPort(PORT);
                    server.addConnector(connector);

                    ResourceHandler resource_handler = new ResourceHandler();
                    //resource_handler.setDirectoriesListed(true);
                    resource_handler.setWelcomeFiles(new String[]{"index.html"});

                    URL baseUrl = AppNoFX.class.getClassLoader().getResource("index.html");
                    String base = baseUrl.toExternalForm().replaceAll("index.html", "");
                    //System.out.println(base);
                    resource_handler.setResourceBase(base);
                    
                    WebAppContext context = new WebAppContext();
                    
                    context.setDescriptor(base + "WEB-INF/web.xml");
                    context.setResourceBase(base);
                    context.setContextPath("/");
                    context.setParentLoaderPriority(true);

                    HandlerList handlers = new HandlerList();
                    handlers.setHandlers(new Handler[]{resource_handler, context, 
                                new DefaultHandler()});
                    server.setHandler(handlers);

                    server.start();
                    server.join();
                    
                    System.out.println("Go to: "+getURL());
                    logger.log(Level.INFO, "Go to: "+getURL());

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        };

        serverThread.start();
        
        while (server == null || !server.isStarted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Executa o servidor da aplicação
     */
    public static void startServer(){
    	ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector myProxySelector = proxySearch.getProxySelector();
        ProxySelector.setDefault(myProxySelector);
        
    	AppNoFX app = new AppNoFX();
    	app.initServer();

        try {
            serverThread.join();
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	System.out.println(URL);

    	startServer();
    }
}
