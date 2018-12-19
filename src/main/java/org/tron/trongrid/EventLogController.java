package org.tron.trongrid;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EventLogController {

  @Autowired
  EventLogRepository eventLogRepository;
  @Autowired
  MongoTemplate mongoTemplate;

  Logger logger = LoggerFactory.getLogger(EventLogController.class);


  @RequestMapping(method = RequestMethod.GET, value = "/healthcheck")
  public String  healthCheck(){
    return "OK";
  }

  @RequestMapping(method = RequestMethod.GET, value = "/events")
  public List<EventLogEntity> events(
          @RequestParam(value="since", required=false, defaultValue = "0" ) long timestamp,
          @RequestParam(value="block", required=false, defaultValue = "-1" ) long blocknum,
          HttpServletRequest request) {

    QueryFactory query = new QueryFactory(timestamp, blocknum);
    query.setPageniate(this.setPagniateVariable(request));
    System.out.println(query.toString());

    List<EventLogEntity> result = mongoTemplate.find(query.getQuery(),EventLogEntity.class);
    return result;

  }
  @RequestMapping(method = RequestMethod.GET, value = "/event/transaction/{transactionId}")
  public List<EventLogEntity> findOneByTransaction(@PathVariable String transactionId) {
    long startTime = System.nanoTime();
    List<EventLogEntity> result = eventLogRepository.findByTransactionId(transactionId);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.info("DEBUG: /event/transaction/ takes {} nano seconds", String.valueOf(totalTime));
    return result;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/contract/{contractAddress}")
  public List<EventLogEntity> findByContractAddress(@PathVariable String contractAddress,
                                                    @RequestParam(value="since", required=false, defaultValue = "0" ) long timestamp,
                                                    @RequestParam(value="block", required=false, defaultValue = "-1" ) long blocknum,
                                                    HttpServletRequest request) {
    long startTime = System.nanoTime();
    QueryFactory query = QueryFactory.intialize(request);
//    query.setContractAddress(contractAddress);
    System.out.println(query.toString());
    List<EventLogEntity> result = mongoTemplate.find(query.getQuery(),EventLogEntity.class,contractAddress);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.info("DEBUG: /event/contract/ takes {} nano seconds", String.valueOf(totalTime));
    return result;

  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/contract/{contractAddress}/{eventName}")
  public List<EventLogEntity> findByContractAddressAndEntryName(
          @PathVariable String contractAddress,
          @PathVariable String eventName,
          @RequestParam(value="since", required=false, defaultValue = "0" ) long timestamp,
          @RequestParam(value="block", required=false, defaultValue = "-1" ) long blocknum,
          HttpServletRequest request) {

    long startTime = System.nanoTime();
    QueryFactory query = QueryFactory.intialize(request);
//    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    System.out.println(query.toString());
    List<EventLogEntity> result = mongoTemplate.find(query.getQuery(),EventLogEntity.class,contractAddress);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.info("DEBUG: /event/contract/event takes {} nano seconds", String.valueOf(totalTime));
    return result;

  }

  @RequestMapping(method = RequestMethod.GET, value = "/eventall/contract/{contractAddress}/{eventName}")
  public List<EventLogEntity> findByContractAddressAndEntryName_compare(
          @PathVariable String contractAddress,
          @PathVariable String eventName,
          @RequestParam(value="since", required=false, defaultValue = "0" ) long timestamp,
          @RequestParam(value="block", required=false, defaultValue = "-1" ) long blocknum,
          HttpServletRequest request) {

    long startTime = System.nanoTime();
    QueryFactory query = QueryFactory.intialize(request);
    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    System.out.println(query.toString());
    List<EventLogEntity> result = mongoTemplate.find(query.getQuery(),EventLogEntity.class);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.info("DEBUG: /eventall/contract/event takes {} nano seconds", String.valueOf(totalTime));
    return result;

  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/contract/{contractAddress}/{eventName}/{blockNumber}")
  public List<EventLogEntity> findByContractAddressAndEntryNameAndBlockNumber(
      @PathVariable String contractAddress,
      @PathVariable String eventName,
      @PathVariable long blockNumber) {

    long startTime = System.nanoTime();
    QueryFactory query = new QueryFactory();
//    query.setContractAddress(contractAddress);
    query.setEventName(eventName);
    System.out.println(query.toString());
    query.setBockNum(blockNumber);
    List<EventLogEntity> result = mongoTemplate.find(query.getQuery(),EventLogEntity.class, contractAddress);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.info("DEBUG: /event/contract/event/blocknumber takes {} nano seconds", String.valueOf(totalTime));
    return result;

  }

  @RequestMapping(method = RequestMethod.GET, value = "/event/filter/contract/{contractAddress}/{eventName}")
  public List<EventLogEntity> filterevent(
          @RequestParam Map<String,String> allRequestParams,
          @PathVariable String contractAddress,
          @PathVariable String eventName,

          @RequestParam(value="since", required=false, defaultValue = "0" ) Long since_timestamp,
          @RequestParam(value="block", required=false, defaultValue = "-1" ) long blocknum,
          HttpServletRequest request){
    long startTime = System.nanoTime();
    Query query = new Query();
    query.addCriteria(Criteria.where("contract_address").is(contractAddress));
    query.addCriteria(Criteria.where("event_name").is(eventName));
    query.addCriteria((Criteria.where("block_timestamp").gte(since_timestamp)));

    if (blocknum > 0)
      query.addCriteria((Criteria.where("block_number").gte(blocknum)));

    try {
      JSONObject res = JSONObject.parseObject(allRequestParams.get("result"));
      for (String k : res.keySet()) {
        if (QueryFactory.isBool(res.getString(k))) {
          query.addCriteria(Criteria.where(String.format("%s.%s", "result", k)).is(Boolean.parseBoolean(res.getString(k))));
          continue;
        }
        query.addCriteria(Criteria.where(String.format("%s.%s", "result", k)).is((res.getString(k))));
      }
    } catch (JSONException e){

    }catch (java.lang.NullPointerException e){

    }

    query.with(this.setPagniateVariable(request));
    System.out.println(query.toString());
    List<EventLogEntity> result = mongoTemplate.find(query,EventLogEntity.class, contractAddress);
    long endTime   = System.nanoTime();
    long totalTime = endTime - startTime;
    logger.debug("/event/filter takes {} nano seconds", String.valueOf(totalTime));
    return result;
  }

  private Pageable setPagniateVariable(HttpServletRequest request){

    // variables for pagniate
    int page = 0;
    int page_size = 20;
    String sort = "-block_timestamp";

    if (request.getParameter("page") != null && request.getParameter("page").length() > 0)
      page = Integer.parseInt(request.getParameter("page"));
    else
      page = 0;
    if (request.getParameter("size") != null && request.getParameter("size").length() > 0)
      page_size = Integer.parseInt(request.getParameter("size"));
    else
      page_size = 20;
    if (request.getParameter("sort") != null && request.getParameter("sort").length() > 0)
      sort = request.getParameter("sort");
    else
      sort = "-block_timestamp";

    return QueryFactory.make_pagination(Math.max(0,page-1),Math.min(200,page_size),sort);

  }


}
