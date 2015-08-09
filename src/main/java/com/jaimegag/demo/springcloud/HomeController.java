package com.jaimegag.demo.springcloud;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    //@Autowired(required = false) DataSource dataSource;
    @Autowired(required = false) RedisConnectionFactory redisConnectionFactory;
    //@Autowired(required = false) MongoDbFactory mongoDbFactory;
    //@Autowired(required = false) ConnectionFactory rabbitConnectionFactory;
    
    @Autowired(required = false) RedisTemplate<String, String> redisTemplate;
    
    @Autowired(required = false) ApplicationInstanceInfo instanceInfo;

    @RequestMapping("/")
    public String home(Model model) {
        Map<Class<?>, String> services = new LinkedHashMap<Class<?>, String>();
        //services.put(dataSource.getClass(), toString(dataSource));
        //services.put(mongoDbFactory.getClass(), toString(mongoDbFactory));
        services.put(redisConnectionFactory.getClass(), toString(redisConnectionFactory));
        //services.put(rabbitConnectionFactory.getClass(), toString(rabbitConnectionFactory));
        model.addAttribute("services", services.entrySet());
        
        model.addAttribute("instanceInfo", instanceInfo);
        
        return "home";
    }

    @RequestMapping("/killSwitch")
    public void die() {
   		System.exit(1);
    }
    
    @RequestMapping("/visits")
    public String visits(Model model) {
    	try {
    		RedisRepository repo = getRedisRepo();
    		Map<String, String> store = repo.getKeyStore();
    		model.addAttribute("store", store);
    	} catch (Exception e) {
    		System.out.println("Exception on visits path: " + e.getMessage());
    		model.addAttribute("noRedisAvailable", true);
    	}
        return "visits";
    }
    
    @RequestMapping("/logVisit")
    public String logVisits(Model model, @RequestHeader ("User-Agent") String userAgent,
    		@RequestHeader ("X-Forwarded-For") String xForwardedFor,
    		@RequestParam ("name") String name, HttpServletRequest request) {
    	try {
    		VisitLog visit = new VisitLog(xForwardedFor, userAgent);
    		System.out.println(visit.toString());
    		ObjectMapper mapper = new ObjectMapper();
    		String jsonVisit = mapper.writeValueAsString(visit);
    		System.out.println(jsonVisit);
    		RedisRepository repo = getRedisRepo();
    		repo.add(name, jsonVisit);
    		Map<String, String> store = repo.getKeyStore();
    		model.addAttribute("store", store);
    	} catch (Exception e) {
    		System.out.println("Exception on logVisits path: " + e.getMessage());
    		model.addAttribute("noRedisAvailable", true);
    	}
        return "visits";
    }
    
    private String toString(DataSource dataSource) {
        if (dataSource == null) {
            return "<none>";
        } else {
            try {
                Field urlField = ReflectionUtils.findField(dataSource.getClass(), "url");
                ReflectionUtils.makeAccessible(urlField);
                return stripCredentials((String) urlField.get(dataSource));
            } catch (Exception fe) {
                try {
                    Method urlMethod = ReflectionUtils.findMethod(dataSource.getClass(), "getUrl");
                    ReflectionUtils.makeAccessible(urlMethod);
                    return stripCredentials((String) urlMethod.invoke(dataSource, (Object[])null));
                } catch (Exception me){
                    return "<unknown> " + dataSource.getClass();                    
                }
            }
        }
    }
    
    private String toString(MongoDbFactory mongoDbFactory) {
        if (mongoDbFactory == null) {
            return "<none>";
        } else {
            try {
                return mongoDbFactory.getDb().getMongo().getAddress().toString();
            } catch (Exception ex) {
                return "<invalid address> " + mongoDbFactory.getDb().getMongo().toString();
            }
        }
    }

    private RedisRepository getRedisRepo() {
    	RedisRepository repo = new RedisRepository(redisTemplate);
    	return repo;
    }
    private String toString(RedisConnectionFactory redisConnectionFactory) {
        if (redisConnectionFactory == null) {
            return "<none>";
        } else {
            if (redisConnectionFactory instanceof JedisConnectionFactory) {
                JedisConnectionFactory jcf = (JedisConnectionFactory) redisConnectionFactory;;
                return jcf.getHostName().toString() + ":" + jcf.getPort() + " - Jedis";
            } else if (redisConnectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lcf = (LettuceConnectionFactory) redisConnectionFactory;
                return lcf.getHostName().toString() + ":" + lcf.getPort() + " - Lettuce";
            }
            return "<unknown> " + redisConnectionFactory.getClass();
        }
    }
    

    private String toString(ConnectionFactory rabbitConnectionFactory) {
        if (rabbitConnectionFactory == null) {
            return "<none>";
        } else {
            return rabbitConnectionFactory.getHost() + ":"
                    + rabbitConnectionFactory.getPort();
        }
    }
    
    private String stripCredentials(String urlString) {
        try {
            if (urlString.startsWith("jdbc:")) {
                urlString = urlString.substring("jdbc:".length());
            }
            URI url = new URI(urlString);
            return new URI(url.getScheme(), null, url.getHost(), url.getPort(), url.getPath(), null, null).toString();
        }
        catch (URISyntaxException e) {
            System.out.println(e);
            return "<bad url> " + urlString;
        }
    }

}
