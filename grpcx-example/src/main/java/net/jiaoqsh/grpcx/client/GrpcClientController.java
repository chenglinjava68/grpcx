package net.jiaoqsh.grpcx.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrpcClientController {

    @Autowired
    private GreeterClientService greeterClientService;

    @RequestMapping("/hello")
    public String hello(@RequestParam(defaultValue = "Jack") String name) {
        return greeterClientService.sayHello(name);
    }
}
