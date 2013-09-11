/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gov.camara.quadrocomparativo;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 *
 * @author p_7174
 */
public class App extends Application {

    public static final String DEFAULT_URL = "http://localhost:8080/";
    private static Server server;
    private static Thread serverThread;

    private void init(Stage primaryStage) {

        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        WebView webView = new WebView();

        final WebEngine webEngine = webView.getEngine();
        webEngine.load(DEFAULT_URL);

        root.getChildren().add(webView);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setTitle("Quadro Comparativo");

        webView.setPrefSize(bounds.getWidth(), bounds.getHeight() - 25);
    }

    private void initServer() {

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server = new Server();
                    SelectChannelConnector connector = new SelectChannelConnector();
                    connector.setPort(8080);
                    server.addConnector(connector);

                    ResourceHandler resource_handler = new ResourceHandler();
                    //resource_handler.setDirectoriesListed(true);
                    resource_handler.setWelcomeFiles(new String[]{"index.html"});

                    URL baseUrl = App.class.getClassLoader().getResource("index.html");
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

                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        });

        serverThread.start();
        
        while (server == null || !server.isStarted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(App.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        initServer();
        init(primaryStage);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if (server != null && server.isStarted()) {
            server.stop();
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

        launch(args);

        try {
            serverThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
