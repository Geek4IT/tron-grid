package org.tron.trongrid.db;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class EventWriterController{

    @RequestMapping(method = RequestMethod.POST, value = "/send")
    @ResponseBody
    public String healthCheck(@RequestParam(value="data", required = true) String data,
                              @RequestHeader("Auth-Secret") String key
                              ){
        System.out.println(data);
        System.out.println("\n");
        System.out.println(key);
        System.out.println("\n");
        return data;

    }
}
