package services.dtservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import services.dtservices.configs.Config;
import services.dtservices.impl.DefaultGateway;
import services.dtservices.services.GatewayService;
import services.dtservices.services.IBootable;
import services.dtservices.services.configuration.JsonGatewayConfiguration;
import services.dtservices.services.configuration.gateway.GatewayContext;
import services.dtservices.services.gateway.GatewayRequest;
import services.dtservices.services.gateway.ServiceRequest;
import services.dtservices.services.query.query.QueryHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@SpringBootApplication(scanBasePackages = "services.dtservices", exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@Configuration
public class Runner implements ApplicationRunner {
    @Autowired
    Config config;

    public void execute() {
        String[] args = new String[] {"--spring.profiles.active=gateway --startup.properties=application-gateway.properties"};

        Logger log = Logger.getLogger("services.dtservices");
        log.info("Starting gateway service");

        System.out.println("Service Gateway started");
        new SpringApplicationBuilder(this.getClass())
                .web(WebApplicationType.NONE)
                .run(args);
    }
    /**
     * for testing set the -Dspring.active.profiles=test
     * @param args
     */
     public static void main(String[] args) {
        new SpringApplicationBuilder(Runner.class)
        .web(WebApplicationType.NONE)       
        .run(args);
      
    }

    @Override
    public void run(ApplicationArguments args) {
        start(args);
    }

    public void start(ApplicationArguments args) {
        try {
            // Retrieve BeanFactory from the context
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            // Create a bean definition, the config needs to be created first
            Config conf = createConfig(args, beanFactory);
            if(conf.isGateway()){
                startGateway(context, beanFactory, conf);
            }
            if(conf.isService()){
                startService(context, beanFactory, conf);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }   
       
    }

    public void startService(AnnotationConfigApplicationContext context, DefaultListableBeanFactory beanFactory, Config conf) {

        // create query
        String serviceClassName = conf.getServiceClass();
        try {
            Class<?> serviceClass = Class.forName(serviceClassName);
            IBootable service = (IBootable) (serviceClass.getConstructor().newInstance());
            service.setConfig(config);
            service.start();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // load class 


        
    }

public void startGateway(AnnotationConfigApplicationContext context, DefaultListableBeanFactory beanFactory, Config conf) throws IOException {

         //System.out.println(this.getClass().getClassLoader().getResource("configurations/example_declaration.json"));
    String jsonConfigPath = this.getClass().getClassLoader().getResource("configurations/example_declaration.json").getFile();

    File jsonConfigFile = new File(jsonConfigPath);

    // add JsonGatewayConfiguration
    JsonGatewayConfiguration jsonGatewayConfiguration = new JsonGatewayConfiguration(jsonConfigFile);
    ConstructorArgumentValues  values = new ConstructorArgumentValues();
    values.addIndexedArgumentValue(0, conf);
    values.addIndexedArgumentValue(1, jsonGatewayConfiguration);

    // Create a bean definition for the default gateway
    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(DefaultGateway.class);
    beanDefinition.setConstructorArgumentValues(values);
    beanFactory.registerBeanDefinition("defaultGateway", beanDefinition);

    GatewayService gatewayService = beanFactory.getBean(DefaultGateway.class);
    context.refresh();


    gatewayService.executeRequestSynd(new services.dtservices.services.gateway.GatewayRequest("Servicename", new ServiceRequest()));

    HttpServer server = HttpServer.create(new InetSocketAddress(conf.getGatewayPort()), 0);
    server.createContext("/query", new GatewayRequestHandler(gatewayService));
    server.setExecutor(null); // creates a default executor
    server.start();
}

public Config createConfig(ApplicationArguments args, DefaultListableBeanFactory beanFactory) {
    GenericBeanDefinition beanDefinitionConfig = new GenericBeanDefinition();
    beanDefinitionConfig.setBeanClass(Config.class);
    beanDefinitionConfig.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
    beanFactory.registerBeanDefinition("config", beanDefinitionConfig);

    List<String> props =  args.getOptionValues("startup.properties");
    PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    String propPath = props != null && !props.isEmpty() ? props.get(0) : "application.properties";
    placeholderConfigurer.setLocation(new ClassPathResource(propPath));
    placeholderConfigurer.postProcessBeanFactory(beanFactory);

    AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
    postProcessor.setBeanFactory(beanFactory);
    beanFactory.addBeanPostProcessor(postProcessor);
    beanFactory.addBeanPostProcessor(new CommonAnnotationBeanPostProcessor());
    Config conf = beanFactory.getBean(Config.class);
    return conf;
}



    static class GatewayRequestHandler implements HttpHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    private GatewayService gatewayService;

    public GatewayRequestHandler(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {       
        String notFound = "Not Found";
        if (!t.getRequestMethod().equals("POST")) {
            t.sendResponseHeaders(404, notFound.length());
            OutputStream os = t.getResponseBody();
            os.write(notFound.getBytes());
            os.close();
            return;
        }
      
        handlePost(t);
        
    }

    private void handlePost(HttpExchange t) throws IOException {   

        try {
            GatewayRequest request = objectMapper.readValue(t.getRequestBody(), GatewayRequest.class);
            QueryHandler queryHandler = new QueryHandler(this.gatewayService);
            String s = objectMapper.writeValueAsString(queryHandler.handleQuery(request.getQuery()));
            t.sendResponseHeaders(200, s.length());
            OutputStream os = t.getResponseBody();
            os.write(s.getBytes());
            os.close();
        } catch (Exception e) {
            t.sendResponseHeaders(500, e.getMessage().length());
            OutputStream os = t.getResponseBody();
            os.write(e.getMessage().getBytes());
            os.close();
        } finally {
            t.close();
        }
    }
}

static class GatewayRequest {
    private String query;
    private String context;

    public GatewayRequest(String query, String context) {
        this.query = query;
        this.context = context;
    }

    private GatewayRequest() {
    }

    public String getQuery() {
        return query;
    }


    public String getContext() {
        return context;
    }

  
}

}
