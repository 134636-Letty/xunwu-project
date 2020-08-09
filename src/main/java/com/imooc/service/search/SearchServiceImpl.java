package com.imooc.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.Longs;
import com.google.gson.Gson;
import com.imooc.base.HouseSort;
import com.imooc.base.RentValueBlock;
import com.imooc.entity.House;
import com.imooc.entity.HouseDetail;
import com.imooc.entity.HouseTag;
import com.imooc.repository.HouseDetailRepository;
import com.imooc.repository.HouseRepository;
import com.imooc.repository.HouseTagRepository;
import com.imooc.service.ServiceMultiResult;
import com.imooc.service.ServiceResult;
import com.imooc.web.form.RentSearch;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Service
public class SearchServiceImpl implements ISearchService {

    private static final Logger log = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME="xunwu";

    private static final String INDEX_TYPE="house";

    private static final String INDEX_TOPIC = "house_build";

    @Autowired
    HouseRepository houseRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TransportClient esClient;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    HouseDetailRepository detailRepository;
    @Autowired
    HouseTagRepository tagRepository;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @KafkaListener(topics = INDEX_TOPIC)
    public void handleMessage(String content){
        Gson gson = new Gson();
        HouseIndexMessage message = gson.fromJson(content,HouseIndexMessage.class);
        switch (message.getOperation()){
            case HouseIndexMessage.INDEX:
                this.createOrUpdateIndex(message);
                break;
            case HouseIndexMessage.REMOVE:
                this.removeIndex(message);
                break;
            default:
                log.warn("Not support message content"+ content);
                break;

        }
    }
    public void createOrUpdateIndex(HouseIndexMessage message){
        long houseId = message.getHouseId();
        House house = houseRepository.findOne(houseId);
        if (null == house){
            log.error("Index house {} does not exist!",houseId);
            return;
        }

        HouseIndexTemplate indexTemplate =modelMapper.map(house,HouseIndexTemplate.class);

        HouseDetail detail = detailRepository.findAllByHouseId(houseId);
        if (null ==detail){
        }
        modelMapper.map(detail,HouseIndexTemplate.class);

        List<HouseTag> tagList = tagRepository.findAllByHouseId(houseId);
        if (!CollectionUtils.isEmpty(tagList)){
            List<String> tagStrList = new ArrayList<>();
            tagList.forEach(tag->{tagStrList.add(tag.getName());});
            indexTemplate.setTags(tagStrList);
        }

        SearchRequestBuilder requestBuilder= this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseId));

        log.debug(requestBuilder.toString());

        boolean success;
        SearchResponse searchResponse = requestBuilder.get();
        long totalHit = searchResponse.getHits().getTotalHits();
        if (totalHit == 0){
            success= create(indexTemplate);
        }else if (totalHit == 1){
            String esId =searchResponse.getHits().getAt(0).getId();
            success=update(esId,indexTemplate);
        }else {
            success= deleteAndCreate(totalHit,indexTemplate);
        }

        if (!success){
            this.index(message.getHouseId(),message.getRetry()+1);
        }else {
            log.debug("Index success with house"+houseId);
        }
    }

    private void removeIndex(HouseIndexMessage message) {
        long houseId = message.getHouseId();
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseId))
                .source(INDEX_NAME);
        log.debug("Delete by query for house:" + builder);
        BulkByScrollResponse response = builder.get();
        long delete = response.getDeleted();
        log.debug("Delete total"+delete);

        if (delete <=0){
            log.warn("Did not remove data from es for response:"+response);
            this.remove(houseId,message.getRetry()+1);
        }
    }

    @Override
    public void index(Long houseId) {
      this.index(houseId,0);
    }

    public void index(Long houseId,int retry){
        if (retry > HouseIndexMessage.MAX_RETRY){
            log.error("Retry index times over 3 for house:" + houseId +"Please check it!");
            return;
        }
        HouseIndexMessage message= new HouseIndexMessage(houseId,HouseIndexMessage.INDEX,retry);

        Gson gson = new Gson();
        try {
            kafkaTemplate.send(INDEX_TOPIC,gson.toJson(message));
        }catch (Exception e){
            log.error("Json encode error for"+message);
        }
    }

    private boolean create(HouseIndexTemplate indexTemplate){
        try {
            IndexResponse response = this.esClient.prepareIndex(INDEX_NAME,INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(indexTemplate),
                    XContentType.JSON).get();

            log.debug("Create index with house:"+indexTemplate.getHouseId()); //todo ??
            if (response.status() == RestStatus.CREATED){
                return true;
            }else {
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error to index house" + indexTemplate.getHouseId(),e);
            return false;
        }
    }

    public boolean update(String esId,HouseIndexTemplate indexTemplate){
        try {
            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME,INDEX_TYPE,esId)
                    .setDoc(objectMapper.writeValueAsBytes(indexTemplate),
                            XContentType.JSON).get();

            log.debug("update index with house:"+indexTemplate.getHouseId()); //todo ??
            if (response.status() == RestStatus.OK){
                return true;
            }else {
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error to index house" + indexTemplate.getHouseId(),e);
            return false;
        }
    }

    public boolean deleteAndCreate(long totalHit,HouseIndexTemplate houseIndexTemplate){
           DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseIndexTemplate.getHouseId()))
                .source(INDEX_NAME);
           log.debug("Delete by query for house:"+builder);
           BulkByScrollResponse response = builder.get();
           long delete = response.getDeleted();
           if (delete != totalHit){
               log.warn("Need delete {},but {} was delete",totalHit,delete);
               return false;
           }else {
               return create(houseIndexTemplate);
           }
    }


    @Override
    public void remove(Long houseId) {
        this.remove(houseId,0);
    }

    public void remove(Long houseId,int retry){
        if (retry > HouseIndexMessage.MAX_RETRY){
            log.error("Retry remove times over 3 for house:"+houseId+"Please check it!");
            return;
        }

        HouseIndexMessage message = new HouseIndexMessage(houseId,HouseIndexMessage.REMOVE,retry);
         Gson gson = new Gson();
         this.kafkaTemplate.send(INDEX_TOPIC,gson.toJson(message));
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(
                QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME,rentSearch.getCityEnName())
        );

        if (!StringUtils.isEmpty(rentSearch.getRegionEnName()) && !"*".equals(rentSearch.getRegionEnName())){
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME,rentSearch.getRegionEnName())
            );
        }

        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() >0){
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() >0){
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)){
            RangeQueryBuilder priceBuilder = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0){
                priceBuilder.lte(price.getMax());
            }
            if (price.getMin() >0){
                priceBuilder.gte(price.getMin());
            }
            boolQuery.filter(priceBuilder);
        }

        if (rentSearch.getDirection() > 0){
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION,rentSearch.getDirection())
            );
        }
        if (rentSearch.getRentWay() > -1){
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY,rentSearch.getRentWay())
            );
        }

        boolQuery.must(
                QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                        HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,
//                        HouseIndexKey.DIRECTION, //
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME)
        );


        SearchRequestBuilder searchRequestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)
                .addSort(HouseSort.getSortKey(rentSearch.getOrderBy()), SortOrder.fromString(rentSearch.getOrderDirection()))
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize());

        log.debug(searchRequestBuilder.toString());

        List<Long> houseIds = new ArrayList<>();
        SearchResponse response = searchRequestBuilder.get();
        if ( response.status() != RestStatus.OK){
            log.warn("Search status is no ok for"+ searchRequestBuilder);
            return new ServiceMultiResult<>(0,houseIds);
        }

        for (SearchHit hit : response.getHits()){
           houseIds.add(Longs.tryParse(String.valueOf(hit.getSource().get(HouseIndexKey.HOUSE_ID))));
        }

        return new ServiceMultiResult<>(response.getHits().totalHits,houseIds);
    }

















}
