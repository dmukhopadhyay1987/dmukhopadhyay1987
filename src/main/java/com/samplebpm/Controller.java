package com.samplebpm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping(value = "/test/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return "Hello".concat(StringUtils.SPACE).concat(name);
    }

}
